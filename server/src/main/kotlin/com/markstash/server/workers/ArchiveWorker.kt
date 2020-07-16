package com.markstash.server.workers

import com.google.common.hash.Hashing
import com.markstash.api.models.Archive
import com.markstash.server.Constants
import com.markstash.server.db.BookmarkWithTags
import com.markstash.server.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.dankito.readability4j.Readability4J
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.proxy.CaptureType
import org.koin.core.qualifier.named
import org.koin.ktor.ext.get
import org.netpreserve.jwarc.HttpRequest
import org.netpreserve.jwarc.HttpResponse
import org.netpreserve.jwarc.WarcRequest
import org.netpreserve.jwarc.WarcResponse
import org.netpreserve.jwarc.WarcWriter
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import ru.yandex.qatools.ashot.AShot
import ru.yandex.qatools.ashot.shooting.ShootingStrategies
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.SecureRandom
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

class ArchiveWorker(
    private val userId: Long,
    private val bookmarkId: Long
) : Worker() {
    companion object {
        val keyPool: List<Char> = ('a'..'f') + ('0'..'9')
    }

    private val db by lazy { application.get<Database>() }
    private val archiveDir by lazy { application.get<String>(named(Constants.Storage.ARCHIVE_DIR)) }

    private val log = LoggerFactory.getLogger(ArchiveWorker::class.java)

    private lateinit var bookmark: BookmarkWithTags
    private lateinit var driver: ChromeDriver
    private var harProxy: BrowserMobProxyServer? = null
    private var warcWriter: WarcWriter? = null
    private var warcRequests = mutableMapOf<String, URI>()
    private var isWarcPaused = false
    private var warcFile: File? = null

    private var originalArchiveId: Long = 0
    private var plainArchiveId: Long = 0
    private var readabilityArchiveId: Long = 0
    private var monolithArchiveId: Long = 0
    private var monolithReadabilityArchiveId: Long = 0
    private var screenshotFullArchiveId: Long = 0
    private var harArchiveId: Long = 0
    private var warcArchiveId: Long = 0

    private val tmpDir by lazy { Files.createTempDirectory("tmp") }

    private val archivePath by lazy {
        val url = URL(bookmark.url)
        val query = url.query?.replace("&", "_")?.replace("=", "-")
        var path = url.path.substring(1).replace(Regex("![A-Za-z0-9\\-_/]"), "_")
        if (path.isBlank()) path = "index"
        var archivePrefix = "${url.host}/$path"
        if (query != null) archivePrefix += "_$query"
        if (archivePrefix.length > 500) archivePrefix = archivePrefix.substring(0, 499)
        archivePrefix
    }

    private val rootFolder by lazy { File(archiveDir).also { it.mkdirs() } }
    private val archiveFolder by lazy { File(rootFolder, archivePath).also { it.mkdirs() } }

    override suspend fun run() = withContext(Dispatchers.IO) {
        bookmark = db.bookmarkQueries.findById(userId, bookmarkId).executeAsOne()

        log.debug("Starting archive of bookmark $bookmarkId to $archivePath")

        originalArchiveId = createArchive(Archive.Type.ORIGINAL)
        plainArchiveId = createArchive(Archive.Type.PLAIN)
        readabilityArchiveId = createArchive(Archive.Type.READABILITY)
        monolithArchiveId = createArchive(Archive.Type.MONOLITH)
        monolithReadabilityArchiveId = createArchive(Archive.Type.MONOLITH_READABILITY)
        screenshotFullArchiveId = createArchive(Archive.Type.SCREENSHOT_FULL)
        harArchiveId = createArchive(Archive.Type.HAR)
        warcArchiveId = createArchive(Archive.Type.WARC)

        startProxy()
        setupDriver()
        loadPage()
        saveBasic()
        saveMonolith()
        saveScreenshot()

        driver.quit()
        pauseProxy()
        stopProxy()
        log.debug("Completed archive of bookmark $bookmarkId!")
    }

    private fun setupDriver() {
        log.debug("Creating webdriver")
        // Ideally would use --headless here but extensions don't work in headless mode :(
        // See https://stackoverflow.com/questions/45372066/is-it-possible-to-run-google-chrome-in-headless-mode-with-extensions
        val options = ChromeOptions().addArguments("--no-sandbox", "--disable-gpu", "--window-size=1920,1080", "--disable-dev-shm-usage", "--headless")
            .setExperimentalOption("prefs", mapOf(
                "profile.default_content_settings.popups" to 0,
                "download.default_directory" to tmpDir.toAbsolutePath().toString()
            ))
        harProxy?.let {
            options.addArguments("--proxy-server=127.0.0.1:${it.port}", "--ignore-certificate-errors")
        }
        driver = ChromeDriver(options)
    }

    private suspend fun loadPage() {
        log.debug("Navigating to page and waiting for it to be ready")
        driver.get(bookmark.url)

        val wait = WebDriverWait(driver, 30)
        wait.until { driver.executeScript("return document.readyState") == "complete" }

        delay(500)
        log.debug("Page loaded, scrolling to bottom")

        var scrolled = 0L
        while (true) {
            val height = driver.executeScript("return document.body.scrollHeight") as? Long ?: 1000
            if (scrolled > height) break
            driver.executeScript("window.scrollBy(0, 500)")
            scrolled += 500
            delay(750)
        }

        delay(500)
    }

    private fun startProxy() {
        val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        val key = (1..16).map { SecureRandom().nextInt(keyPool.size) }.map(keyPool::get).joinToString("") // TODO: use actual hash of warc
        val fileName = "${date}_warc_${key}.warc"
        val file = File(archiveFolder, fileName).also { warcFile = it }
        warcWriter = WarcWriter(FileOutputStream(file))

        harProxy = BrowserMobProxyServer().also { proxy ->
            proxy.start(0)
            proxy.enableHarCaptureTypes(CaptureType.getAllContentCaptureTypes() + CaptureType.getHeaderCaptureTypes())
            proxy.newHar()
            log.debug("Started warc proxy on: ${proxy.port}")
            proxy.addRequestFilter { request, contents, messageInfo ->
                if (isWarcPaused) return@addRequestFilter null

                val httpRequestBuilder = HttpRequest.Builder(request.method.name(), messageInfo.originalUrl)
                request.headers().forEach { (key, value) -> httpRequestBuilder.addHeader(key, value) }
                httpRequestBuilder.body(null, contents.binaryContents)

                val warcRequest = WarcRequest.Builder(URI.create(messageInfo.originalUrl))
                    .body(httpRequestBuilder.build())
                    .build()

                warcWriter?.write(warcRequest)
                warcRequests[messageInfo.originalUrl] = warcRequest.id()

                null
            }

            proxy.addResponseFilter { response, contents, messageInfo ->
                if (isWarcPaused) return@addResponseFilter

                val warcRequestId = warcRequests.remove(messageInfo.originalUrl) ?: return@addResponseFilter

                val httpResponseBuilder = HttpResponse.Builder(response.status.code(), response.status.reasonPhrase())
                response.headers().forEach { (key, value) -> httpResponseBuilder.addHeader(key, value) }
                httpResponseBuilder.body(null, contents.binaryContents)

                val warcResponse = WarcResponse.Builder(URI.create(messageInfo.originalUrl))
                    .body(httpResponseBuilder.build())
                    .concurrentTo(warcRequestId)
                    .build()

                warcWriter?.write(warcResponse)
            }
        }
    }

    private fun pauseProxy() {
        isWarcPaused = true
        warcWriter?.close()

        val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        val key = (1..16).map { SecureRandom().nextInt(keyPool.size) }.map(keyPool::get).joinToString("") // TODO: use actual hash of har
        val fileName = "${date}_har_${key}.har"
        val file = File(archiveFolder, fileName)
        harProxy?.har?.writeTo(file)

        db.archiveQueries.update(Archive.Status.COMPLETED, "$archivePath/$fileName", file.length().toString(), harArchiveId)
        db.archiveQueries.update(Archive.Status.COMPLETED, "$archivePath/${warcFile?.name}", warcFile?.length().toString(), warcArchiveId)

        log.debug("Saved current warc and har files")
    }

    private fun stopProxy() {
        harProxy?.stop()
        log.debug("Stopped har proxy")
    }

    private fun saveBasic() {
        log.debug("Saving basic archives")
        val html = driver.findElementByXPath("//*").getAttribute("outerHTML")
        saveArchive(originalArchiveId, Archive.Type.ORIGINAL, html)

        val article = Readability4J(driver.currentUrl, html).parse()
        saveArchive(plainArchiveId, Archive.Type.PLAIN, article.textContent)
        saveArchive(readabilityArchiveId, Archive.Type.READABILITY, article.content)
        db.bookmarkQueries.updateMetadata(article.excerpt, article.byline, bookmarkId)
    }

    private suspend fun saveMonolith() {
        val originalArchive = db.archiveQueries.findById(originalArchiveId).executeAsOne()
        val originalFile = File(File(archiveDir), originalArchive.path!!)
        val monolithFile = File.createTempFile("monolith", ".html")
        log.info("Starting monolith archive")
        val monolith = ProcessBuilder("monolith", originalFile.absolutePath, "-o", monolithFile.absolutePath)
            .start()

        var time = 0
        while (time < 60) {
            if (monolith.isAlive) {
                delay(1000)
                time++
            } else if (monolith.exitValue() == 0) {
                val html = monolithFile.readText()
                saveArchive(monolithArchiveId, Archive.Type.MONOLITH, html)

                val article = Readability4J(driver.currentUrl, html).parse()
                saveArchive(monolithReadabilityArchiveId, Archive.Type.MONOLITH_READABILITY, article.content)
                log.info("Completed monolith archive")
                return
            } else {
                log.error("Could not generate monolith: exit code ${monolith.exitValue()}")
                return
            }
        }

        log.error("Timeout waiting for monolith, killing process")
        monolith.destroyForcibly()
    }

    private fun saveScreenshot() {
        log.debug("Saving screenshot of page")
        val screenshot = AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
            .takeScreenshot(driver)
        val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        val key = (1..16).map { SecureRandom().nextInt(keyPool.size) }.map(keyPool::get).joinToString("") // TODO: use actual hash of image
        val fileName = "${date}_screenshot_${key}.png"
        val file = File(archiveFolder, fileName)
        ImageIO.write(screenshot.image, "png", file)
        db.archiveQueries.update(Archive.Status.COMPLETED, "$archivePath/$fileName", file.length().toString(), screenshotFullArchiveId)
    }

    private fun createArchive(type: Archive.Type): Long = db.transactionWithResult {
        val key = (1..16).map { SecureRandom().nextInt(keyPool.size) }.map(keyPool::get).joinToString("")
        db.archiveQueries.insert(bookmarkId, type, Archive.Status.PROCESSING, null, null, key)
        db.archiveQueries.lastInsert().executeAsOne()
    }

    private fun saveArchive(id: Long, type: Archive.Type, content: String?) {
        val status = if (content == null) Archive.Status.FAILED else Archive.Status.COMPLETED
        when {
            type == Archive.Type.PLAIN -> {
                db.archiveQueries.update(status, null, content ?: "Could not get content", id)
            }
            content == null -> {
                db.archiveQueries.update(status, null, "Could not get content", id)
            }
            else -> {
                val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                val hash = Hashing.sha256().hashString(content, StandardCharsets.UTF_8).toString()
                val fileName = "${date}_${type.name.toLowerCase()}_${hash.substring(0, 15)}.html"
                val file = File(archiveFolder, fileName)
                file.writeText(content)
                db.archiveQueries.update(status, "$archivePath/$fileName", file.length().toString(), id)
            }
        }
    }
}
