package com.markstash.server.workers

import com.google.common.hash.Hashing
import com.markstash.api.models.Archive
import com.markstash.server.db.BookmarkWithTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.dankito.readability4j.Readability4J
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ArchiveWorker(
    private val userId: Long,
    private val bookmarkId: Long
) : Worker() {
    private val log = LoggerFactory.getLogger(ArchiveWorker::class.java)

    private lateinit var bookmark: BookmarkWithTags
    private lateinit var driver: ChromeDriver

    private var originalArchiveId: Long = 0
    private var plainArchiveId: Long = 0
    private var readabilityArchiveId: Long = 0
    private var monolithArchiveId: Long = 0
    private var monolithReadabilityArchiveId: Long = 0

    private val tmpDir by lazy { Files.createTempDirectory("tmp") }

    private val archivePath by lazy {
        val url = URL(bookmark.url)
        val query = url.query?.replace("&", "_")?.replace("=", "-")
        var path = url.path.substring(1)
        if (path.isBlank()) path = "index"
        var archivePrefix = "${url.host}/$path"
        if (query != null) archivePrefix += "_$query"
        if (archivePrefix.length > 500) archivePrefix = archivePrefix.substring(0, 499)
        archivePrefix
    }

    private val rootFolder by lazy { File("archives").also { it.mkdirs() } }
    private val archiveFolder by lazy { File(rootFolder, archivePath).also { it.mkdirs() } }

    override suspend fun run() = withContext(Dispatchers.IO) {
        bookmark = db.bookmarkQueries.findById(userId, bookmarkId).executeAsOne()

        log.debug("Starting archive of bookmark $bookmarkId to $archivePath")

        originalArchiveId = createArchive(Archive.Type.ORIGINAL)
        plainArchiveId = createArchive(Archive.Type.PLAIN)
        readabilityArchiveId = createArchive(Archive.Type.READABILITY)
        monolithArchiveId = createArchive(Archive.Type.MONOLITH)
        monolithReadabilityArchiveId = createArchive(Archive.Type.MONOLITH_READABILITY)

        setupDriver()
        loadPage()

        // Start the download before saving basic archives to remove the download buttons from the page
        log.debug("Starting monolith download")
        driver.executeScript("document.getElementById('__monolith_download').click()")

        saveBasic()
        saveMonolith()

        driver.close()
        log.debug("Completed archive of bookmark $bookmarkId!")
    }

    private fun setupDriver() {
        log.debug("Creating webdriver")
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver")
        // Ideally would use --headless here but extensions don't work in headless mode :(
        // See https://stackoverflow.com/questions/45372066/is-it-possible-to-run-google-chrome-in-headless-mode-with-extensions
        val options = ChromeOptions().addArguments("--disable-gpu", "--window-size=1920,1080")
            .addExtensions(File("src/main/resources/extensions/monolith.crx"))
            .setExperimentalOption("prefs", mapOf(
                "profile.default_content_settings.popups" to 0,
                "download.default_directory" to tmpDir.toAbsolutePath().toString()
            ))
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
            driver.executeScript("window.scrollBy(0, 250)")
            scrolled += 250
            delay(100)
        }

        delay(500)
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
        log.debug("Waiting for monolith download")
        var times = 0
        while (true) {
            if (times >= 60) break

            val files = tmpDir.toFile().listFiles { _, name -> name.endsWith(".html") } ?: emptyArray()
            val htmlFile = files.firstOrNull()
            if (htmlFile == null) {
                delay(500)
            } else {
                log.debug("Saving monolith archives")
                val html = htmlFile.readText()
                saveArchive(monolithArchiveId, Archive.Type.MONOLITH, html)

                val article = Readability4J(driver.currentUrl, html).parse()
                saveArchive(monolithReadabilityArchiveId, Archive.Type.MONOLITH_READABILITY, article.content)

                break
            }
            times++
        }
    }

    private fun createArchive(type: Archive.Type): Long = db.transactionWithResult {
        db.archiveQueries.insert(bookmarkId, type, Archive.Status.PROCESSING, null, null)
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
                db.archiveQueries.update(status, "$archivePath/$fileName", null, id)
            }
        }
    }
}
