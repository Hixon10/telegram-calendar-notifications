package ru.hixon.service

import io.micronaut.context.annotation.Context
import org.slf4j.LoggerFactory
import ru.hixon.ics.IcsHttpClient
import ru.hixon.ics.IcsParser
import ru.hixon.model.CalendarEntity
import ru.hixon.model.CalendarEvent
import ru.hixon.telegram.TelegramConfiguration
import java.time.Duration
import javax.annotation.PostConstruct

@Context
public class UserCalendarUpdater(
        private val telegramConfiguration: TelegramConfiguration,
        private val storageService: StorageService,
        private val icsHttpClient: IcsHttpClient,
        private val icsParser: IcsParser
) {

    private val logger = LoggerFactory.getLogger(UserCalendarUpdater::class.java)

    private val updateInterval = Duration.ofMinutes(30).toMillis()

    private lateinit var calendarUpdaterThread: Thread

    @PostConstruct
    public fun init() {
        if (!telegramConfiguration.needPoolUpdates!!) {
            // we do not want to send notifications in test
            return
        }

        calendarUpdaterThread = Thread({
            while (!Thread.currentThread().isInterrupted) {
                try {
                    updateCalendars()
                    Thread.sleep(updateInterval)
                } catch (th: Throwable) {
                    when (th) {
                        is InterruptedException -> {
                            logger.info("${Thread.currentThread().name} thread were interrupted")
                            return@Thread
                        }
                        else -> logger.error("Receive error in updateCalendars loop", th)
                    }
                }
            }
        }, "calendar-updater")
        calendarUpdaterThread.start()
    }

    public fun updateCalendars() {
        val calendars = storageService.getAllCalendars()
        for (calendar in calendars) {
            try {
                updateCalendar(calendar)
            } catch (th: Throwable) {
                logger.error("Cannot update specific calendar: telegramChatId={}", calendar.telegramChatId, th)
            }
        }
    }

    private fun updateCalendar(calendar: CalendarEntity) {
        val icsContent = icsHttpClient.downloadIcs(calendar.icsUrl)
        if (icsContent == null || icsContent.isNullOrEmpty()) {
            logger.info("updateCalendar(): calendar content is empty")
            return
        }

        val parsedCalendarEvents: List<CalendarEvent> = icsParser.parse(icsContent.byteInputStream(), Duration.ofMinutes(calendar.notifyBeforeInMinutes), calendar.telegramChatId)
        if (parsedCalendarEvents.isEmpty()) {
            logger.info("updateCalendar(): parsed calendar events are empty")
            return
        }

        storageService.upsertCalendarEvents(parsedCalendarEvents)
    }
}
