package pe.edu.cibertec.appfragmentsnavigation.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pe.edu.cibertec.appfragmentsnavigation.db.entity.CarritoItemEntity

@Dao
interface CarritoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertar(item: CarritoItemEntity)

    @Update
    fun actualizar(item: CarritoItemEntity)

    @Query("DELETE FROM carrito_item WHERE id_producto = :idProducto")
    fun eliminar(idProducto: Int)

    @Query("DELETE FROM carrito_item")
    fun vaciar()

    @Query("SELECT * FROM carrito_item ORDER BY nombre")
    fun obtenerTodos(): List<CarritoItemEntity>
}