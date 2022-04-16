package ru.netology.diploma.dto

import ru.netology.diploma.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

