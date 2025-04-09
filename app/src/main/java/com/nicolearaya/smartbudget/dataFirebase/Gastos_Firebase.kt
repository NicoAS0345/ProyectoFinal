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

// Clase que maneja todas las operaciones con Firebase Firestore para los gastos
class Gastos_Firebase @Inject constructor(
    private val auth: FirebaseAuth, // Inyecta la autenticación de Firebase
    private val firestore: FirebaseFirestore // Inyecta Firestore
) {
    // Nombres de las colecciones en Firestore
    private val coleccion1 = "SmartBudget" // Colección principal
    private val coleccion2 = "UserGastos"  // Subcolección para gastos de usuario

    // Propiedad que obtiene el email del usuario actual o lanza excepción si no está autenticado
    private val usuario: String
        get() = auth.currentUser?.email ?: throw IllegalStateException("Usuario no autenticado")

     //Guarda un gasto en Firestore (crea uno nuevo o actualiza existente)

    fun saveGasto(gasto: GastosFirebase) {
        // Determina si es un nuevo gasto o una actualización
        val document = if (gasto.id.isEmpty()) {
            // Nuevo gasto: crea documento con ID automático
            firestore.collection(coleccion1)
                .document(usuario)
                .collection(coleccion2)
                .document()
                .also { gasto.id = it.id } // Asigna el ID generado al objeto gasto
        } else {
            // Gasto existente: referencia al documento con el ID del gasto
            firestore.collection(coleccion1)
                .document(usuario)
                .collection(coleccion2)
                .document(gasto.id)
        }

        // Guarda el gasto en Firestore
        document.set(gasto)
            .addOnSuccessListener {
                Log.d("Firestore", "Documento guardado en: SmartBudget/$usuario/UserGastos/${gasto.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al guardar en: SmartBudget/$usuario/UserGastos/${gasto.id}", e)
            }
    }

 //Elimina un gasto de Firestore

    fun deleteGasto(gasto: GastosFirebase) {
        if(gasto.id.isNotEmpty()) { // Solo intenta borrar si el gasto tiene ID
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

    // Obtiene todos los gastos del usuario actual como LiveData

    fun getGastos(): MutableLiveData<List<GastosFirebase>> {
        val listaGastos = MutableLiveData<List<GastosFirebase>>()

        // Escucha cambios en la colección en tiempo real
        firestore.collection(coleccion1)
            .document(usuario)
            .collection(coleccion2)
            .addSnapshotListener { instantenea, error ->
                if(error != null) {
                    return@addSnapshotListener // Ignora errores
                }
                if(instantenea != null) {
                    val lista = ArrayList<GastosFirebase>()
                    instantenea.documents.forEach {
                        val gasto = it.toObject(GastosFirebase::class.java)
                        if(gasto != null) {
                            lista.add(gasto)
                        }
                    }
                    listaGastos.value = lista // Actualiza el LiveData
                }
            }

        return listaGastos
    }

    //Obtiene los gastos como Flow (alternativa a LiveData)

    fun getGastosFlow(): Flow<List<GastosFirebase>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("Firestore", "Usuario no autenticado")
            close() // Cierra el Flow si no hay usuario
            return@callbackFlow
        }

        // Log para depuración
        Log.d("FULL_PATH", "SmartBudget/${usuario}/UserGastos where userId=$userId")

        // Escucha cambios en la colección filtrados por userId
        val listener = firestore.collection("SmartBudget")
            .document(usuario)
            .collection("UserGastos")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Error: ${error.message}", error)
                    close(error) // Cierra el Flow con error
                    return@addSnapshotListener
                }


                snapshot?.documents?.forEach { doc ->
                    Log.d("DOCUMENT_DATA", "ID: ${doc.id}, Data: ${doc.data}")
                }

                // Convierte los documentos a objetos GastosFirebase
                val gastos = snapshot?.toObjects(GastosFirebase::class.java) ?: emptyList()
                Log.d("FirestoreData", "Datos convertidos (${gastos.size}): $gastos")
                trySend(gastos) // Envía los datos a través del Flow
            }

        // Limpieza cuando se cancela el Flow
        awaitClose { listener.remove() }
    }

    // Elimina todos los gastos del usuario actual

    fun deleteAllGastos() {
        val itemsRef = firestore.collection(coleccion1)
            .document(usuario)
            .collection(coleccion2)

        // Obtiene todos los documentos y los elimina en un lote
        itemsRef.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firestore.batch() // Operación por lotes
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