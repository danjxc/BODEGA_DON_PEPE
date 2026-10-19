package pe.edu.cibertec.appfragmentsnavigation.retrofit.response

data class VentaResponse(
    val rpta: Boolean,
    val id_venta: Int,
    val total: Double,
    val mensaje: String?
)