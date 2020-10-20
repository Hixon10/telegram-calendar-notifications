package ru.hixon.model

public data class CalendarEntity(
        public val icsUrl: String,
        public val notifyBeforeInMinutes: Long,
        public val telegramChatId: Long
)
