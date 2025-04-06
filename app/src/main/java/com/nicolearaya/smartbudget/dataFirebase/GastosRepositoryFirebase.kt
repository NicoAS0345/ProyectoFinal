package com.nicolearaya.smartbudget.dataFirebase

import androidx.lifecycle.MutableLiveData
import com.nicolearaya.smartbudget.model.GastosFirebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


class GastosRepositoryFirebase @Inject constructor(
    private val gastosFirebase: Gastos_Firebase
) {
    fun getAllGastos(): MutableLiveData<List<GastosFirebase>> {
        return gastosFirebase.getGastos()
    }

    fun getAllGastosFlow(): Flow<List<GastosFirebase>> {
        return gastosFirebase.getGastosFlow()
        /*return callbackFlow {
            val listener = firestore.collection("gastos")
                .whereEqualTo("userId", auth.currentUser?.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val gastos = snapshot?.toObjects(GastosFirebase::class.java) ?: emptyList()
                    trySend(gastos).isSuccess
                }

            awaitClose { /* Limpieza */ }
        }*/
    }

    suspend fun insert(gasto: GastosFirebase) {
        gastosFirebase.saveGasto(gasto)
    }

    suspend fun update(gasto: GastosFirebase) {
        gastosFirebase.saveGasto(gasto)
    }

    suspend fun delete(gasto: GastosFirebase) {
        gastosFirebase.deleteGasto(gasto)
    }

    suspend fun deleteAllGastos() {
        gastosFirebase.deleteAllGastos()
    }
}