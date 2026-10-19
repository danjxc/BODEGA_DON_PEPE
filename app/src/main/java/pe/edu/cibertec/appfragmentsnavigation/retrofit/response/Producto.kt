package pe.edu.cibertec.appfragmentsnavigation.retrofit.response

data class Producto(
    val id_producto: Int,
    val id_categoria: Int,
    val categoria: String,
    val nombre: String,
    val precio: Double,
    val stock: Int
)