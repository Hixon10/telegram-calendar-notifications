package ru.hixon.integrationtests

import io.micronaut.core.util.StringUtils
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.hixon.ics.IcsHttpClient
import ru.hixon.ics.IcsParser
import java.time.Duration
import javax.inject.Inject

@MicronautTest
class IcsHttpClientTest {

    @Inject
    lateinit var icsHttpClient: IcsHttpClient

    @Inject
    lateinit var icsParser: IcsParser

    @Test
    fun testDownloadIcs() {
        val icsUrl = "https://www.italki.com/calendar/42/ics"
        val icsContent = icsHttpClient.downloadIcs(icsUrl)

        Assertions.assertTrue(StringUtils.isNotEmpty(icsContent))
        Assertions.assertTrue(icsContent!!.contains("SUMMARY:italki Lesson"))

        val parsedIcs = icsParser.parse(icsContent.byteInputStream(), Duration.ofMinutes(15), 435)
        Assertions.assertTrue(parsedIcs.isNotEmpty())
    }
}
