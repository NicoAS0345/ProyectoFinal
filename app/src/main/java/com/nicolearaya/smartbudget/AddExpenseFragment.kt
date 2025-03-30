package com.nicolearaya.smartbudget.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nicolearaya.smartbudget.databinding.FragmentAddExpenseBinding
import com.nicolearaya.smartbudget.model.Gastos
import com.nicolearaya.smartbudget.viewmodel.GastosViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GastosViewModel by viewModels()

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
        binding.btnSaveExpense.setOnClickListener {
            guardarGasto()
        }
    }

    private fun guardarGasto() {
        val nombre = binding.nombreGasto.text.toString()
        val monto = binding.montoGasto.text.toString().toDoubleOrNull() ?: 0.0
        val descripcion = binding.descripcionGasto.text.toString()
        val categoria = binding.categoriaGasto.text.toString()
        val fecha = binding.fechaGasto.text.toString()

        if (nombre.isNotEmpty() && monto > 0) {
            val nuevoGasto = Gastos(
                nombreGasto = nombre,
                descripcion = descripcion,
                categoria = categoria, // Puedes agregar un spinner para categorías
                monto = monto,
                fecha = fecha
            )

            viewModel.insert(nuevoGasto)
            findNavController().popBackStack() // Regresa al fragment anterior
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}