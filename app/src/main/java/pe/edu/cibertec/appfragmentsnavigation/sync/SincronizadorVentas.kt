package pe.edu.cibertec.appfragmentsnavigation.sync

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import pe.edu.cibertec.appfragmentsnavigation.db.DonPepeRoomDatabase
import pe.edu.cibertec.appfragmentsnavigation.retrofit.ClienteDonPepeRetrofit
import pe.edu.cibertec.appfragmentsnavigation.retrofit.request.DetalleVentaRequest
import pe.edu.cibertec.appfragmentsnavigation.retrofit.request.VentaRequest
import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.VentaResponse
import retrofit2.Response
import java.io.IOException

object SincronizadorVentas {

    // Envía al servidor las ventas guardadas en SQLite. Las enviadas se borran del celular.
    fun sincronizar(
        context: Context,
        onTerminado: (enviadas: Int, pendientes: Int, mensaje: String?) -> Unit
    ) {
        val appContext = context.applicationContext
        val handler = Handler(Looper.getMainLooper())

        Thread {
            // Descarta conexiones viejas que pudieron quedar muertas al cambiar de red
            ClienteDonPepeRetrofit.okHttpClient.connectionPool.evictAll()

            val dao = DonPepeRoomDatabase.getDatabase(appContext).ventaPendienteDao()
            val gson = Gson()
            var enviadas = 0
            var mensaje: String? = null

            for (venta in dao.obtenerTodos()) {
                val detalles = gson.fromJson(
                    venta.detalles_json, Array<DetalleVentaRequest>::class.java
                ).toList()

                val request = VentaRequest(
                    codigo_local = venta.codigo_local,
                    id_usuario = venta.id_usuario,
                    id_cliente = null,
                    fecha = venta.fecha,
                    detalles = detalles
                )

                try {
                    val response = enviar(request)

                    var cuerpo = response.body()
                    if (cuerpo == null) {
                        try {
                            cuerpo = gson.fromJson(
                                response.errorBody()?.string(), VentaResponse::class.java
                            )
                        } catch (e: Exception) {
                            cuerpo = null
                        }
                    }

                    val yaRegistrada = cuerpo?.mensaje
                        ?.contains("ya estaba registrada", ignoreCase = true) == true

                    if (cuerpo?.rpta == true || yaRegistrada) {
                        dao.eliminar(venta.codigo_local)
                        enviadas++
                    } else {
                        mensaje = cuerpo?.mensaje ?: "El servidor rechazó una venta (código ${response.code()})"
                    }
                } catch (e: IOException) {
                    mensaje = "Sin conexión con el servidor (${e.javaClass.simpleName}: ${e.message})"
                    break
                }
            }

            val pendientes = dao.contar()
            handler.post { onTerminado(enviadas, pendientes, mensaje) }
        }.start()
    }

    // Si falla por una conexión vieja, limpia el pool y reintenta una vez
    private fun enviar(request: VentaRequest): Response<VentaResponse> {
        return try {
            ClienteDonPepeRetrofit.retrofitVentaService.registrar(request).execute()
        } catch (e: IOException) {
            ClienteDonPepeRetrofit.okHttpClient.connectionPool.evictAll()
            ClienteDonPepeRetrofit.retrofitVentaService.registrar(request).execute()
        }
    }
}