package ru.hixon

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.hixon.ics.IcsParser
import java.time.Duration
import javax.inject.Inject

@MicronautTest
class IcsParserTest {

    @Inject
    lateinit var icsParser: IcsParser

    @Test
    fun testParse() {
        val icsStream = javaClass.getResourceAsStream("italki.ics")
        val telegramChatId = 42342L
        val notifyBefore = Duration.ofMinutes(15)
        val calendarEvents = icsParser.parse(icsStream, notifyBefore, telegramChatId)

        Assertions.assertEquals(3, calendarEvents.size)

        // event 0
        Assertions.assertEquals(telegramChatId, calendarEvents.get(0).telegramChatId)
        Assertions.assertEquals("ITALKI_SESSION_123", calendarEvents.get(0).uid)
        Assertions.assertEquals("italki Lesson: english - Ivan Ivanov", calendarEvents.get(0).summary)
        Assertions.assertEquals("2020-10-24T16:00Z", calendarEvents.get(0).dateStart.toString())
        Assertions.assertEquals("2020-10-24T17:00Z", calendarEvents.get(0).dateEnd.toString())
        Assertions.assertEquals(calendarEvents.get(0).dateNotification, calendarEvents.get(0).dateStart.minus(notifyBefore))

        Assertions.assertEquals("Session ID: 123\r\n" +
                "Course /Service: Speech Practice\r\n" +
                "Teacher: Ivan Ivanov (https://www.italki.com/teacher/Ivan)\r\n" +
                "Skype: admin@example.com\r\n" +
                "URL: https://www.italki.com/lesson/session/123", calendarEvents.get(0).description)

        // event 1
        Assertions.assertEquals(telegramChatId, calendarEvents.get(1).telegramChatId)
        Assertions.assertEquals("ITALKI_SESSION_82342352", calendarEvents.get(1).uid)
        Assertions.assertEquals("italki Lesson: english - Vadim Ivanov", calendarEvents.get(1).summary)
        Assertions.assertEquals("2020-10-27T11:00Z", calendarEvents.get(1).dateStart.toString())
        Assertions.assertEquals("2020-10-27T12:00Z", calendarEvents.get(1).dateEnd.toString())
        Assertions.assertEquals(calendarEvents.get(1).dateNotification, calendarEvents.get(1).dateStart.minus(notifyBefore))

        // event 2
        Assertions.assertEquals("ITALKI_SESSION_4242424", calendarEvents.get(2).uid)
        Assertions.assertEquals(telegramChatId, calendarEvents.get(2).telegramChatId)
        Assertions.assertEquals("italki Lesson: english - Nikolay Ivanov", calendarEvents.get(2).summary)
        Assertions.assertEquals("2020-10-29T13:00Z", calendarEvents.get(2).dateStart.toString())
        Assertions.assertEquals("2020-10-29T14:00Z", calendarEvents.get(2).dateEnd.toString())
        Assertions.assertEquals(calendarEvents.get(2).dateNotification, calendarEvents.get(2).dateStart.minus(notifyBefore))
    }
}
