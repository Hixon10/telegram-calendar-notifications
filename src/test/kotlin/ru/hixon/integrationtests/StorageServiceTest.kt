package ru.hixon.integrationtests

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.hixon.model.CalendarEntity
import ru.hixon.model.CalendarEvent
import ru.hixon.service.StorageService
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest
class StorageServiceTest {

    @Inject
    lateinit var storageService: StorageService

    @Test
    fun testUpsertCalendarEvents() {
        val startEventDate = ZonedDateTime.of(2020, 10, 22, 17, 0, 0, 0, ZoneOffset.UTC)
        val firstElement = CalendarEvent(startEventDate.minusMinutes(15), startEventDate, startEventDate.plusHours(1), "summary", "description", 42, UUID.randomUUID().toString())

        storageService.upsertCalendarEvents(listOf(
                firstElement,
                firstElement
        ))

        storageService.upsertCalendarEvents(listOf(
                firstElement,
        ))

        Assertions.assertTrue(storageService.findCalendarEventsForNotification(firstElement.dateNotification.minusMinutes(1)).none { it.uid == firstElement.uid })

        val foundFirstElement = storageService.findCalendarEventsForNotification(firstElement.dateNotification.plusMinutes(1)).filter { it.uid == firstElement.uid }
        Assertions.assertEquals(1, foundFirstElement.size)

        Assertions.assertEquals(firstElement.uid, foundFirstElement.get(0).uid)
        Assertions.assertEquals(firstElement.telegramChatId, foundFirstElement.get(0).telegramChatId)
        Assertions.assertEquals(firstElement.dateNotification, foundFirstElement.get(0).dateNotification)
        Assertions.assertEquals(firstElement.dateStart, foundFirstElement.get(0).dateStart)
        Assertions.assertEquals(firstElement.dateEnd, foundFirstElement.get(0).dateEnd)
        Assertions.assertEquals(firstElement.description, foundFirstElement.get(0).description)
        Assertions.assertEquals(firstElement.summary, foundFirstElement.get(0).summary)

        storageService.deleteCalendarEvent(firstElement)
        Assertions.assertTrue(storageService.findCalendarEventsForNotification(firstElement.dateNotification.plusMinutes(1)).none { it.uid == firstElement.uid })
    }

    @Test
    fun testIcsUrlCollection() {
        val first = CalendarEntity(UUID.randomUUID().toString(), UUID.randomUUID().toString().hashCode().toLong(), UUID.randomUUID().toString().hashCode().toLong())
        val second = CalendarEntity(UUID.randomUUID().toString(), UUID.randomUUID().toString().hashCode().toLong(), UUID.randomUUID().toString().hashCode().toLong())

        storageService.upsertIcsCalendar(first)
        var allCalendars = storageService.getAllCalendars()
        Assertions.assertTrue(allCalendars.any { c -> c.icsUrl == first.icsUrl })
        Assertions.assertTrue(allCalendars.any { c -> c.telegramChatId == first.telegramChatId })
        Assertions.assertTrue(allCalendars.any { c -> c.notifyBeforeInMinutes == first.notifyBeforeInMinutes })

        storageService.upsertIcsCalendar(second)
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
