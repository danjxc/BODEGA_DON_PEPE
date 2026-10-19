package pe.edu.cibertec.appfragmentsnavigation.retrofit.api

import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.ResultProducto
import retrofit2.Call
import retrofit2.http.GET

interface IProductoService {
    //http://TU_IP/Tda_DonPepe/ws/productos.php
    @GET("ws/productos.php")
    fun obtenerProductos(): Call<ResultProducto>
}