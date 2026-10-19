package pe.edu.cibertec.appfragmentsnavigation.retrofit.api

import pe.edu.cibertec.appfragmentsnavigation.retrofit.request.VentaRequest
import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.VentaResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IVentaService {
    @POST("ws/venta_registrar.php")
    fun registrar(@Body request: VentaRequest): Call<VentaResponse>
}