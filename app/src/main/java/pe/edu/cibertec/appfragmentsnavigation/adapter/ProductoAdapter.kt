package pe.edu.cibertec.appfragmentsnavigation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pe.edu.cibertec.appfragmentsnavigation.databinding.ItemProductoBinding
import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.Producto

class ProductoAdapter(
    private val onAgregar: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

    private var lista: List<Producto> = emptyList()

    inner class ViewHolder(val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun actualizarLista(nuevaLista: List<Producto>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductoAdapter.ViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoAdapter.ViewHolder, position: Int) {
        val producto = lista[position]
        holder.binding.tvnombre.text = producto.nombre
        holder.binding.tvcategoria.text = producto.categoria
        holder.binding.tvprecio.text = "S/ " + String.format("%.2f", producto.precio)
        holder.binding.tvstock.text = "Stock: " + producto.stock
        holder.binding.btnagregar.isEnabled = producto.stock > 0
        holder.binding.btnagregar.setOnClickListener {
            onAgregar(producto)
        }
    }

    override fun getItemCount(): Int {
        return lista.size
    }
}