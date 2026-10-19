package pe.edu.cibertec.appfragmentsnavigation

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import pe.edu.cibertec.appfragmentsnavigation.adapter.CarritoAdapter
import pe.edu.cibertec.appfragmentsnavigation.carrito.Carrito
import pe.edu.cibertec.appfragmentsnavigation.databinding.FragmentCarritoBinding
import pe.edu.cibertec.appfragmentsnavigation.db.DonPepeRoomDatabase
import pe.edu.cibertec.appfragmentsnavigation.db.entity.VentaPendienteEntity
import pe.edu.cibertec.appfragmentsnavigation.retrofit.ClienteDonPepeRetrofit
import pe.edu.cibertec.appfragmentsnavigation.retrofit.request.DetalleVentaRequest
import pe.edu.cibertec.appfragmentsnavigation.retrofit.request.VentaRequest
import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.VentaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CarritoFragment : Fragment() {

    private var _binding: FragmentCarritoBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CarritoAdapter

    // Se reutiliza si el envío falla, para que el servidor no duplique la venta
    private var codigoLocal: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarritoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CarritoAdapter(
            onMas = { item ->
                Carrito.agregar(item.producto)
                codigoLocal = null
                refrescar()
            },
            onMenos = { item ->
                Carrito.restar(item.producto.id_producto)
                codigoLocal = null
                refrescar()
            },
            onQuitar = { item ->
                Carrito.quitar(item.producto.id_producto)
                codigoLocal = null
                refrescar()
            }
        )

        binding.rvcarrito.layoutManager = LinearLayoutManager(requireContext())
        binding.rvcarrito.adapter = adapter

        binding.btnFinalizar.setOnClickListener { finalizarCompra() }

        refrescar()

        // Recupera de SQLite el carrito guardado (va al final, cuando el adapter ya existe)
        Carrito.cargar(requireContext()) { if (_binding != null) refrescar() }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) refrescar()
    }

    private fun refrescar() {
        val items = Carrito.obtenerItems()
        adapter.actualizarLista(items)
        binding.tvtotal.text = String.format(Locale.US, "S/ %.2f", Carrito.total())
        val vacio = items.isEmpty()
        binding.tvvacio.visibility = if (vacio) View.VISIBLE else View.GONE
        binding.btnFinalizar.isEnabled = !vacio
    }

    private fun finalizarCompra() {
        val items = Carrito.obtenerItems()
        if (items.isEmpty()) return

        val prefs = requireContext().getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val idUsuario = prefs.getInt("id_usuario", 0)
        val totalVenta = Carrito.total()

        if (codigoLocal == null) {
            codigoLocal = "APP-" + UUID.randomUUID().toString()
        }

        val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val request = VentaRequest(
            codigo_local = codigoLocal!!,
            id_usuario = idUsuario,
            id_cliente = null,
            fecha = fecha,
            detalles = items.map { DetalleVentaRequest(it.producto.id_producto, it.cantidad) }
        )

        binding.btnFinalizar.isEnabled = false
        binding.btnFinalizar.text = "Enviando..."

        ClienteDonPepeRetrofit.retrofitVentaService.registrar(request)
            .enqueue(object : Callback<VentaResponse> {
                override fun onResponse(call: Call<VentaResponse>, response: Response<VentaResponse>) {
                    if (_binding == null) return
                    binding.btnFinalizar.text = "Finalizar compra"

                    if (response.isSuccessful && response.body()?.rpta == true) {
                        val total = response.body()!!.total
                        Carrito.vaciar()
                        codigoLocal = null
                        refrescar()
                        Toast.makeText(
                            requireContext(),
                            "Venta registrada. Total: S/ %.2f".format(Locale.US, total),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // 422 u otro error: el mensaje viene en el cuerpo de error
                        var mensaje = response.body()?.mensaje
                        if (mensaje == null) {
                            try {
                                val texto = response.errorBody()?.string()
                                mensaje = Gson().fromJson(texto, VentaResponse::class.java)?.mensaje
                            } catch (e: Exception) {
                                mensaje = null
                            }
                        }
                        Toast.makeText(
                            requireContext(),
                            mensaje ?: "No se pudo registrar la venta",
                            Toast.LENGTH_LONG
                        ).show()
                        refrescar()
                    }
                }

                override fun onFailure(call: Call<VentaResponse>, t: Throwable) {
                    if (_binding == null) return
                    binding.btnFinalizar.text = "Finalizar compra"
                    // Sin conexión: la venta se guarda en SQLite para enviarla después
                    guardarVentaPendiente(request, totalVenta)
                }
            })
    }

    private fun guardarVentaPendiente(request: VentaRequest, total: Double) {
        val pendiente = VentaPendienteEntity(
            codigo_local = request.codigo_local,
            id_usuario = request.id_usuario,
            fecha = request.fecha,
            detalles_json = Gson().toJson(request.detalles),
            total = total
        )
        val appContext = requireContext().applicationContext

        Thread {
            DonPepeRoomDatabase.getDatabase(appContext).ventaPendienteDao().insertar(pendiente)
            Handler(Looper.getMainLooper()).post {
                Carrito.vaciar()
                codigoLocal = null
                if (_binding != null) {
                    refrescar()
                    Toast.makeText(
                        appContext,
                        "Sin conexión: venta guardada en el celular. Se enviará al sincronizar.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}