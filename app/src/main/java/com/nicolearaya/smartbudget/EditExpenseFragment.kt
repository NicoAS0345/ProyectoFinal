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
import com.nicolearaya.smartbudget.databinding.FragmentEditExpenseBinding
import com.nicolearaya.smartbudget.model.Gastos
import com.nicolearaya.smartbudget.model.GastosFirebase
import com.nicolearaya.smartbudget.viewmodel.GastosViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditExpenseFragment : Fragment() {
    private var _binding: FragmentEditExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GastosViewModel by viewModels()
    private lateinit var currentGasto: GastosFirebase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtiene el gasto desde los argumentos

        currentGasto = arguments?.getParcelable<GastosFirebase>("gasto") ?: run {
            Toast.makeText(requireContext(), "Error: No se encontró el gasto", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()// Navega de regreso si no hay datos
            return
        }

        setupUI()
    }

    // Rellena los campos con los datos del gasto
    private fun setupUI() {
        with(binding) {
            nombreGasto.setText(currentGasto.nombreGasto)
            montoGasto.setText(currentGasto.monto.toString())
            descripcionGasto.setText(currentGasto.descripcion)
            categoriaGasto.setText(currentGasto.categoria)
            fechaGasto.setText(currentGasto.fecha)

            btnSaveEditExpense.setOnClickListener {
                // Validación básica
                if (nombreGasto.text.isNullOrEmpty() || montoGasto.text.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Nombre y monto son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Crea una copia actualizada del gasto
                val updatedGasto = currentGasto.copy(
                    nombreGasto = nombreGasto.text.toString(),
                    monto = montoGasto.text.toString().toDoubleOrNull() ?: 0.0,
                    descripcion = descripcionGasto.text.toString(),
                    categoria = categoriaGasto.text.toString(),
                    fecha = fechaGasto.text.toString()
                )

                viewModel.update(updatedGasto) // Actualiza en la base de datos
                Toast.makeText(requireContext(), "Gasto actualizado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Regresa al fragmento anterior
            }
        }
    }

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