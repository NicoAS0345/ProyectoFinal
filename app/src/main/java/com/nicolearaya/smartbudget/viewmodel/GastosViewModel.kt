package com.nicolearaya.smartbudget.viewmodel

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

//Esta va a ser la clase que permite que el viewmodel se comunique con el repositorio y la informacion se inyecte a la capa de datos
@HiltViewModel
class GastosViewModel @Inject constructor(
    private val repository: GastosRepository
): ViewModel()
{
    //Este LiveData es el que contiene todos los elementos que se encuentran en HouseNicole
    val allItems: Flow<List<Gastos>> get() = repository.getAllItems()


    // Cambia a StateFlow para mejor manejo de estados
    private val _gastos = MutableStateFlow<List<Gastos>>(emptyList())
    val gastos: StateFlow<List<Gastos>> = _gastos.asStateFlow()


    //Estos metodos son iguales a los que se han explicado en el DAO y el repositorio pero con la diferencia de que se ejecutan desde el viewmodel
    fun insert(gastos: Gastos) = viewModelScope.launch {
        repository.insert(gastos)
    }

    fun update(gastos: Gastos) = viewModelScope.launch {
        repository.update(gastos)
    }
    fun delete(gastos: Gastos) {
        viewModelScope.launch {
            repository.delete(gastos)
        }
    }

    fun deleteAllItems() = viewModelScope.launch {
        repository.deleteAllItems()
    }
}