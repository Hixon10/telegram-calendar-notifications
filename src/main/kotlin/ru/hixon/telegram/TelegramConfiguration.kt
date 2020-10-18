package ru.hixon.telegram

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires
import kotlin.properties.Delegates

@ConfigurationProperties(TelegramConfiguration.PREFIX)
@Requires(property = TelegramConfiguration.PREFIX)
public class TelegramConfiguration {
    public lateinit var token: String
    public var needPoolUpdates: Boolean? = null

    public companion object {
        public const val PREFIX: String = "telegram"
        public const val TELEGRAM_API_URL: String = "https://api.telegram.org"
    }
}
