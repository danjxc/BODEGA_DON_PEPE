package pe.edu.cibertec.appfragmentsnavigation.retrofit.response

data class LoginResponse(
    val rpta: Boolean,
    val id_usuario: Int,
    val nombres: String?,
    val rol: String?,
    val mensaje: String?
)