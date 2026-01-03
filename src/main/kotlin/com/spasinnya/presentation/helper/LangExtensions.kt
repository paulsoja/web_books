package com.spasinnya.presentation.helper

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey


val LanguageKey = AttributeKey<String>("language")

fun ApplicationCall.language(): String =
    attributes.getOrNull(LanguageKey) ?: "en"