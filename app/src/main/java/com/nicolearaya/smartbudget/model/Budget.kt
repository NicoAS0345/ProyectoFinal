package com.nicolearaya.smartbudget.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.nicolearaya.smartbudget.DateUtils
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Budget(
    var id: String = "",
    var userId: String = "",
    var monthlyBudget: Double = 0.0,
    var currentSpending: Double = 0.0,
    var monthYear: String = DateUtils.getCurrentMonthYear(), // Nuevo campo
    var lastUpdated: Timestamp = Timestamp(Date())
) : Parcelable {
    constructor() : this("", "", 0.0, 0.0, DateUtils.getCurrentMonthYear(), Timestamp(Date()))
}