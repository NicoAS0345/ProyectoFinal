import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nicolearaya.smartbudget.model.Gastos
import com.nicolearaya.smartbudget.R
import com.nicolearaya.smartbudget.databinding.ItemGastoCardBinding


class GastosAdapter(private val onEditClick: (Gastos) -> Unit,
                    private val onDeleteClick: (Gastos) -> Unit,
                    private val onItemClick: (Gastos) -> Unit) :
    //Verifica los datos de la lista para indicarlos en las cards
    ListAdapter<Gastos, GastosAdapter.GastosViewHolder>(DiffCallback()) {


    inner class GastosViewHolder(private val binding: ItemGastoCardBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(gasto: Gastos) {
                binding.apply {
                    cardTitle.text = gasto.nombreGasto
                    cardDescription.text = "$${gasto.monto} - ${gasto.fecha}"

                    // Listeners para botones de editar/eliminar y clic en la tarjeta
                    root.setOnClickListener { onItemClick(gasto) }
                    btnEdit.setOnClickListener { onEditClick(gasto) }
                    btnDelete.setOnClickListener { onDeleteClick(gasto) }
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastosViewHolder {
        val binding = ItemGastoCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GastosViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GastosViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // DiffUtil para comparar listas de manera eficiente
    class DiffCallback : DiffUtil.ItemCallback<Gastos>() {
        override fun areItemsTheSame(oldItem: Gastos, newItem: Gastos) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Gastos, newItem: Gastos) =
            oldItem == newItem
    }
}