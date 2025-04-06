package com.nicolearaya.smartbudget.dataFirebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nicolearaya.smartbudget.model.GastosFirebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class Gastos_Firebase @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val coleccion1 = "SmartBudget"
    private val coleccion2 = "UserGastos"

    private val usuario: String
        get() = auth.currentUser?.email ?: throw IllegalStateException("Usuario no autenticado")

    fun saveGasto(gasto: GastosFirebase) {
        val document = if (gasto.id.isEmpty()) {
            firestore.collection(coleccion1)
                .document(usuario)
                .collection(coleccion2)
                .document()
                .also { gasto.id = it.id }
        } else {
            firestore.collection(coleccion1)
                .document(usuario)
                .collection(coleccion2)
                .document(gasto.id)
        }

        document.set(gasto)
            .addOnSuccessListener {
                Log.d("Firestore", "Documento guardado en: SmartBudget/$usuario/UserGastos/${gasto.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al guardar en: SmartBudget/$usuario/UserGastos/${gasto.id}", e)
            }

        document.set(gasto)
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al guardar gasto", e)
            }
    }

    fun deleteGasto(gasto: GastosFirebase) {
        if(gasto.id.isNotEmpty()) {
            firestore.collection(coleccion1)
                .document(usuario)
                .collection(coleccion2)
                .document(gasto.id)
                .delete()
                .addOnSuccessListener {
                    Log.d("DeleteGasto", "Gasto eliminado")
                }
                .addOnFailureListener {
                    Log.e("DeleteGasto", "Gasto NO eliminado")
                }
        }
    }

    fun getGastos(): MutableLiveData<List<GastosFirebase>> {
        val listaGastos = MutableLiveData<List<GastosFirebase>>()

        firestore.collection(coleccion1)
            .document(usuario)
            .collection(coleccion2)
            .addSnapshotListener { instantenea, error ->
                if(error != null) {
                    return@addSnapshotListener
                }
                if(instantenea != null) {
                    val lista = ArrayList<GastosFirebase>()
                    instantenea.documents.forEach {
                        val gasto = it.toObject(GastosFirebase::class.java)
                        if(gasto != null) {
                            lista.add(gasto)
                        }
                    }
                    listaGastos.value = lista
                }
            }

        return listaGastos
    }

    fun getGastosFlow(): Flow<List<GastosFirebase>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("Firestore", "Usuario no autenticado")
            close()
            return@callbackFlow
        }

        // LOG CRÍTICO para verificar la ruta completa
        Log.d("FULL_PATH", "SmartBudget/${usuario}/UserGastos where userId=$userId")

        firestore.collection("SmartBudget")
            .document(usuario) // Asegúrate que "usuario" es el email correcto
            .collection("UserGastos")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Error: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                // LOG para ver documentos brutos
                snapshot?.documents?.forEach { doc ->
                    Log.d("DOCUMENT_DATA", "ID: ${doc.id}, Data: ${doc.data}")
                }

                val gastos = snapshot?.toObjects(GastosFirebase::class.java) ?: emptyList()
                Log.d("FirestoreData", "Datos convertidos (${gastos.size}): $gastos")
                trySend(gastos)
            }

        awaitClose { /* Limpieza */ }
    }

    fun deleteAllGastos() {
        val itemsRef = firestore.collection(coleccion1)
            .document(usuario)
            .collection(coleccion2)

        itemsRef.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firestore.batch()
                querySnapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("DeleteAll", "Todos los gastos eliminados")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DeleteAll", "Error al eliminar gastos: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DeleteAll", "Error al obtener gastos: ${e.message}")
            }
    }
}