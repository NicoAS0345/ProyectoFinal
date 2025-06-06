package com.nicolearaya.smartbudget.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.nicolearaya.smartbudget.DateUtils
import com.nicolearaya.smartbudget.dataFirebase.BudgetRepositoryFirebase
import com.nicolearaya.smartbudget.dataFirebase.GastosRepositoryFirebase
import com.nicolearaya.smartbudget.model.Budget
import com.nicolearaya.smartbudget.model.GastosFirebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepositoryFirebase,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private val _budget = MutableStateFlow<Budget?>(null)
    val budget: StateFlow<Budget?> = _budget.asStateFlow()

    private val _budgetHistory = MutableStateFlow<List<Budget>>(emptyList())
    val budgetHistory: StateFlow<List<Budget>> = _budgetHistory


    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        loadBudget()
    }


    private fun loadBudget() {
        viewModelScope.launch {
            repository.getBudgetFlow().collect { budget ->
                _budget.value = budget

                // Actualizar histórico si es un mes diferente
                if (budget.monthYear != DateUtils.getCurrentMonthYear()) {
                    saveHistoricalBudget(budget)
                }
            }
        }
    }

    fun updateBudget(newBudget: Double) {
        viewModelScope.launch {
            repository.updateBudget(newBudget)
        }
    }

    fun resetBudget() {
        viewModelScope.launch {
            try {
                repository.resetCurrentSpending()
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Error al reiniciar presupuesto", e)
            }
        }
    }

    suspend fun checkAndShowExceeded(amount: Double, currentMonthGastos: List<GastosFirebase>): Boolean {
        return try {
            val currentBudget = repository.getBudgetOnce()
            val totalSpending = currentMonthGastos.sumOf { it.monto }
            (totalSpending + amount) > currentBudget.monthlyBudget
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun saveHistoricalBudget(budget: Budget) {
        // Delegate this operation to the repository
        repository.saveHistoricalBudget(budget)
    }

    fun getHistoricalBudgets(): Flow<List<Budget>> {
        return repository.getHistoricalBudgets(userId)
    }
}
