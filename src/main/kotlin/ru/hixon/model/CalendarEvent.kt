package ru.hixon.model

import java.time.ZonedDateTime

public data class CalendarEvent(
        public val dateNotification: ZonedDateTime, // a moment, when we have to send notification
        public val dateStart: ZonedDateTime,
        public val dateEnd: ZonedDateTime?,
        public val summary: String?,
        public val description: String?,
        public val telegramChatId: Long, // an id of telegram chat, where we need to send notification
        public val uid: String
)
