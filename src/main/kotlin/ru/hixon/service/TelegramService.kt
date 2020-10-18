package ru.hixon.service

import io.micronaut.context.annotation.Context
import io.micronaut.core.util.StringUtils
import org.slf4j.LoggerFactory
import ru.hixon.telegram.MessageResponse
import ru.hixon.telegram.TelegramClient
import ru.hixon.telegram.TelegramConfiguration
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct


@Context
public class TelegramService(
        private val telegramClient: TelegramClient,
        private val storageService: StorageService,
        private val telegramConfiguration: TelegramConfiguration
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

        telegramClient.sendMessage(message.chat.id, message.text!!)
    }
}
