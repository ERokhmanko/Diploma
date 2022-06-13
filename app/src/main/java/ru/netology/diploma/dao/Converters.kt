package ru.netology.diploma.dao

import androidx.room.TypeConverter
import ru.netology.diploma.enumeration.AttachmentType
import ru.netology.diploma.enumeration.EventType

class Converters {
    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name

    @TypeConverter
    fun fromSet(set: Set<Long>): String = set.joinToString(",")

    @TypeConverter
    fun toSet(data: String): Set<Long> =
        if (data.isBlank()) emptySet() else data.split(",").map { it.toLong() }.toSet()
}