package pe.edu.cibertec.appfragmentsnavigation.retrofit.api

import pe.edu.cibertec.appfragmentsnavigation.retrofit.request.LoginRequest
import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IUsuarioService {
    //http://TU_IP/Tda_DonPepe/ws/login.php
    @POST("ws/login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}