package pe.edu.cibertec.appfragmentsnavigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pe.edu.cibertec.appfragmentsnavigation.databinding.ActivityLoginBinding
import pe.edu.cibertec.appfragmentsnavigation.retrofit.ClienteDonPepeRetrofit
import pe.edu.cibertec.appfragmentsnavigation.retrofit.request.LoginRequest
import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIngresar.setOnClickListener {
            val usuario = binding.etUsuario.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                iniciarSesion(usuario, password)
            }
        }
    }

    private fun iniciarSesion(usuario: String, password: String) {
        binding.btnIngresar.isEnabled = false
        ClienteDonPepeRetrofit.retrofitUsuarioService
            .login(LoginRequest(usuario, password))
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    binding.btnIngresar.isEnabled = true
                    val body = response.body()
                    if (response.isSuccessful && body != null && body.rpta) {
                        val prefs = getSharedPreferences("sesion", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putInt("id_usuario", body.id_usuario)
                            .putString("nombres", body.nombres)
                            .putString("rol", body.rol)
                            .apply()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else if (response.code() == 401) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Usuario o contraseña incorrectos",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error del servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    binding.btnIngresar.isEnabled = true
                    Toast.makeText(
                        this@LoginActivity,
                        "No se pudo conectar con el servidor",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}