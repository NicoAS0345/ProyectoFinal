package com.nicolearaya.smartbudget.ui.expenses

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nicolearaya.smartbudget.PantallaPrincipalActivity
import com.nicolearaya.smartbudget.R
import com.nicolearaya.smartbudget.databinding.FragmentEditExpenseBinding
import com.nicolearaya.smartbudget.model.Gastos
import com.nicolearaya.smartbudget.model.GastosFirebase
import com.nicolearaya.smartbudget.viewmodel.BudgetViewModel
import com.nicolearaya.smartbudget.viewmodel.GastosViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditExpenseFragment : Fragment() {
    private var _binding: FragmentEditExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GastosViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()
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

            setupCategorySelector()

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
                    categoria = categoriaGasto.text.toString()
                )


                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        // 1. Calcular diferencia de montos
                        val montoOriginal = currentGasto.monto
                        val diferencia = updatedGasto.monto - montoOriginal

                        // 2. Actualizar gasto (existente)
                        viewModel.update(updatedGasto)

                        // 3. Verificar presupuesto (nuevo)
                        budgetViewModel.budget.value?.let { budget ->
                            if ((budget.currentSpending + diferencia) > budget.monthlyBudget) {
                                showBudgetExceededDialog()
                            }
                        }

                        Toast.makeText(requireContext(), "Gasto actualizado", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    private fun setupCategorySelector() {
        val currentCategory = binding.categoriaGasto.text.toString()
        val categorias = viewModel.categoriasPredeterminadas.toMutableList()

        // Asegúrate de que la categoría actual esté en la lista del dropdown
        if (currentCategory.isNotEmpty() && !categorias.contains(currentCategory)) {
            categorias.add(0, currentCategory)
        }

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_item,
            categorias
        )

        binding.categoriaGasto.setAdapter(adapter)

        // Postergar el setText a cuando la vista ya esté completamente renderizada
        binding.categoriaGasto.post {
            binding.categoriaGasto.setText(currentCategory, false)
        }

        // Mostrar el menú al hacer click
        binding.categoriaGasto.setOnClickListener {
            binding.categoriaGasto.showDropDown()
        }

        binding.categoriaGasto.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position)
            if (selected == "Otros") {
                showCustomCategoryDialog()
            }
        }
    }

    private fun showCustomCategoryDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Ingresa tu categoría"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nueva categoría")
            .setView(input)
            .setPositiveButton("Guardar") { dialog, _ ->
                val customCategory = input.text.toString().trim()
                if (customCategory.isNotEmpty()) {
                    binding.categoriaGasto.setText(customCategory)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    //Mesaje sobre el presupuesto
    private fun showBudgetExceededDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¡Presupuesto excedido!")
            .setMessage("Esta modificación ha excedido tu presupuesto mensual.")
            .setPositiveButton("Entendido", null)
            .show()
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