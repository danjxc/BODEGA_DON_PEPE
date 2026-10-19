package pe.edu.cibertec.appfragmentsnavigation.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "producto")
data class ProductoEntity(
    @PrimaryKey
    val id_producto: Int,
    val id_categoria: Int,
    val categoria: String,
    val nombre: String,
    val precio: Double,
    val stock: Int
)