package ru.netology.diploma.model

/**
 * Data validation state of the event form.
 */
data class EventFormState(
    val linkError: Int? = null,
    val isDataValid: Boolean = false,
    val isDataNotBlank: Boolean = false,
    val emptyDateError: Int? = null,
    val emptyTimeError: Int? = null,
    val emptyLinkError: Int? = null,
    val emptyCoordError: Int? = null,
    val emptyContentError: Int? = null
)