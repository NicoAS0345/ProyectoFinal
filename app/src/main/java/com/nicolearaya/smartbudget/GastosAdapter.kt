import android.util.Log
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
import com.nicolearaya.smartbudget.model.GastosFirebase


class GastosAdapter(private val onEditClick: (GastosFirebase) -> Unit,
                    private val onDeleteClick: (GastosFirebase) -> Unit,
                    private val onItemClick: (GastosFirebase) -> Unit) :
    //Verifica los datos de la lista para indicarlos en las cards
    ListAdapter<GastosFirebase, GastosAdapter.GastosViewHolder>(DiffCallback()) {


    inner class GastosViewHolder(private val binding: ItemGastoCardBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(gasto: GastosFirebase) {
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
        val gasto = getItem(position)
        Log.d("AdapterDebug", "Mostrando gasto: ${gasto.nombreGasto}")
        holder.bind(gasto)
    }

    // DiffUtil para comparar listas de manera eficiente
    class DiffCallback : DiffUtil.ItemCallback<GastosFirebase>() {
        override fun areItemsTheSame(oldItem: GastosFirebase, newItem: GastosFirebase) =
            oldItem.id == newItem.id // Comparar por ID de Firebase

        override fun areContentsTheSame(oldItem: GastosFirebase, newItem: GastosFirebase) =
            oldItem == newItem
    }
}