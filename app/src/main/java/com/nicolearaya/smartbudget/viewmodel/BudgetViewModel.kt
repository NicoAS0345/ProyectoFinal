package com.nicolearaya.smartbudget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nicolearaya.smartbudget.dataFirebase.BudgetRepositoryFirebase
import com.nicolearaya.smartbudget.model.Budget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepositoryFirebase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _budget = MutableStateFlow<Budget?>(null)
    val budget: StateFlow<Budget?> = _budget.asStateFlow()

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    init {
        loadBudget()
    }

    private fun loadBudget() {
        viewModelScope.launch {
            repository.getBudgetFlow().collect { budget ->
                _budget.value = budget
            }
        }
    }

    fun updateBudget(newBudget: Double) {
        viewModelScope.launch {
            repository.updateBudget(newBudget)
        }
    }
}