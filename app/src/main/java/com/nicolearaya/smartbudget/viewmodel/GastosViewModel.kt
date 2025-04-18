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
import com.nicolearaya.smartbudget.DateUtils
import com.nicolearaya.smartbudget.dataFirebase.BudgetRepositoryFirebase
import com.nicolearaya.smartbudget.dataFirebase.GastosRepositoryFirebase
import com.nicolearaya.smartbudget.model.GastosFirebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//Esta va a ser la clase que permite que el viewmodel se comunique con el repositorio y la informacion se inyecte a la capa de datos
@HiltViewModel
class GastosViewModel @Inject constructor(
    private val firebaseRepository: GastosRepositoryFirebase,
    private val budgetRepository: BudgetRepositoryFirebase,
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
        viewModelScope.launch {
            try {
                firebaseRepository.getAllGastosFlow().collect { gastosList ->
                    val gastosFiltrados = gastosList
                        .filter { gasto ->
                            // Filtra por usuario actual y mes actual
                            gasto.userId == auth.currentUser?.uid &&
                                    DateUtils.isCurrentMonth(gasto.fechaCreacion)
                        }
                        .sortedByDescending { it.fechaCreacion }

                    _gastos.value = gastosFiltrados
                    Log.d("ViewModel", "Mostrando ${gastosFiltrados.size} gastos del mes actual")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error al cargar gastos", e)
            }
        }
    }
    val allGastos: Flow<List<GastosFirebase>> = firebaseRepository.getAllGastosFlow()

    // Métodos para Firebase
    fun insert(gasto: GastosFirebase) = viewModelScope.launch {
        gasto.userId = auth.currentUser?.uid ?: ""
        firebaseRepository.insert(gasto)
        // Solo actualizar el presupuesto si el gasto es del mes actual
        if (DateUtils.isCurrentMonth(gasto.fechaCreacion)) {
            budgetRepository.updateCurrentSpending(gasto.monto, true)
        }
    }

    fun update(gasto: GastosFirebase): Job = viewModelScope.launch {
        try {
            Log.d("UpdateDebug", "Antes de llamar a getGastoById()")
            val oldGasto = firebaseRepository.getGastoById(gasto.id)
            Log.d("UpdateDebug", "Después de llamar a getGastoById()")
            Log.d("Updategastos2", "Gasto viejo: ${oldGasto}" )


            // Actualiza primero el gasto en Firestore
            firebaseRepository.update(gasto)

            oldGasto?.let { old ->
                // Solo actualizar si es del mes actual
                if (DateUtils.isCurrentMonth(old.fechaCreacion)) {
                    // Calcula la diferencia (nuevo - viejo)
                    val diferencia = gasto.monto - old.monto

                    Log.d("Updategastos2.1", "Gasto viejo monto: ${old.monto}" )

                    if (diferencia != 0.0) { // Solo actualizar si hay cambio
                        Log.d("Updategastos3", "diferencia: ${diferencia}" )
                         budgetRepository.updateCurrentSpending(diferencia, isAdding = true)
                    }
                }
            }

            // Vuelve a cargar los gastos para actualizar la UI
            loadGastos()
        } catch (e: Exception) {
            Log.e("GastosViewModel", "Error al actualizar gasto", e)
        }
    }

    fun delete(gasto: GastosFirebase) = viewModelScope.launch {
        firebaseRepository.delete(gasto)
        // Solo restar del presupuesto si el gasto es del mes actual
        if (DateUtils.isCurrentMonth(gasto.fechaCreacion)) {
            budgetRepository.updateCurrentSpending(gasto.monto, false)
        }
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

    // Agrega esta función
    fun getGastosGroupedByMonth(): Flow<List<Any>> {
        return allGastos.map { gastos ->
            gastos
                .filter { it.userId == auth.currentUser?.uid }
                .groupBy { gasto ->
                    val calendar = Calendar.getInstance().apply {
                        time = gasto.fechaCreacion.toDate()
                    }
                    "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
                }
                .entries
                .sortedByDescending { it.key }
                .flatMap { entry ->
                    val calendar = Calendar.getInstance().apply {
                        time = entry.value.first().fechaCreacion.toDate()
                    }
                    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                        .format(calendar.time)
                    listOf(monthName) + entry.value.sortedByDescending { it.fechaCreacion }
                }
        }
    }
}