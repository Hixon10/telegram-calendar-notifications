package ru.hixon.service

import io.micronaut.context.annotation.Context
import io.micronaut.core.util.StringUtils
import org.slf4j.LoggerFactory
import ru.hixon.ics.IcsHttpClient
import ru.hixon.ics.IcsParser
import ru.hixon.model.CalendarEntity
import ru.hixon.model.CalendarEvent
import ru.hixon.telegram.MessageResponse
import ru.hixon.telegram.TelegramClient
import ru.hixon.telegram.TelegramConfiguration
import java.lang.StringBuilder
import java.net.URI
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct


@Context
public class TelegramService(
        private val telegramClient: TelegramClient,
        private val storageService: StorageService,
        private val telegramConfiguration: TelegramConfiguration,
        private val icsHttpClient: IcsHttpClient,
        private val icsParser: IcsParser
) {

    private val logger = LoggerFactory.getLogger(TelegramService::class.java)

    private val telegramOffsetCache = AtomicLong(0)

    private lateinit var getTelegramUpdatesThread: Thread

    @PostConstruct
    public fun init() {
        val healthCheck = telegramClient.getMe()
        if (healthCheck == null || !healthCheck.ok) {
            logger.error("Cannot connect to telegram: healthCheck={}", healthCheck)
            return
        }

        if (!telegramConfiguration.needPoolUpdates!!) {
            // we do not want to pool updates in test
            return
        }

        getTelegramUpdatesThread = Thread({
            while (!Thread.currentThread().isInterrupted) {
                try {
                    getTelegramUpdate()
                    Thread.sleep(3000)
                } catch (th: Throwable) {
                    when (th) {
                        is InterruptedException -> {
                            logger.info("${Thread.currentThread().name} thread were interrupted")
                            return@Thread
                        }
                        else -> logger.error("Receive error in GetUpdates loop", th)
                    }
                }
            }
        }, "get-telegram-updates")
        getTelegramUpdatesThread.start()
    }

    private fun getTelegramUpdate() {
        val offset: Long = getOffset()
        val updates = telegramClient.getUpdates(offset)
        logger.info("getTelegramUpdate(): offset={}, updates={}", offset, updates)

        if (updates == null || !updates.ok || updates.result.isEmpty()) {
            return
        }

        for (updatesItem in updates.result) {
            if (updatesItem.message != null) {
                try {
                    processNewMessage(updatesItem.message!!)
                } catch (e: Exception) {
                    logger.error("getTelegramUpdate(): get error for message: {}", updatesItem.message!!, e)
                }
            }
        }

        val maxUpdateId = updates.result.maxByOrNull { element -> element.update_id }!!.update_id
        updateOffset(maxUpdateId, offset)
    }

    private fun getOffset(): Long {
        val cacheOffset = telegramOffsetCache.get()
        if (cacheOffset > 0) {
            return cacheOffset
        }

        return storageService.getTelegramOffset() ?: 0L
    }

    private fun updateOffset(maxUpdateId: Long, currentOffset: Long) {
        val newOffsetValue = maxUpdateId + 1
        if (newOffsetValue > currentOffset) {
            telegramOffsetCache.set(newOffsetValue)
            storageService.saveTelegramOffset(newOffsetValue)
        }
    }

    private fun processNewMessage(message: MessageResponse) {
        if (StringUtils.isEmpty(message.text)) {
            return
        }

        when (message.text) {
            "/help" -> {
                val text = "You need to send ics URL and before notification minutes to the bot. For example:\r\n" +
                        "https://example.ru/3242352/file.ics 15"
                telegramClient.sendMessage(message.chat.id, text)
            }
            "/calendars" -> sendMyCalendars(message)
            "/stop" -> {
                storageService.deleteIcsCalendarsByChatId(message.chat.id)
                storageService.deleteCalendarEvents(message.chat.id)
                telegramClient.sendMessage(message.chat.id, "Your calendars were deleted")
            }
            "/about" -> telegramClient.sendMessage(message.chat.id, "https://github.com/Hixon10/telegram-calendar-notifications")
            else -> processAddCalendarMessage(message)
        }
    }

    private fun sendMyCalendars(message: MessageResponse) {
        val icsCalendars = storageService.getIcsCalendarsByChatId(message.chat.id)
        val text: String = if (icsCalendars.isEmpty()) {
            "You have no subscribed calendars"
        } else {
            val sb = StringBuilder()
            for (icsCalendar in icsCalendars) {
                sb.append("${icsCalendar.icsUrl} ${icsCalendar.notifyBeforeInMinutes}\r\n")
            }
            sb.subSequence(0, Math.min(4000, sb.length)).toString()
        }

        telegramClient.sendMessage(message.chat.id, text)
    }

    private fun processAddCalendarMessage(message: MessageResponse) {
        val messageParts: List<String> = message.text!!.split("\\s+".toRegex())
        if (messageParts.size != 2) {
            telegramClient.sendMessage(message.chat.id, "Your message has wrong format: ${message.text}")
            return
        }

        val notifyBeforeInMinutes: Long? = messageParts.get(1).toLongOrNull()
        if (notifyBeforeInMinutes == null || notifyBeforeInMinutes < 0) {
            telegramClient.sendMessage(message.chat.id, "Your message has wrong notification time: ${message.text}")
            return
        }

        try {
            URI(messageParts.get(0)).toURL()
        } catch (e: Throwable) {
            telegramClient.sendMessage(message.chat.id, "Your message has wrong URL: ${message.text}")
            return
        }

        val icsContent = icsHttpClient.downloadIcs(messageParts.get(0))
        if (icsContent == null || icsContent.isNullOrEmpty()) {
            telegramClient.sendMessage(message.chat.id, "Cannot download your calendar")
            return
        }

        val parsedCalendarEvents: List<CalendarEvent> = icsParser.parse(icsContent.byteInputStream(), Duration.ofMinutes(notifyBeforeInMinutes), message.chat.id)
        if (parsedCalendarEvents.isEmpty()) {
            telegramClient.sendMessage(message.chat.id, "Cannot find any events in your calendar")
            return
        }

        storageService.upsertIcsCalendar(CalendarEntity(messageParts.get(0), notifyBeforeInMinutes, message.chat.id))

        storageService.upsertCalendarEvents(parsedCalendarEvents)

        telegramClient.sendMessage(message.chat.id, "Your calendar is added")
    }
}
