package com.nicolearaya.smartbudget

import android.util.Log
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun getCurrentMonthYear(): String {
        return SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
    }

    fun isCurrentMonth(timestamp: Timestamp): Boolean {
        val calendarGasto = Calendar.getInstance().apply {
            time = timestamp.toDate()
        }
        val calendarActual = Calendar.getInstance()

        return calendarGasto.get(Calendar.YEAR) == calendarActual.get(Calendar.YEAR) &&
                calendarGasto.get(Calendar.MONTH) == calendarActual.get(Calendar.MONTH)
    }

    fun formatTimestampForDisplay(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    fun formatDateForDisplay(dateStr: String): String {
        return try {
            dateFormat.parse(dateStr)?.let { dateFormat.format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}