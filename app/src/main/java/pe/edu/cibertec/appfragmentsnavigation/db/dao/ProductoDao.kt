package pe.edu.cibertec.appfragmentsnavigation.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pe.edu.cibertec.appfragmentsnavigation.db.entity.ProductoEntity

@Dao
interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarTodos(productos: List<ProductoEntity>)

    @Query("DELETE FROM producto")
    fun eliminarTodo()

    @Query("SELECT * FROM producto ORDER BY nombre")
    fun obtenerTodos(): List<ProductoEntity>

    @Query("SELECT * FROM producto WHERE nombre LIKE '%' || :texto || '%' ORDER BY nombre")
    fun buscar(texto: String): List<ProductoEntity>
}