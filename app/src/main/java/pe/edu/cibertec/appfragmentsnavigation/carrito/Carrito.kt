package pe.edu.cibertec.appfragmentsnavigation.carrito

import android.content.Context
import android.os.Handler
import android.os.Looper
import pe.edu.cibertec.appfragmentsnavigation.db.DonPepeRoomDatabase
import pe.edu.cibertec.appfragmentsnavigation.db.dao.CarritoDao
import pe.edu.cibertec.appfragmentsnavigation.db.entity.CarritoItemEntity
import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.Producto
import java.util.concurrent.Executors

object Carrito {

    private val items = mutableListOf<ItemCarrito>()
    private var contexto: Context? = null
    private var cargado = false
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    // Recupera de SQLite lo que quedó guardado (se ejecuta una sola vez)
    fun cargar(context: Context, onListo: (() -> Unit)? = null) {
        contexto = context.applicationContext
        if (cargado) {
            onListo?.invoke()
            return
        }
        cargado = true
        executor.execute {
            val guardados = DonPepeRoomDatabase.getDatabase(contexto!!).carritoDao().obtenerTodos()
            handler.post {
                for (e in guardados) {
                    if (items.none { it.producto.id_producto == e.id_producto }) {
                        items.add(
                            ItemCarrito(
                                Producto(
                                    id_producto = e.id_producto,
                                    id_categoria = e.id_categoria,
                                    categoria = e.categoria,
                                    nombre = e.nombre,
                                    precio = e.precio,
                                    stock = e.stock
                                ),
                                e.cantidad
                            )
                        )
                    }
                }
                onListo?.invoke()
            }
        }
    }

    fun obtenerItems(): List<ItemCarrito> = items.toList()

    // REGISTRO: producto nuevo -> insertar / MODIFICACIÓN: ya existe -> actualizar cantidad
    fun agregar(producto: Producto): Boolean {
        val existente = items.find { it.producto.id_producto == producto.id_producto }
        if (existente == null) {
            if (producto.stock < 1) return false
            val nuevo = ItemCarrito(producto, 1)
            items.add(nuevo)
            val entidad = aEntidad(nuevo)
            ejecutar { it.insertar(entidad) }
            return true
        }
        if (existente.cantidad >= existente.producto.stock) return false
        existente.cantidad++
        val entidad = aEntidad(existente)
        ejecutar { it.actualizar(entidad) }
        return true
    }

    // MODIFICACIÓN: baja la cantidad (mínimo 1; para sacarlo se usa Quitar)
    fun restar(idProducto: Int) {
        val item = items.find { it.producto.id_producto == idProducto } ?: return
        if (item.cantidad > 1) {
            item.cantidad--
            val entidad = aEntidad(item)
            ejecutar { it.actualizar(entidad) }
        }
    }

    // BORRADO: elimina el producto del carrito y de SQLite
    fun quitar(idProducto: Int) {
        val item = items.find { it.producto.id_producto == idProducto } ?: return
        items.remove(item)
        ejecutar { it.eliminar(idProducto) }
    }

    fun total(): Double = items.sumOf { it.producto.precio * it.cantidad }

    fun vaciar() {
        items.clear()
        ejecutar { it.vaciar() }
    }

    private fun aEntidad(item: ItemCarrito) = CarritoItemEntity(
        id_producto = item.producto.id_producto,
        id_categoria = item.producto.id_categoria,
        categoria = item.producto.categoria ?: "",
        nombre = item.producto.nombre,
        precio = item.producto.precio,
        stock = item.producto.stock,
        cantidad = item.cantidad
    )

    private fun ejecutar(tarea: (CarritoDao) -> Unit) {
        val ctx = contexto ?: return
        executor.execute {
            tarea(DonPepeRoomDatabase.getDatabase(ctx).carritoDao())
        }
    }
}