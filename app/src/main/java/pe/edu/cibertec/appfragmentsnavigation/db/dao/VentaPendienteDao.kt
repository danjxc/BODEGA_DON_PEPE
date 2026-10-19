package pe.edu.cibertec.appfragmentsnavigation.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pe.edu.cibertec.appfragmentsnavigation.db.entity.VentaPendienteEntity

@Dao
interface VentaPendienteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertar(venta: VentaPendienteEntity)

    @Query("DELETE FROM venta_pendiente WHERE codigo_local = :codigo")
    fun eliminar(codigo: String)

    @Query("SELECT * FROM venta_pendiente ORDER BY fecha")
    fun obtenerTodos(): List<VentaPendienteEntity>

    @Query("SELECT COUNT(*) FROM venta_pendiente")
    fun contar(): Int
}