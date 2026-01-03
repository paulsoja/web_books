package com.spasinnya.presentation.helper

import io.ktor.server.application.*

class LanguageFromHeadersConfig {
    var headerName: String = "X-Language"
    var defaultLanguage: String = "en"
    var useAcceptLanguageFallback: Boolean = true
}

val LanguageFromHeaders = createRouteScopedPlugin(
    name = "LanguageFromHeaders",
    createConfiguration = ::LanguageFromHeadersConfig
) {
    onCall { call ->
        val cfg = pluginConfig

        val xLang = call.request.headers[cfg.headerName]
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        val acceptLang = if (cfg.useAcceptLanguageFallback) {
            call.request.headers["Accept-Language"]
                ?.split(',')
                ?.firstOrNull()
                ?.split(';')          // убираем q=0.9
                ?.firstOrNull()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        } else null

        val lang = (xLang ?: acceptLang ?: cfg.defaultLanguage)
            .trim()
            .lowercase()

        call.attributes.put(LanguageKey, lang)
    }
}
