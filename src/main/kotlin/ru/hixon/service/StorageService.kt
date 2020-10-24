package ru.hixon.service

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import org.bson.Document
import org.bson.conversions.Bson
import org.slf4j.LoggerFactory
import ru.hixon.model.CalendarEntity
import ru.hixon.model.CalendarEvent
import ru.hixon.storage.StorageConfiguration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import javax.annotation.PostConstruct
import javax.inject.Singleton
import kotlin.collections.ArrayList


@Singleton
public class StorageService(
        private val mongoClient: MongoClient,
        private val storageConfiguration: StorageConfiguration
) {
    private val logger = LoggerFactory.getLogger(StorageService::class.java)

    private val offsetId = 1L
    private val telegramOffsetFilter = Filters.eq("_id", offsetId)

    private val icsUrlFieldName = "icsUrl"
    private val notifyBeforeInMinutesFieldName = "notifyBeforeInMinutes"
    private val telegramChatIdFieldName = "telegramChatId"

    private lateinit var telegramOffsetCollection: MongoCollection<Document>

    private lateinit var icsUrlCollection: MongoCollection<Document>

    private lateinit var calendarEventCollection: MongoCollection<Document>

    @PostConstruct
    public fun init() {
        logger.info("Storage service started")

        telegramOffsetCollection = mongoClient
                .getDatabase(storageConfiguration.databaseName)
                .getCollection("telegramOffset")

        icsUrlCollection = mongoClient
                .getDatabase(storageConfiguration.databaseName)
                .getCollection("icsUrl")

        calendarEventCollection = mongoClient
                .getDatabase(storageConfiguration.databaseName)
                .getCollection("calendarEvent")
    }

    public fun saveTelegramOffset(offset: Long) {
        val update: Bson = Document("\$set",
                Document()
                        .append("_id", offsetId)
                        .append("offsetValue", offset))

        telegramOffsetCollection
                .updateOne(telegramOffsetFilter, update, UpdateOptions().upsert(true))
    }

    public fun getTelegramOffset(): Long? {
        val result = telegramOffsetCollection.find(telegramOffsetFilter).first()
        if (result == null) {
            return null
        }
        return result.getLong("offsetValue")
    }

    public fun upsertIcsCalendar(calendarEntity: CalendarEntity) {
        val id = "${calendarEntity.telegramChatId}_${calendarEntity.icsUrl}"

        val update: Bson = Document("\$set",
                Document()
                        .append("_id", id)
                        .append(icsUrlFieldName, calendarEntity.icsUrl)
                        .append(notifyBeforeInMinutesFieldName, calendarEntity.notifyBeforeInMinutes)
                        .append(telegramChatIdFieldName, calendarEntity.telegramChatId))

        icsUrlCollection
                .updateOne(Filters.eq("_id", id), update, UpdateOptions().upsert(true))
    }

    public fun deleteIcsCalendarsByChatId(telegramChatId: Long) {
        icsUrlCollection
                .deleteMany(Filters.eq(telegramChatIdFieldName, telegramChatId))
    }

    public fun getIcsCalendarsByChatId(telegramChatId: Long): List<CalendarEntity> {
        val findResult = icsUrlCollection
                .find(Filters.eq(telegramChatIdFieldName, telegramChatId))

        return convertCalendarEntities(findResult)
    }

    public fun getAllCalendars(): List<CalendarEntity> {
        try {
            val limit = 500
            val findResult = icsUrlCollection.find().limit(limit)

            val result = convertCalendarEntities(findResult)
            if (result.size == limit) {
                logger.error("Too many calendars; we need to introduce pagination for it")
            }

            return result
        } catch (th: Throwable) {
            logger.error("getAllCalendars(): some error occurs", th)
            return emptyList()
        }
    }

    public fun upsertCalendarEvents(events: List<CalendarEvent>) {
        val currentDate = ZonedDateTime.now(ZoneOffset.UTC)
        val calendarEvents = events.filter { ce -> ce.dateStart.isAfter(currentDate) }
                .toList()

        if (calendarEvents.isEmpty()) {
            logger.info("There is no events: size={}", calendarEvents.size)
            return
        }

        if (calendarEvents.size > 500) {
            logger.error("Too many calendar events: size={}", calendarEvents.size)
            return
        }

        try {
            val upsertRequest = ArrayList<UpdateOneModel<Document>>()
            for (calendarEvent in calendarEvents) {
                val documentId = buildCalendarEventId(calendarEvent.telegramChatId, calendarEvent.uid)
                val document = Document()
                        .append("_id", documentId)
                        .append("telegramChatId", calendarEvent.telegramChatId)
                        .append("uid", calendarEvent.uid)
                        .append("dateNotification", convertToDate(calendarEvent.dateNotification))
                        .append("dateStart", convertToDate(calendarEvent.dateStart))

                if (calendarEvent.dateEnd != null) {
                    document.append("dateEnd", convertToDate(calendarEvent.dateEnd))
                }

                if (!calendarEvent.description.isNullOrEmpty()) {
                    document.append("description", calendarEvent.description)
                }

                if (!calendarEvent.summary.isNullOrEmpty()) {
                    document.append("summary", calendarEvent.summary)
                }

                val update: Bson = Document("\$set", document)

                upsertRequest.add(
                        UpdateOneModel<Document>(Filters.eq("_id", documentId), update, UpdateOptions().upsert(true))
                )
            }

            calendarEventCollection.bulkWrite(upsertRequest)
        } catch (th: Throwable) {
            logger.error("upsertCalendarEvents(): upsert error occurs", th)
        }
    }

    public fun findCalendarEventsForNotification(currentTime: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)): List<CalendarEvent> {
        try {
            val findResult = calendarEventCollection.find(Filters.lt("dateNotification", convertToDate(currentTime)!!)).limit(100)
            val result = ArrayList<CalendarEvent>()

            for (calendarEventDocument in findResult) {
                result.add(CalendarEvent(
                        convertToZonedDateTime(calendarEventDocument.getDate("dateNotification"))!!,
                        convertToZonedDateTime(calendarEventDocument.getDate("dateStart"))!!,
                        convertToZonedDateTime(calendarEventDocument.getDate("dateEnd")),
                        calendarEventDocument.getString("summary"),
                        calendarEventDocument.getString("description"),
                        calendarEventDocument.getLong("telegramChatId"),
                        calendarEventDocument.getString("uid")
                ))
            }

            return result
        } catch (th: Throwable) {
            logger.error("findCalendarEventsForNotification(): some error occurs", th)
            return emptyList()
        }
    }

    public fun deleteCalendarEvent(calendarEvent: CalendarEvent) {
        try {
            calendarEventCollection
                    .deleteOne(Filters.eq("_id", buildCalendarEventId(calendarEvent.telegramChatId, calendarEvent.uid)))
        } catch (th: Throwable) {
            logger.error("deleteCalendarEvent(): some delete error occur", th)
        }
    }

    public fun deleteCalendarEvents(telegramChatId: Long) {
        calendarEventCollection
                .deleteMany(Filters.eq("telegramChatId", telegramChatId))
    }

    private fun buildCalendarEventId(telegramChatId: Long, uid: String): String {
        return "${telegramChatId}_${uid}"
    }

    private fun convertToZonedDateTime(date: Date?): ZonedDateTime? {
        if (date == null) {
            return null
        }
        return date.toInstant().atZone(ZoneOffset.UTC)
    }

    private fun convertToDate(zonedDateTime: ZonedDateTime?): Date? {
        if (zonedDateTime == null) {
            return null
        }
        return Date.from(zonedDateTime.toInstant())
    }

    private fun convertCalendarEntities(findResult: FindIterable<Document>): List<CalendarEntity> {
        val result = ArrayList<CalendarEntity>()
        for (document in findResult) {
            result.add(CalendarEntity(
                    document.getString(icsUrlFieldName),
                    document.getLong(notifyBeforeInMinutesFieldName),
                    document.getLong(telegramChatIdFieldName)
            ))
        }
        return result
    }
}
