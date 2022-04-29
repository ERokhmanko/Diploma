package ru.netology.diploma.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.diploma.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

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

    fun formatDate(value: Long?): String {
      return if(value==null)"Until now" else DateFormat.format("dd/MM/yyyy", Date(value * 1000)).toString()
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun uploadingAvatar(view: ImageView, avatar: String?){
        Glide.with(view)
            .load(avatar)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_avatar_24)
            .timeout(10_000)
            .into(view)
    }
}