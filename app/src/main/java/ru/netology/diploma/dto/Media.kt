package ru.netology.diploma.dto

import android.net.Uri
import java.io.File
import java.io.InputStream

data class Media(val url: String)

data class MediaUpload(val file: File)

