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
import com.nicolearaya.smartbudget.dataFirebase.Gastos_Firebase
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
                viewLifecycleOwner.lifecycleScope.launch {
                    try {

                        // Validación básica
                        if (nombreGasto.text.isNullOrEmpty() || montoGasto.text.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "Nombre y monto son obligatorios", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val nuevoMonto = binding.montoGasto.text.toString().toDouble()
                        val diferencia = nuevoMonto - currentGasto.monto


                        // Crea una copia actualizada del gasto
                        val updatedGasto = currentGasto.copy(
                            nombreGasto = nombreGasto.text.toString(),
                            //monto = montoGasto.text.toString().toDoubleOrNull() ?: 0.0,
                            monto = nuevoMonto,
                            descripcion = descripcionGasto.text.toString(),
                            categoria = categoriaGasto.text.toString()
                        )

                        // Verificar exceso de presupuesto si hay aumento
                        if (diferencia > 0) {
                            val willExceed = budgetViewModel.checkAndShowExceeded(
                                amount = diferencia,
                                currentMonthGastos = viewModel.gastos.value
                            )
                            if (willExceed) {
                                showBudgetExceededDialog()
                            }
                        }

                        // 1. Actualizar el gasto primero
                        viewModel.update(updatedGasto).join()//esto es para que el sistema tenga tiempo de terminar la operación
                        Log.d("Updategastos1", "Gasto actualizado ${updatedGasto}" )


                        // Si no excede el presupuesto
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
    private fun showBudgetExceededDialog(onDismiss: () -> Unit = {}) {
        budgetViewModel.budget.value?.let { budget ->
            val exceso = budget.currentSpending - budget.monthlyBudget
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("¡Presupuesto excedido!")
                .setMessage(
                    "Has superado tu presupuesto mensual por $${String.format("%.2f", exceso)}. " +
                            "Presupuesto: $${String.format("%.2f", budget.monthlyBudget)}\n" +
                            "Gastado: $${String.format("%.2f", budget.currentSpending)}"
                )
                .setPositiveButton("Entendido") { dialog, _ ->
                    dialog.dismiss()
                    onDismiss() // Ejecuta la callback al cerrar
                }
                .setCancelable(false) // Obliga al usuario a presionar el botón
                .show()
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