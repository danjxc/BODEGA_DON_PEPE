package pe.edu.cibertec.appfragmentsnavigation.retrofit.api

import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.ResultCategoria
import retrofit2.Call
import retrofit2.http.GET

interface ICategoriaService {
    //http://TU_IP/Tda_DonPepe/ws/categorias.php
    @GET("ws/categorias.php")
    fun obtenerCategorias(): Call<ResultCategoria>
}