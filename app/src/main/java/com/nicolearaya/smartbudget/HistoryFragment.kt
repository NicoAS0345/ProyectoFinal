package com.nicolearaya.smartbudget.ui.history

import GastosAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nicolearaya.smartbudget.databinding.FragmentHistoryBinding
import com.nicolearaya.smartbudget.viewmodel.GastosViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.os.bundleOf
import com.nicolearaya.smartbudget.model.GastosFirebase
import com.nicolearaya.smartbudget.R

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GastosViewModel by viewModels()

    private val adapter = GastosAdapter(
        onEditClick = { gasto ->
            findNavController().navigate(
                R.id.action_historyFragment_to_editExpenseFragment,
                bundleOf("gasto" to gasto)
            )
        },
        onDeleteClick = { gasto ->
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar gasto")
                .setMessage("¿Eliminar ${gasto.nombreGasto}?")
                .setPositiveButton("Eliminar") { _, _ -> viewModel.delete(gasto) }
                .setNegativeButton("Cancelar", null)
                .show()
        },
        onItemClick = { gasto ->
            Toast.makeText(requireContext(), gasto.nombreGasto, Toast.LENGTH_SHORT).show()
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHistory.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getGastosGroupedByMonth().collect { groupedGastos ->
                    adapter.submitList(groupedGastos)
                }
            }
        }
    }

    private fun groupGastosByMonthYear(gastos: List<GastosFirebase>): List<Any> {
        return gastos
            .groupBy { gasto ->
                val calendar = Calendar.getInstance().apply {
                    time = gasto.fechaCreacion.toDate()
                }
                "${getMonthName(calendar.get(Calendar.MONTH))} ${calendar.get(Calendar.YEAR)}"
            }
            .flatMap { (monthYear, gastos) ->
                listOf(monthYear) + gastos.sortedByDescending { it.fechaCreacion }
            }
    }

    private fun getMonthName(month: Int): String {
        return SimpleDateFormat("MMMM", Locale.getDefault())
            .format(Calendar.getInstance().apply { set(Calendar.MONTH, month) }.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}