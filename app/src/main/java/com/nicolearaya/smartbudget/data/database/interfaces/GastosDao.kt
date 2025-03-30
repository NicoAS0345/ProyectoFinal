package com.nicole_u_latina_araya_solano.data.database.interfaces

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nicolearaya.smartbudget.model.Gastos
import kotlinx.coroutines.flow.Flow

//Esta es la interfaz para poder acceder a la tabla housenicole
@Dao
interface GastosDao {

    //Este metodo inserta un objeto a HouseNicole
    @Insert
    suspend fun insert(gastos: Gastos)

    //Este metodo actualiza un objeto de HouseNicole
    @Update
    suspend fun update(gastos: Gastos)

    //Aqui se obtienen todos los elementos de la tabla
    @Query("SELECT * FROM gastos")
    fun getAllItems(): Flow<List<Gastos>>

    //Aqui se eliminan todos los elementos de la tabla
    @Query("DELETE FROM gastos")
    suspend fun deleteAllItems()

    //Aqui se elimina solo un elemento seleccionado
    @Delete
    suspend fun delete(gastos: Gastos)

}