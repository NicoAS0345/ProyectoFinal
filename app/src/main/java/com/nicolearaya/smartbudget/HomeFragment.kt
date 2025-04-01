package com.nicolearaya.smartbudget.ui.home  // Ajusta el paquete según tu estructura

import GastosAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.nicolearaya.smartbudget.databinding.FragmentHomeBinding
import com.nicolearaya.smartbudget.R
import com.nicolearaya.smartbudget.viewmodel.GastosViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.app.AlertDialog
import com.nicolearaya.smartbudget.PantallaPrincipalActivity


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GastosViewModel by viewModels()

    private val adapter = GastosAdapter(
        // Navega al EditExpenseFragment con el gasto seleccionado
        onEditClick = { gasto ->
            val bundle = Bundle().apply {
                putSerializable("gasto", gasto)
            }
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToEditExpenseFragment(gasto))
        },
        //Indica que pasa cuando se le dal icono del basurero
        onDeleteClick = { gasto ->
            // Muestra diálogo de confirmación para eliminar
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar gasto")
                .setMessage("¿Estás seguro de eliminar ${gasto.nombreGasto}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.delete(gasto)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        },
        onItemClick = { gasto ->
            // Aquí puedes agregar acción al hacer click en la tarjeta si es necesario
            Toast.makeText(requireContext(), "Seleccionado: ${gasto.nombreGasto}", Toast.LENGTH_SHORT).show()
        }
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura RecyclerView y observa cambios en la lista de gastos
        binding.recyclerGastos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
            setHasFixedSize(true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allItems.collect { gastos ->
                adapter.submitList(gastos)
            }
        }

    }

    //Maneja la visibilidad del boton de agregar
    override fun onResume() {
        super.onResume()
        (activity as? PantallaPrincipalActivity)?.showHideFab(true)
    }

    override fun onPause() {
        (activity as? PantallaPrincipalActivity)?.showHideFab(false)
        super.onPause()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Limpiar binding para evitar leaks de memoria
    }


}