package pe.edu.cibertec.appfragmentsnavigation.retrofit

import okhttp3.OkHttpClient
import pe.edu.cibertec.appfragmentsnavigation.retrofit.api.ICategoriaService
import pe.edu.cibertec.appfragmentsnavigation.retrofit.api.IProductoService
import pe.edu.cibertec.appfragmentsnavigation.retrofit.api.IUsuarioService
import pe.edu.cibertec.appfragmentsnavigation.retrofit.api.IVentaService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteDonPepeRetrofit {

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.99.192.33/Tda_DonPepe/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitUsuarioService: IUsuarioService by lazy {
        retrofit.create(IUsuarioService::class.java)
    }

    val retrofitProductoService: IProductoService by lazy {
        retrofit.create(IProductoService::class.java)
    }

    val retrofitCategoriaService: ICategoriaService by lazy {
        retrofit.create(ICategoriaService::class.java)
    }

    val retrofitVentaService: IVentaService by lazy {
        retrofit.create(IVentaService::class.java)
    }
}