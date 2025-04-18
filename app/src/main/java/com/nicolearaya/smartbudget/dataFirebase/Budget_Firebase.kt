package com.nicolearaya.smartbudget.dataFirebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nicolearaya.smartbudget.DateUtils
import com.nicolearaya.smartbudget.model.Budget
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class Budget_Firebase @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val collectionName = "UserBudgets"

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")

    fun getBudgetFlow(): Flow<Budget> = callbackFlow {
        val documentRef = firestore.collection(collectionName).document(userId)
        var listenerRegistered = false

        try {
            // Verificar si el documento existe primero
            val snapshot = documentRef.get().await()

            if (!snapshot.exists()) {
                // Crear documento inicial si no existe
                val initialBudget = Budget(userId = userId)
                documentRef.set(initialBudget).await()
                trySend(initialBudget)
            } else {
                // Enviar datos iniciales
                val existingBudget = snapshot.toObject(Budget::class.java) ?: Budget(userId = userId)
                trySend(existingBudget)
            }

            // Configurar listener para cambios en tiempo real
            val listener = documentRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BudgetFirebase", "Listener error", error)
                    return@addSnapshotListener
                }

                try {
                    snapshot?.let {
                        val budget = it.toObject(Budget::class.java) ?: Budget(userId = userId)
                        trySend(budget)
                    }
                } catch (e: Exception) {
                    Log.e("BudgetFirebase", "Error processing snapshot", e)
                }
            }

            listenerRegistered = true
            awaitClose {
                Log.d("BudgetFirebase", "Removing listener")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e("BudgetFirebase", "Initial setup error", e)
            close(e)
            if (!listenerRegistered) {
                awaitClose { } // Empty awaitClose if listener wasn't registered
            }
        }
    }

    suspend fun updateBudget(newBudget: Double) {
        firestore.collection(collectionName)
            .document(userId)
            .update(
                "monthlyBudget", newBudget,
                "monthYear", DateUtils.getCurrentMonthYear() // Actualizar mes/año
            )
            .await()
    }

    suspend fun updateCurrentSpending(amount: Double, isAdding: Boolean) {
        Log.d("Updategastos5", "monto: ${amount}" )
        firestore.runTransaction { transaction ->
            val document = firestore.collection(collectionName).document(userId)
            val snapshot = transaction.get(document)
            val currentSpending = snapshot.getDouble("currentSpending") ?: 0.0
            Log.d("Updategastos4", "gasto actual: ${currentSpending}" )
            val newSpending = if (isAdding) currentSpending + amount else currentSpending - amount
            Log.d("Updategastos6", "gasto actual despues de que lo tuvo que actualizar: ${newSpending}" )
            transaction.update(document, "currentSpending", newSpending)
        }.await()
    }


    suspend fun resetCurrentSpending() {
        try {
            val budgetRef = firestore.collection(collectionName).document(userId)
            firestore.runTransaction { transaction ->
                transaction.update(budgetRef,
                    "monthlyBudget", 0.0,
                    "currentSpending", 0.0,
                    "monthYear", DateUtils.getCurrentMonthYear() // Resetear mes/año
                )
            }.await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getBudgetOnce(): Budget {
        val snapshot = firestore.collection(collectionName)
            .document(userId)
            .get()
            .await()

        return snapshot.toObject(Budget::class.java) ?: Budget(userId = userId)
    }

    // Budget_Firebase.kt
    suspend fun saveHistoricalBudget(budget: Budget) {
        firestore.collection("BudgetHistory")
            .document("${budget.userId}_${budget.monthYear}")
            .set(budget)
            .await()
    }

    fun getHistoricalBudgets(userId: String): Flow<List<Budget>> = callbackFlow {
        val listener = firestore.collection("BudgetHistory")
            .whereEqualTo("userId", userId)
            .orderBy("monthYear", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val budgets = snapshot?.toObjects(Budget::class.java) ?: emptyList()
                trySend(budgets)
            }
        awaitClose { listener.remove() }
    }
}