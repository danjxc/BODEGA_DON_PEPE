package pe.edu.cibertec.appfragmentsnavigation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pe.edu.cibertec.appfragmentsnavigation.databinding.ItemCategoriaBinding
import pe.edu.cibertec.appfragmentsnavigation.retrofit.response.Categoria

class CategoriaAdapter
    : RecyclerView.Adapter<CategoriaAdapter.ViewHolder>() {

    private var lista: List<Categoria> = emptyList()

    inner class ViewHolder(val binding: ItemCategoriaBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun actualizarLista(nuevaLista: List<Categoria>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoriaAdapter.ViewHolder {
        val binding = ItemCategoriaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriaAdapter.ViewHolder, position: Int) {
        holder.binding.tvnombre.text = lista[position].nombre
    }

    override fun getItemCount(): Int {
        return lista.size
    }
}