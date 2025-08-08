package org.swirlsea.tiletalk


import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatTimestamp(timestamp: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = try {
            sdf.parse(timestamp)
        } catch (e: Exception) {
            return "Invalid Date"
        }

        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        val messageCalendar = Calendar.getInstance().apply { time = date }
        val messageDay = messageCalendar.get(Calendar.DAY_OF_YEAR)
        val messageYear = messageCalendar.get(Calendar.YEAR)

        return when {
            messageYear == currentYear && messageDay == today -> {
                // Same day: "HH:mm"
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
            messageYear == currentYear -> {
                // Same year: "MMM d, HH:mm"
                SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(date)
            }
            else -> {
                // Different year: "MMM d, yyyy"
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
            }
        }
    }
}