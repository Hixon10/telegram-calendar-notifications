package ru.hixon.telegram

import io.micronaut.core.annotation.Introspected

@Introspected
public data class OkResultResponse(
        var ok: Boolean
)
