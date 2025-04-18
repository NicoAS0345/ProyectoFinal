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


    fun getMonthYearFromTimestamp(timestamp: Timestamp): String {
        val calendar = Calendar.getInstance().apply {
            time = timestamp.toDate()
        }
        return SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(calendar.time)
    }

    fun isSameMonthYear(monthYear1: String, monthYear2: String): Boolean {
        return monthYear1 == monthYear2
    }

    // DateUtils.kt
    fun formatMonthYear(monthYear: String): String {
        return try {
            // Parsear el formato MM/yyyy a un formato más legible (ej: "Abril 2023")
            val inputFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(monthYear)

            val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.e("DateUtils", "Error formateando fecha: $monthYear", e)
            monthYear // Devuelve el original si hay error
        }
    }
}