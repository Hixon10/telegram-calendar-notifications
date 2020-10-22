package ru.hixon.service

import io.micronaut.context.annotation.Context
import org.slf4j.LoggerFactory
import ru.hixon.telegram.TelegramClient
import ru.hixon.telegram.TelegramConfiguration
import javax.annotation.PostConstruct


@Context
public class NotificationService(
        private val telegramConfiguration: TelegramConfiguration,
        private val storageService: StorageService,
        private val telegramClient: TelegramClient
) {

    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    private lateinit var notificationThread: Thread

    @PostConstruct
    public fun init() {
        if (!telegramConfiguration.needPoolUpdates!!) {
            // we do not want to send notifications in test
            return
        }

        notificationThread = Thread({
            while (!Thread.currentThread().isInterrupted) {
                try {
                    sendNotifications()
                    Thread.sleep(30_000)
                } catch (th: Throwable) {
                    when (th) {
                        is InterruptedException -> {
                            logger.info("${Thread.currentThread().name} thread were interrupted")
                            return@Thread
                        }
                        else -> logger.error("Receive error in sendNotifications loop", th)
                    }
                }
            }
        }, "notification-service")
        notificationThread.start()
    }

    public fun sendNotifications() {
        val calendarEventsForNotification = storageService.findCalendarEventsForNotification()
        for (calendarEvent in calendarEventsForNotification) {
            val messageText = "${calendarEvent.summary} \r\n${calendarEvent.description} \r\nfrom: ${calendarEvent.dateStart} \r\nto: ${calendarEvent.dateEnd}"

            try {
                telegramClient.sendMessage(calendarEvent.telegramChatId, messageText.subSequence(0, Math.min(4000, messageText.length)).toString())
            } catch (th: Throwable) {
                logger.error("sendNotifications(): cannot send notification to user: {}", calendarEvent.telegramChatId)

                // retry with fallback text
                try {
                    telegramClient.sendMessage(calendarEvent.telegramChatId, "You will have a meeting soon")
                } catch (th: Throwable) {
                    logger.info("sendNotifications(): cannot retry sending notification to user: {}", calendarEvent.telegramChatId)
                }
            }

            storageService.deleteCalendarEvent(calendarEvent)
        }
    }
}
