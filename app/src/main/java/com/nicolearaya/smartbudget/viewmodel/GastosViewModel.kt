package com.nicolearaya.smartbudget.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.nicole_u_latina_araya_solano.data.GastosRepository
import com.nicolearaya.smartbudget.model.Gastos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nicolearaya.smartbudget.dataFirebase.GastosRepositoryFirebase
import com.nicolearaya.smartbudget.model.GastosFirebase
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

//Esta va a ser la clase que permite que el viewmodel se comunique con el repositorio y la informacion se inyecte a la capa de datos
@HiltViewModel
class GastosViewModel @Inject constructor(
    private val firebaseRepository: GastosRepositoryFirebase,
    private val auth: FirebaseAuth
): ViewModel()
{

    // Para Firebase
    private val _gastos = MutableStateFlow<List<GastosFirebase>>(emptyList())
    val gastos: StateFlow<List<GastosFirebase>> = _gastos

    //Categorias del select de categorias
    val categoriasPredeterminadas = listOf(
        "Comida",
        "Transporte",
        "Entretenimiento",
        "Vivienda",
        "Salud",
        "Educación",
        "Ropa",
        "Otros" // Esta será la opción para ingresar categoría personalizada
    )

    init {
        Log.d("FirestoreDebug", "Inicializando ViewModel")
        loadGastos()
    }

    private fun loadGastos() {
        viewModelScope.launch {  // <-- Esto crea el contexto de corrutina necesario
            try {
                firebaseRepository.getAllGastosFlow().collect { gastosList: List<GastosFirebase> ->  // <-- Ahora collect funciona
                    _gastos.value = gastosList
                    Log.d("Firestore", "Datos actualizados: ${gastosList.size} elementos")
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error al recolectar datos", e)
            }
        }
    }

    val allGastos: Flow<List<GastosFirebase>> = firebaseRepository.getAllGastosFlow()

    // Métodos para Firebase
    fun insert(gasto: GastosFirebase) = viewModelScope.launch {
        gasto.userId = auth.currentUser?.uid ?: ""
        firebaseRepository.insert(gasto)
    }

    fun update(gasto: GastosFirebase) = viewModelScope.launch {
        firebaseRepository.update(gasto)
    }

    fun delete(gasto: GastosFirebase) = viewModelScope.launch {
        firebaseRepository.delete(gasto)
    }

    fun deleteAllGastos() = viewModelScope.launch {
        firebaseRepository.deleteAllGastos()
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                // Opcional: Limpiar datos locales al cerrar sesión
                _gastos.value = emptyList()
                Log.d("Auth", "Sesión cerrada exitosamente")
            } catch (e: Exception) {
                Log.e("Auth", "Error al cerrar sesión", e)
            }
        }
    }
}