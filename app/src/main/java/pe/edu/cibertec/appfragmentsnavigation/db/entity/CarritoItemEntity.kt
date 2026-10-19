package pe.edu.cibertec.appfragmentsnavigation.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrito_item")
data class CarritoItemEntity(
    @PrimaryKey
    val id_producto: Int,
    val id_categoria: Int,
    val categoria: String,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val cantidad: Int
)