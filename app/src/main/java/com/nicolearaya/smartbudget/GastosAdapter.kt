import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nicolearaya.smartbudget.DateUtils
import com.nicolearaya.smartbudget.model.Gastos
import com.nicolearaya.smartbudget.R
import com.nicolearaya.smartbudget.databinding.ItemGastoCardBinding
import com.nicolearaya.smartbudget.model.GastosFirebase

// Adaptador para mostrar una lista de gastos en un RecyclerView
class GastosAdapter(private val onEditClick: (GastosFirebase) -> Unit,
                    private val onDeleteClick: (GastosFirebase) -> Unit,
                    private val onItemClick: (GastosFirebase) -> Unit) :
    //Verifica los datos de la lista para indicarlos en las cards
     ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(header: String) {
            (itemView as TextView).text = header
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is String -> TYPE_HEADER
            else -> TYPE_ITEM
        }
    }

    //ViewHolder que representa cada item de gasto en el RecyclerView.
    inner class GastosViewHolder(private val binding: ItemGastoCardBinding) : RecyclerView.ViewHolder(binding.root) {

        // Vincula los datos del gasto con las vistas de la tarjeta.
        fun bind(gasto: GastosFirebase) {
            binding.apply {
                cardTitle.text = gasto.nombreGasto

                //Muestra la fecha
                val fechaFormateada = DateUtils.formatTimestampForDisplay(gasto.fechaCreacion)


                // Agrega la categoría si está disponible
                val descripcion = "$${gasto.monto} - $fechaFormateada" +
                        (gasto.categoria?.let { " - $it" } ?: "")
                cardDescription.text = descripcion

                root.setOnClickListener { onItemClick(gasto) }
                btnEdit.setOnClickListener { onEditClick(gasto) }
                btnDelete.setOnClickListener { onDeleteClick(gasto) }
            }
        }
    }

    // Crea nuevos ViewHolders cuando el RecyclerView los necesita.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_month_header, parent, false)
            )
        } else {
            GastosViewHolder(
                ItemGastoCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as String)
            is GastosViewHolder -> holder.bind(getItem(position) as GastosFirebase)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is String && newItem is String -> oldItem == newItem
                oldItem is GastosFirebase && newItem is GastosFirebase -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }
}


