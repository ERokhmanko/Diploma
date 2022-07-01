package ru.netology.diploma.model

import android.net.Uri
import ru.netology.diploma.enumeration.AttachmentType

data class FileModel(val uri: Uri? = null, val type: AttachmentType? = null)

