package pe.edu.cibertec.appfragmentsnavigation.retrofit.request

data class DetalleVentaRequest(
    val id_producto: Int,
    val cantidad: Int
)

data class VentaRequest(
    val codigo_local: String,
    val id_usuario: Int,
    val id_cliente: Int?,
    val fecha: String,
    val detalles: List<DetalleVentaRequest>
)