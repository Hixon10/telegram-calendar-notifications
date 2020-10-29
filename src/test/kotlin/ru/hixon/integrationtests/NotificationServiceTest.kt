package ru.hixon.integrationtests

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.hixon.model.CalendarEvent
import ru.hixon.service.NotificationService
import ru.hixon.service.StorageService
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest
class NotificationServiceTest {

    @Inject
    lateinit var storageService: StorageService

    @Inject
    lateinit var notificationService: NotificationService

    @Test
    fun testSendNotifications() {
        val telegramChatId: Long = 42

        val description = "Session ID: 123\r\n" +
                "Course /Service: Speech Practice\r\n" +
                "Teacher: Ivan Ivanov (https://www.italki.com/teacher/Ivan)\r\n" +
                "Skype: admin@example.com\r\n" +
                "URL: https://www.italki.com/lesson/session/123"

        val startEventDate = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(2)
        val calendarEvent = CalendarEvent(startEventDate.minusMinutes(15), startEventDate, startEventDate.plusHours(1), "italki Lesson: english - Vadim Ivanov", description, telegramChatId, UUID.randomUUID().toString())

        storageService.upsertCalendarEvents(listOf(
                calendarEvent
        ))

        Assertions.assertFalse(storageService.isNotificationSent(calendarEvent))

        notificationService.sendNotifications()

        Assertions.assertTrue(storageService.isNotificationSent(calendarEvent))

        Thread.sleep(300)

        notificationService.sendNotifications()

        // we need to test, that we don't send notification second time
        storageService.upsertCalendarEvents(listOf(
                calendarEvent
        ))

        notificationService.sendNotifications()
    }
}
