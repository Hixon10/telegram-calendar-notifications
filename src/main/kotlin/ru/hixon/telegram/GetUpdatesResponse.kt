package ru.hixon.telegram

import io.micronaut.core.annotation.Introspected

@Introspected
public data class GetUpdatesResponse(
        var ok: Boolean,
        var result: List<GetUpdatesItem>
)

@Introspected
public data class GetUpdatesItem(
        var update_id: Long,
        var message: MessageResponse?
)

@Introspected
public data class MessageResponse(
        var message_id: Long,
        var chat: ChatResponse,
        var text: String?,
        var entities: List<EntityItem>?
)

@Introspected
public data class ChatResponse(
        var id: Long
)

@Introspected
public data class EntityItem(
        var type: String
)
