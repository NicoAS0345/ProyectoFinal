package com.nicolearaya.smartbudget.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

//Aqui se crea la entidad y se le asigna un nombre de tabla
@Entity(tableName = "gastos")

data class Gastos (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombreGasto: String = " ",
    val descripcion: String = " ",
    val categoria: String= " ",
    val monto: Double = 0.0,
    val fecha: String = "" /*SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())*/,
    val isSelected: Boolean=false

    ):Serializable