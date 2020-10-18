package ru.hixon.integrationtests

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.hixon.service.StorageService
import javax.inject.Inject

@MicronautTest
class StorageServiceTest {

    @Inject
    lateinit var storageService: StorageService

    @Test
    fun testStorageService() {
        val initialTgOffset = storageService.getTelegramOffset()
        Assertions.assertTrue(initialTgOffset == null || initialTgOffset >= 0)

        val newValue: Long = 10
        storageService.saveTelegramOffset(newValue)
        Assertions.assertEquals(newValue, storageService.getTelegramOffset())
        Assertions.assertEquals(newValue, storageService.getTelegramOffset())

        storageService.saveTelegramOffset(newValue)
        Assertions.assertEquals(newValue, storageService.getTelegramOffset())

        val newValue2: Long = 20
        storageService.saveTelegramOffset(newValue2)
        storageService.saveTelegramOffset(newValue2)
        Assertions.assertEquals(newValue2, storageService.getTelegramOffset())
        Assertions.assertEquals(newValue2, storageService.getTelegramOffset())
        storageService.saveTelegramOffset(newValue2)
        Assertions.assertEquals(newValue2, storageService.getTelegramOffset())

        storageService.saveTelegramOffset(newValue)
        Assertions.assertEquals(newValue, storageService.getTelegramOffset())
    }
}
