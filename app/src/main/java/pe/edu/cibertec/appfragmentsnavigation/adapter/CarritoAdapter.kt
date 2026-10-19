package pe.edu.cibertec.appfragmentsnavigation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pe.edu.cibertec.appfragmentsnavigation.carrito.ItemCarrito
import pe.edu.cibertec.appfragmentsnavigation.databinding.ItemCarritoBinding

class CarritoAdapter(
    private val onMas: (ItemCarrito) -> Unit,
    private val onMenos: (ItemCarrito) -> Unit,
    private val onQuitar: (ItemCarrito) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.ViewHolder>() {

    private var lista: List<ItemCarrito> = emptyList()

    inner class ViewHolder(val binding: ItemCarritoBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun actualizarLista(nuevaLista: List<ItemCarrito>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarritoAdapter.ViewHolder {
        val binding = ItemCarritoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarritoAdapter.ViewHolder, position: Int) {
        val item = lista[position]
        val subtotal = item.producto.precio * item.cantidad
        holder.binding.tvnombre.text = item.producto.nombre
        holder.binding.tvprecio.text = "S/ " + String.format("%.2f", item.producto.precio) + " c/u"
        holder.binding.tvcantidad.text = item.cantidad.toString()
        holder.binding.tvsubtotal.text = "S/ " + String.format("%.2f", subtotal)
        holder.binding.btnmas.setOnClickListener { onMas(item) }
        holder.binding.btnmenos.setOnClickListener { onMenos(item) }
        holder.binding.btnquitar.setOnClickListener { onQuitar(item) }
    }

    override fun getItemCount(): Int {
        return lista.size
    }
}