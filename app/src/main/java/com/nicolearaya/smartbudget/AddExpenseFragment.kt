package com.nicolearaya.smartbudget.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nicolearaya.smartbudget.PantallaPrincipalActivity
import com.nicolearaya.smartbudget.databinding.FragmentAddExpenseBinding
import com.nicolearaya.smartbudget.model.Gastos
import com.nicolearaya.smartbudget.ui.home.HomeFragment
import com.nicolearaya.smartbudget.viewmodel.GastosViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddExpenseFragment : Fragment() {
    // Binding para acceder a las vistas del layout
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    // ViewModel para manejar operaciones de gastos (inyectado con Hilt)
    private val viewModel: GastosViewModel by viewModels()

    //Le inserta el layout a la vista para que se pueda ver la interfaz grafica
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        //Le dice al boton de guardar que hace cuando se le da click
        binding.btnSaveExpense.setOnClickListener {
            guardarGasto()
        }
    }

    private fun guardarGasto() {
        // Valida campos y crea un nuevo objeto Gastos
        val nombre = binding.nombreGasto.text.toString()
        val monto = binding.montoGasto.text.toString().toDoubleOrNull() ?: 0.0
        val descripcion = binding.descripcionGasto.text.toString()
        val categoria = binding.categoriaGasto.text.toString()
        val fecha = binding.fechaGasto.text.toString()

        //Llena el modelo
        if (nombre.isNotEmpty() && monto > 0) {
            val nuevoGasto = Gastos(
                nombreGasto = nombre,
                descripcion = descripcion,
                categoria = categoria,
                monto = monto,
                fecha = fecha
            )

            viewModel.insert(nuevoGasto)
            findNavController().popBackStack() // Regresa al fragment anterior
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    // Controla la visibilidad del FAB en la actividad principal

    override fun onResume() {
        super.onResume()
        (activity as? PantallaPrincipalActivity)?.showHideFab(false)
    }

    override fun onPause() {
        (activity as? PantallaPrincipalActivity)?.showHideFab(true)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}