package ru.hixon.integrationtests
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.hixon.service.StorageService
import ru.hixon.telegram.TelegramClient
import javax.inject.Inject

@MicronautTest
class TelegramClientTest {

    @Inject
    lateinit var telegramClient: TelegramClient

    @Inject
    lateinit var storageService: StorageService

    @Test
    fun testGetMet() {
        val response = telegramClient.getMe()

        Assertions.assertNotNull(response)
        Assertions.assertTrue(response!!.ok, response.toString())
    }

    @Test
    fun testSendMessage() {
        val chatId: Long = 42
        val response = telegramClient.sendMessage(chatId, "message from kotlin test")

        Assertions.assertNotNull(response)
        Assertions.assertTrue(response!!.ok, response.toString())
    }

    @Test
    fun testGetUpdates() {
        val response = telegramClient.getUpdates(storageService.getTelegramOffset() ?: 0L)

        Assertions.assertNotNull(response)
        Assertions.assertTrue(response!!.ok, response.toString())
    }

}
