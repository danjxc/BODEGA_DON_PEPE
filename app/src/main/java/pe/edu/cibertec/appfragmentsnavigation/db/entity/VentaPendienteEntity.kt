package pe.edu.cibertec.appfragmentsnavigation.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venta_pendiente")
data class VentaPendienteEntity(
    @PrimaryKey
    val codigo_local: String,
    val id_usuario: Int,
    val fecha: String,
    val detalles_json: String,
    val total: Double
)