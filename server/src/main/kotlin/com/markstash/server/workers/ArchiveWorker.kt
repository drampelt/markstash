package com.markstash.server.workers

import com.markstash.api.models.Archive
import net.dankito.readability4j.Readability4J
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory

class ArchiveWorker(
    private val userId: Long,
    private val bookmarkId: Long
) : Worker() {
    private val log = LoggerFactory.getLogger(ArchiveWorker::class.java)

    override suspend fun run() {
        val bookmark = db.bookmarkQueries.findById(userId, bookmarkId).executeAsOne()

        val plainArchiveId = db.transactionWithResult<Long> {
            db.archiveQueries.insert(bookmarkId, Archive.Type.PLAIN, Archive.Status.PROCESSING, null, null)
            db.archiveQueries.lastInsert().executeAsOne()
        }

        val readabilityArchiveId = db.transactionWithResult<Long> {
            db.archiveQueries.insert(bookmarkId, Archive.Type.READABILITY, Archive.Status.PROCESSING, null, null)
            db.archiveQueries.lastInsert().executeAsOne()
        }

        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver")
        val driver = ChromeDriver(ChromeOptions().addArguments("--headless", "--disable-gpu", "--window-size=1920,1080"))
        driver.get(bookmark.url)

        val wait = WebDriverWait(driver, 30)
        wait.until { driver.executeScript("return document.readyState") == "complete" }

        val html = driver.findElementByXPath("//*").getAttribute("outerHTML")
        val article = Readability4J(driver.currentUrl, html).parse()
        log.debug("Plain: ${article.textContent}")
        log.debug("Title: ${article.title}")
        log.debug("Byline: ${article.byline}")
        log.debug("Excerpt: ${article.excerpt}")

        db.archiveQueries.update(Archive.Status.COMPLETED, null, article.textContent, plainArchiveId)
        db.archiveQueries.update(Archive.Status.COMPLETED, null, article.content, readabilityArchiveId)
    }
}
