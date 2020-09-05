package com.markstash.server.workers

import com.google.common.hash.Hashing
import com.markstash.api.models.Archive
import com.markstash.server.Constants
import com.markstash.server.db.BookmarkWithTags
import com.markstash.server.db.Database
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel
import io.ktor.util.extension
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.dankito.readability4j.Readability4J
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.proxy.CaptureType
import net.mm2d.touchicon.PageIcon
import net.mm2d.touchicon.Relationship
import org.koin.core.qualifier.named
import org.koin.ktor.ext.get
import org.netpreserve.jwarc.HttpRequest
import org.netpreserve.jwarc.HttpResponse
import org.netpreserve.jwarc.WarcRequest
import org.netpreserve.jwarc.WarcResponse
import org.netpreserve.jwarc.WarcWriter
import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.SecureRandom
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import io.ktor.client.statement.HttpResponse as KtorHttpResponse

class ArchiveWorker(
    private val userId: Long,
    private val bookmarkId: Long
) : Worker() {
    companion object {
        val keyPool: List<Char> = ('a'..'f') + ('0'..'9')

        private val defaultChromeArgs = listOf("--no-sandbox", "--disable-gpu", "--window-size=1920,1080", "--headless")
    }

    private val db by lazy { application.get<Database>() }
    private val archiveDir by lazy { application.get<String>(named(Constants.Storage.ARCHIVE_DIR)) }
    private val chromeBin by lazy { application.get<String>(named(Constants.Binaries.CHROME_BIN)) }
    private val chromeUseDevShm by lazy { application.get<Boolean>(named(Constants.Settings.CHROME_USE_DEV_SHM)) }

    private val chromeArgs by lazy { defaultChromeArgs + if (chromeUseDevShm) emptyList() else listOf("--disable-dev-shm-usage") }

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
    private var screenshotArchiveId: Long = 0
    private var screenshotFullArchiveId: Long = 0
    private var harArchiveId: Long = 0
    private var warcArchiveId: Long = 0
    private var pdfArchiveId: Long = 0
    private var faviconArchiveId: Long = 0

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
        screenshotArchiveId = createArchive(Archive.Type.SCREENSHOT)
        screenshotFullArchiveId = createArchive(Archive.Type.SCREENSHOT_FULL)
        harArchiveId = createArchive(Archive.Type.HAR)
        warcArchiveId = createArchive(Archive.Type.WARC)
        pdfArchiveId = createArchive(Archive.Type.PDF)
        faviconArchiveId = createArchive(Archive.Type.FAVICON)

        startProxy()
        setupDriver()
        loadPage()
        saveFavicon()
        saveBasic()
        saveMonolith()
        saveScreenshot()

        driver.quit()
        pauseProxy()
        stopProxy()
        savePdf()
        log.debug("Completed archive of bookmark $bookmarkId!")
    }

    private fun setupDriver() {
        log.debug("Creating webdriver")
        // Ideally would use --headless here but extensions don't work in headless mode :(
        // See https://stackoverflow.com/questions/45372066/is-it-possible-to-run-google-chrome-in-headless-mode-with-extensions
        val options = ChromeOptions().addArguments(chromeArgs)
            .setBinary(chromeBin)
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
        log.debug("Starting monolith archive")
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
                log.debug("Completed monolith archive")
                monolithFile.delete()
                return
            } else {
                log.error("Could not generate monolith: exit code ${monolith.exitValue()}")
                monolithFile.delete()
                return
            }
        }

        log.error("Timeout waiting for monolith, killing process")
        monolith.destroyForcibly()
        monolithFile.delete()
    }

    private suspend fun saveScreenshot() {
        log.debug("Saving screenshot of page")
        driver.executeScript("window.scrollTo(0, 0)")
        delay(1000)

        val pageHeight = (driver.executeScript("""
            var body = document.body, html = document.documentElement;
            return Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight)
        """.trimIndent()) as Number).toInt()
        val viewportHeight = (driver.executeScript("return window.innerHeight || documentElement.clientHeight|| body.clientHeight") as Number).toInt()
        val dpi = (driver.executeScript("return window.devicePixelRatio || 1") as Number).toDouble()

        log.debug("Page height: $pageHeight, viewportHeight: $viewportHeight")

        val screenshots = mutableListOf<File>()
        var capturedHeight = 0
        while (capturedHeight < pageHeight) {
            screenshots += driver.getScreenshotAs(OutputType.FILE)
            driver.executeScript("window.scrollBy(0, ${viewportHeight})")
            capturedHeight += viewportHeight
            delay(1000)
        }

        if (screenshots.isEmpty()) {
            log.error("Could not save any screenshots")
            return
        }

        val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        val key = (1..16).map { SecureRandom().nextInt(keyPool.size) }.map(keyPool::get).joinToString("") // TODO: use actual hash of image
        val fileName = "${date}_screenshot_${key}.png"
        val file = File(archiveFolder, fileName)
        Files.copy(screenshots.first().toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        db.archiveQueries.update(Archive.Status.COMPLETED, "$archivePath/$fileName", file.length().toString(), screenshotArchiveId)
        log.debug("Completed screenshot archive")
        file.delete()

        val extraHeight = ((viewportHeight - (pageHeight % viewportHeight)).takeIf { it < pageHeight } ?: 0) * dpi
        log.debug("Finished scrolling, saved ${screenshots.size} screenshots. Starting imagemagick merge, cutting off extra height: $extraHeight")

        val mergedScreenshot = File.createTempFile("screenshot", ".png")
        val convertProcess = ProcessBuilder(
            listOf("convert")
                + screenshots.map { it.absolutePath }
                + listOf("-set", "page", "+0+%[fx:u[t-1]page.y+u[t-1].h-(t==${screenshots.size - 1}?${extraHeight.toInt()}:0)]", "-layers", "merge", "+repage", mergedScreenshot.absolutePath)
        )
            .start()

        var time = 0
        while (time < 60) {
            if (convertProcess.isAlive) {
                delay(1000)
                time++
            } else if (convertProcess.exitValue() == 0) {
                val fullKey = (1..16).map { SecureRandom().nextInt(keyPool.size) }.map(keyPool::get).joinToString("") // TODO: use actual hash of image
                val fullFileName = "${date}_screenshot_full_${fullKey}.png"
                val fullFile = File(archiveFolder, fullFileName)
                Files.copy(mergedScreenshot.toPath(), fullFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                db.archiveQueries.update(Archive.Status.COMPLETED, "$archivePath/$fullFileName", fullFile.length().toString(), screenshotFullArchiveId)
                log.debug("Completed full screenshot archive")
                mergedScreenshot.delete()
                screenshots.forEach { it.delete() }
                return
            } else {
                log.error("Could not generate screenshot: exit code ${convertProcess.exitValue()}")
                mergedScreenshot.delete()
                screenshots.forEach { it.delete() }
                return
            }
        }

        log.error("Timeout waiting for imagemagick, killing process")
        convertProcess.destroyForcibly()
        mergedScreenshot.delete()
        screenshots.forEach { it.delete() }
    }

    private suspend fun savePdf() {
        val htmlArchive = db.archiveQueries.findById(monolithArchiveId).executeAsOneOrNull()
            ?: db.archiveQueries.findById(originalArchiveId).executeAsOneOrNull()
            ?: return

        log.debug("Starting pdf archive")

        val port = application.environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull() ?: 8080
        val pdfFile = File.createTempFile("pdf", ".pdf")
        val chromeProcess = ProcessBuilder(
            listOf(chromeBin)
                + chromeArgs
                + listOf("--print-to-pdf=${pdfFile.absolutePath}", "http://localhost:$port/api/archives/${htmlArchive.key}")
        ).start()

        var time = 0
        while (time < 30) {
            if (chromeProcess.isAlive) {
                delay(1000)
                time++
            } else if (chromeProcess.exitValue() == 0) {
                val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                val key = (1..16).map { SecureRandom().nextInt(keyPool.size) }.map(keyPool::get).joinToString("") // TODO: use actual hash of pdf
                val fileName = "${date}_pdf_${key}.pdf"
                val file = File(archiveFolder, fileName)
                Files.copy(pdfFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                db.archiveQueries.update(Archive.Status.COMPLETED, "$archivePath/$fileName", file.length().toString(), pdfArchiveId)
                log.debug("Completed pdf archive")
                pdfFile.delete()
                return
            } else {
                log.error("Could not generate pdf: exit code ${chromeProcess.exitValue()}")
                pdfFile.delete()
                return
            }
        }

        log.error("Timeout waiting for chrome, killing process")
        pdfFile.delete()
        chromeProcess.destroyForcibly()
    }

    private suspend fun saveFavicon() {
        log.debug("Starting favicon download")
        val icons = (driver as WebDriver).findElements(By.xpath("//link")).mapNotNull { el ->
            val rel = Relationship.values().firstOrNull { it.value == el.getAttribute("rel") } ?: return@mapNotNull null
            if (rel == Relationship.MANIFEST) return@mapNotNull null
            val href = el.getAttribute("href") ?: ""
            if (href.isEmpty()) return@mapNotNull null
            val url = URL(driver.currentUrl).let {
                if (href.startsWith("//")) {
                    it.protocol + ":" + href
                } else {
                    URL(it, href).toString()
                }
            }
            val sizes = el.getAttribute("sizes") ?: ""
            val mimeType = el.getAttribute("type") ?: ""
            PageIcon(rel, url, sizes, mimeType)
        }.sortedWith(compareBy<PageIcon> { it.rel.priority }.thenByDescending { it.inferSize().height }).let {
            val url = URL(driver.currentUrl)
            it + PageIcon(Relationship.SHORTCUT_ICON, "${url.protocol}://${url.host}/favicon.ico", "", "")
        }

        icons.forEach { log.debug("Icon: $it") }

        if (icons.isEmpty()) {
            log.error("No icons found")
            return
        }

        HttpClient(CIO) { followRedirects = true }.use { client ->
            for (icon in icons) {
                log.debug("Trying to download $icon")
                val response = runCatching { client.get<KtorHttpResponse>(icon.url) }
                    .getOrNull() ?: continue

                if (response.status.isSuccess()) {
                    val ext = Paths.get(URL(icon.url).path).fileName.toString().substringAfterLast(".").takeIf { it.length <= 5 } ?: "unknown"
                    val downloadTmp = File.createTempFile("favicon", "input.$ext")
                    response.content.copyAndClose(downloadTmp.writeChannel())

                    val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    val key = (1..16).map { SecureRandom().nextInt(keyPool.size) }.map(keyPool::get).joinToString("") // TODO: use actual hash of image
                    val fileName = "${date}_favicon_${key}.png"
                    val file = File(archiveFolder, fileName)

                    log.debug("Converting icon: ${downloadTmp.absolutePath} -> ${file.absolutePath}")
                    val convertProcess = ProcessBuilder(listOf("convert", "${downloadTmp.absolutePath}[0]", file.absolutePath))
                        .inheritIO()
                        .start()

                    var time = 0
                    while (time < 300) {
                        if (convertProcess.isAlive) {
                            delay(100)
                            time++
                        } else if (convertProcess.exitValue() == 0) {
                            log.debug("Completed icon conversion")
                            db.archiveQueries.update(Archive.Status.COMPLETED, "$archivePath/$fileName", file.length().toString(), faviconArchiveId)
                            db.bookmarkQueries.updateIcon(faviconArchiveId, bookmarkId)
                            downloadTmp.delete()
                            return
                        } else {
                            log.error("Could not convert icon: exit code ${convertProcess.exitValue()}")
                            downloadTmp.delete()
                            break
                        }
                    }
                }
            }
        }

        log.error("Could not download icon")
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
