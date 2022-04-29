package ru.netology.diploma.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.diploma.dto.Job
import ru.netology.diploma.dto.User


@Entity
data class JobWorkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val position: String,
    val start: Long,
    val finish: Long? = null,
    val link: String? = null,
) {
    fun toDto() = Job(id, name, position, start, finish, link)

    companion object {
        fun fromDto(dto: Job) = JobWorkEntity(
            dto.id,
            dto.name,
            dto.position,
            dto.start,
            dto.finish,
            dto.link,
        )
    }
}
