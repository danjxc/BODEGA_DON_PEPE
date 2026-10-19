package pe.edu.cibertec.appfragmentsnavigation.carrito

import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.Producto

data class ItemCarrito(
    val producto: Producto,
    var cantidad: Int
)