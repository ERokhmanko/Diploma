package ru.netology.diploma.utils

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.diploma.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.stream.Collectors

object Utils {

    fun reductionInNumbers(count: Int): String {
        val formatCount = when {
            count in 1000..9999 -> {
                String.format("%.1fK", count / 1000.0)
            }
            count in 10000..999999 -> {
                String.format("%dK", count / 1000)
            }
            count > 1000000 -> {
                String.format("%.1fM", count / 1000000.0)
            }

            else -> {
                count.toString()
            }
        }
        return formatCount
    }

    @SuppressLint("NewApi")
    fun formatDate(value: String): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.ROOT)
            .withZone(ZoneId.systemDefault())

        return formatter.format(Instant.parse(value))
    }

    fun formatDate(value: Long?): String? {
        return if (value == null) null else DateFormat.format("yyyy-MM-dd", Date(value * 1000))
            .toString()
    }


    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun uploadingAvatar(view: ImageView, avatar: String?) {
        Glide.with(view)
            .load(avatar)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_avatar_24)
            .timeout(10_000)
            .into(view)
    }

    fun uploadingMedia(view: ImageView, url: String?) {
        Glide.with(view)
            .load(url)
            .timeout(10_000)
            .into(view)
    }

    fun showDateDialog(editText: EditText, context: Context) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar[Calendar.YEAR] = year
            calendar[Calendar.MONTH] = monthOfYear
            calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            editText.setText(
                SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

                    .format(calendar.time)
            )
        }

        DatePickerDialog(
            context, datePicker,
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
            .show()
    }

    fun showTimeDialog(editText: EditText, context: Context) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            editText.setText(
                SimpleDateFormat("HH:mm", Locale.ROOT)
                    .format(calendar.time)
            )
        }
        TimePickerDialog(
            context, timePicker,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), true
        )
            .show()
    }

    fun dateToEpochSec(str: String?): Long? {
        return if (str.isNullOrBlank()) null else LocalDate.parse(str)
            .atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond()
    }

    fun formatToInstant(value: String): String {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(value)
        val formatter = DateTimeFormatter.ISO_INSTANT

        return formatter.format(date?.toInstant())
    }

    fun listToString(list: List<String?>): String {
        return list.stream()
            .map { n -> java.lang.String.valueOf(n) }
            .collect(Collectors.joining(", ", "", ""))
    }

    fun formatToDate(value: String): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.ROOT)
            .withZone(ZoneId.systemDefault())

        return formatter.format(Instant.parse(value))
    }
}