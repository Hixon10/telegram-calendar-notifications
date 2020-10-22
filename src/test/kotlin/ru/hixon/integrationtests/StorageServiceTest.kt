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
    fun testUpsertNewAndOldEvents() {
        val telegramChatId: Long = 137

        val oldDate = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(1)
        val oldEvent = CalendarEvent(oldDate.minusMinutes(20), oldDate, oldDate.plusHours(1), "old event", "old event", telegramChatId, UUID.randomUUID().toString())

        val newDate = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1)
        val newEvent = CalendarEvent(newDate.minusMinutes(20), newDate, newDate.plusHours(1), "new event", "new event", telegramChatId, UUID.randomUUID().toString())

        storageService.upsertCalendarEvents(listOf(
                oldEvent,
                newEvent
        ))

        val firstAndSecondEvents = storageService.findCalendarEventsForNotification(newEvent.dateNotification.plusMinutes(1)).filter { it.uid == oldEvent.uid || it.uid == newEvent.uid }
        Assertions.assertEquals(1, firstAndSecondEvents.size)

        Assertions.assertEquals(newEvent.uid, firstAndSecondEvents.firstOrNull { it.uid == newEvent.uid }!!.uid)

        storageService.deleteCalendarEvent(newEvent)
    }

    @Test
    fun testUpsertCalendarEvents1() {
        val telegramChatId: Long = 137
        val newDate = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(2)

        val firstElement = CalendarEvent(newDate.minusMinutes(17), newDate, newDate.plusHours(1), "summary1", "description1", telegramChatId, UUID.randomUUID().toString())

        val secondElement = CalendarEvent(newDate.minusMinutes(18), newDate, newDate.plusHours(1), "summary2", "description2", telegramChatId, UUID.randomUUID().toString())

        val thirdElement = CalendarEvent(newDate.minusMinutes(19), newDate, newDate.plusHours(1), "summary3", "description3", telegramChatId, UUID.randomUUID().toString())

        storageService.upsertCalendarEvents(listOf(
                firstElement,
                secondElement
        ))

        val firstAndSecondEvents = storageService.findCalendarEventsForNotification(firstElement.dateNotification.plusMinutes(1)).filter { it.uid == firstElement.uid || it.uid == secondElement.uid || it.uid == thirdElement.uid }
        Assertions.assertEquals(2, firstAndSecondEvents.size)

        Assertions.assertEquals(firstElement.uid, firstAndSecondEvents.firstOrNull { it.uid == firstElement.uid }!!.uid)
        Assertions.assertEquals(secondElement.uid, firstAndSecondEvents.firstOrNull { it.uid == secondElement.uid }!!.uid)

        storageService.upsertCalendarEvents(listOf(
                firstElement,
                secondElement,
                thirdElement
        ))

        val firstAndSecondAndThirdEvents = storageService.findCalendarEventsForNotification(firstElement.dateNotification.plusMinutes(1)).filter { it.uid == firstElement.uid || it.uid == secondElement.uid || it.uid == thirdElement.uid }
        Assertions.assertEquals(3, firstAndSecondAndThirdEvents.size)

        Assertions.assertEquals(firstElement.uid, firstAndSecondAndThirdEvents.firstOrNull { it.uid == firstElement.uid }!!.uid)
        Assertions.assertEquals(secondElement.uid, firstAndSecondAndThirdEvents.firstOrNull { it.uid == secondElement.uid }!!.uid)
        Assertions.assertEquals(thirdElement.uid, firstAndSecondAndThirdEvents.firstOrNull { it.uid == thirdElement.uid }!!.uid)

        storageService.deleteCalendarEvents(telegramChatId)
    }

    @Test
    fun testUpsertCalendarEvents2() {
        val startEventDate = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(2)
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
        Assertions.assertEquals(firstElement.dateNotification.toEpochSecond(), foundFirstElement.get(0).dateNotification.toEpochSecond())
        Assertions.assertEquals(firstElement.dateStart.toEpochSecond(), foundFirstElement.get(0).dateStart.toEpochSecond())
        Assertions.assertEquals(firstElement.dateEnd!!.toEpochSecond(), foundFirstElement.get(0).dateEnd!!.toEpochSecond())
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
