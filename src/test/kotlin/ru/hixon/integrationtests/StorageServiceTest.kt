package ru.hixon.integrationtests

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.hixon.model.CalendarEntity
import ru.hixon.service.StorageService
import java.util.*
import javax.inject.Inject

@MicronautTest
class StorageServiceTest {

    @Inject
    lateinit var storageService: StorageService

    @Test
    fun testIcsUrlCollection() {
        val first = CalendarEntity(UUID.randomUUID().toString(), UUID.randomUUID().toString().hashCode().toLong(), UUID.randomUUID().toString().hashCode().toLong())
        val second = CalendarEntity(UUID.randomUUID().toString(), UUID.randomUUID().toString().hashCode().toLong(), UUID.randomUUID().toString().hashCode().toLong())

        storageService.saveIcsCalendar(first)
        var allCalendars = storageService.getAllCalendars()
        Assertions.assertTrue(allCalendars.any { c -> c.icsUrl == first.icsUrl })
        Assertions.assertTrue(allCalendars.any { c -> c.telegramChatId == first.telegramChatId })
        Assertions.assertTrue(allCalendars.any { c -> c.notifyBeforeInMinutes == first.notifyBeforeInMinutes })

        storageService.saveIcsCalendar(second)
        allCalendars = storageService.getAllCalendars()
        Assertions.assertTrue(allCalendars.any { c -> c.telegramChatId == first.telegramChatId })
        Assertions.assertTrue(allCalendars.any { c -> c.telegramChatId == second.telegramChatId })

        storageService.deleteIcsCalendarsByChatId(first.telegramChatId)
        allCalendars = storageService.getAllCalendars()
        Assertions.assertTrue(allCalendars.none { c -> c.telegramChatId == first.telegramChatId })
        Assertions.assertTrue(allCalendars.any { c -> c.telegramChatId == second.telegramChatId })

        storageService.deleteIcsCalendarsByChatId(first.telegramChatId)

        storageService.deleteIcsCalendarsByChatId(second.telegramChatId)
        allCalendars = storageService.getAllCalendars()
        Assertions.assertTrue(allCalendars.none { c -> c.telegramChatId == first.telegramChatId })
        Assertions.assertTrue(allCalendars.none { c -> c.telegramChatId == second.telegramChatId })
    }

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

        if (initialTgOffset != null) {
            storageService.saveTelegramOffset(initialTgOffset)
            Assertions.assertEquals(initialTgOffset, storageService.getTelegramOffset())
        }
    }
}
