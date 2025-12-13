package com.spasinnya.domain.model.auth

data class EmailMessage(
    val to: String,
    val subject: String,
    val text: String
)
