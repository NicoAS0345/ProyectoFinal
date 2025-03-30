package com.nicole_u_latina_araya_solano.data

import javax.inject.Inject
import androidx.lifecycle.LiveData
import com.nicole_u_latina_araya_solano.data.database.interfaces.GastosDao
import com.nicolearaya.smartbudget.model.Gastos
import kotlinx.coroutines.flow.Flow

//Este es el repositorio en donde se manejan las operacones de datos relacionadas a la base de datos y la entidad
class GastosRepository @Inject constructor(private val gastosDao: GastosDao){

    //Aqui se obtienen todos los elementos de la base de datos
    fun getAllItems(): Flow<List<Gastos>> = gastosDao.getAllItems()

    //Aqui se inserta un objeto a la base de datos
    suspend fun insert(gastos: Gastos) {
        gastosDao.insert(gastos)
    }

    //Aqui se actualiza un objeto que ya exista en la base de datos
    suspend fun update(gastos: Gastos) {
        gastosDao.update(gastos)
    }

    //Este elimina todos los elementos de la base de datos
    suspend fun deleteAllItems() {
        gastosDao.deleteAllItems()
    }

    //Y aqui se elimina un objeto especifico de la base de datos
    suspend fun delete(gastos: Gastos) {
        gastosDao.delete(gastos)
    }
}