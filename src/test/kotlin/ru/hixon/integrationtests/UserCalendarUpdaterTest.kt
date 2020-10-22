package ru.hixon.integrationtests

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import ru.hixon.model.CalendarEntity
import ru.hixon.service.NotificationService
import ru.hixon.service.StorageService
import ru.hixon.service.UserCalendarUpdater
import java.time.Duration
import javax.inject.Inject

@MicronautTest
class UserCalendarUpdaterTest {

    @Inject
    lateinit var userCalendarUpdater: UserCalendarUpdater

    @Inject
    lateinit var storageService: StorageService

    @Inject
    lateinit var notificationService: NotificationService

    @Test
    fun testUpdateCalendars() {
        val icsUrl = "https://www.example.com/ics"
        val chatId: Long = 42

        val calendar = CalendarEntity(icsUrl, Duration.ofHours(24).toMinutes(), chatId)
        storageService.upsertIcsCalendar(calendar)

        userCalendarUpdater.updateCalendars()

        notificationService.sendNotifications()
    }
}
