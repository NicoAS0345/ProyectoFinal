package com.nicolearaya.smartbudget.model


import java.io.Serializable
import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class GastosFirebase(
    var id: String = "",
    var nombreGasto: String = " ",
    var descripcion: String = " ",
    var categoria: String= " ",
    var monto: Double = 0.0,
    var isSelected: Boolean=false,
    var userId: String = " ",
    var fechaCreacion: Timestamp = Timestamp(Date()) // Para ordenar

):Parcelable, Serializable
{
    constructor() : this ("", "", "","",0.0, false, "",Timestamp(Date()))
}