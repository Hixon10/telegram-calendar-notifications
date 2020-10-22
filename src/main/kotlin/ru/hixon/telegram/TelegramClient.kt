package ru.hixon.telegram

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single

@Client(TelegramConfiguration.TELEGRAM_API_URL + "/bot\${telegram.token}")
public interface TelegramClient {

    @Get("/getMe")
    public fun getMe(): OkResultResponse?

    @Post("/sendMessage")
    public fun sendMessage(
            chat_id: Long,
            text: String
    ): OkResultResponse?

    @Post("/getUpdates")
    public fun getUpdates(
            offset: Long,
            limit: Int = 100,
            timeout: Int = 5,
            allowed_updates: String = "[\"message\"]"
    ): GetUpdatesResponse?
}
