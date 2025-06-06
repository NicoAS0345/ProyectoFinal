package com.nicolearaya.smartbudget.dataFirebase

import com.google.firebase.auth.FirebaseAuth
import com.nicolearaya.smartbudget.model.Budget
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BudgetRepositoryFirebase @Inject constructor(
    private val budgetFirebase: Budget_Firebase
) {
    fun getBudgetFlow(): Flow<Budget> {
        return budgetFirebase.getBudgetFlow()
    }

    suspend fun updateBudget(newBudget: Double) {
        budgetFirebase.updateBudget(newBudget)
    }

    suspend fun updateCurrentSpending(amount: Double, isAdding: Boolean) {
        budgetFirebase.updateCurrentSpending(amount, isAdding)
    }

    suspend fun resetCurrentSpending()
    {
        budgetFirebase.resetCurrentSpending()
    }

    suspend fun getBudgetOnce(): Budget {
        return budgetFirebase.getBudgetOnce()
    }

    // BudgetRepositoryFirebase.kt
    suspend fun saveHistoricalBudget(budget: Budget) {
        budgetFirebase.saveHistoricalBudget(budget)
    }

    fun getHistoricalBudgets(userId: String): Flow<List<Budget>> {
        return budgetFirebase.getHistoricalBudgets(userId)
    }
}