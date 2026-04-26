package ru.hixon.ics

import io.micronaut.http.client.HttpClient
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
public class IcsHttpClient(
        private val httpClient: HttpClient
) {
    private val logger = LoggerFactory.getLogger(IcsHttpClient::class.java)

    public fun downloadIcs(icsUrl: String): String? {
        try {
            return httpClient.toBlocking().retrieve(icsUrl)
        } catch (th: Throwable) {
            logger.error("downloadIcs(): some error occur: url={}", icsUrl, th)
            return null
        }
    }
}
