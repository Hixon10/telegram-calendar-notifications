package ru.hixon.ics

import biweekly.Biweekly
import biweekly.ICalendar
import biweekly.component.VEvent
import biweekly.io.chain.ChainingTextParser
import biweekly.io.chain.ChainingTextStringParser
import org.slf4j.LoggerFactory
import ru.hixon.model.CalendarEvent
import java.io.InputStream
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Singleton


@Singleton
public class IcsParser {

    private val logger = LoggerFactory.getLogger(IcsParser::class.java)

    public fun parse(icsContent: InputStream, notifyBefore: Duration, telegramChatId: Long): List<CalendarEvent> {
        val parseResult: ChainingTextParser<ChainingTextParser<*>>

        try {
            parseResult = Biweekly.parse(icsContent)
        } catch (e: Exception) {
            logger.error("Cannot parse calendar", e)
            return emptyList()
        }

        try {
            val result = ArrayList<CalendarEvent>()

            val ical: ICalendar? = parseResult.first()
            if (ical == null) {
                logger.info("Empty result for parsing")
                return emptyList()
            }

            for (event: VEvent? in ical.events) {
                val startTimeStamp: Long? = event?.dateStart?.value?.time
                val endTimeStamp: Long? = event?.dateEnd?.value?.time
                if (startTimeStamp == null) {
                    logger.info("Cannot build calendar event with null start time")
                    continue
                }
                result.add(CalendarEvent(
                        ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTimeStamp), ZoneOffset.UTC).minus(notifyBefore),
                        ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTimeStamp), ZoneOffset.UTC),
                        if (endTimeStamp != null) ZonedDateTime.ofInstant(Instant.ofEpochMilli(endTimeStamp), ZoneOffset.UTC) else null,
                        event.summary?.value,
                        event.description?.value,
                        telegramChatId
                ))
            }

            return result
        } catch (e: Exception) {
            logger.error("Cannot build calendar events", e)
            return emptyList()
        }
    }
}
