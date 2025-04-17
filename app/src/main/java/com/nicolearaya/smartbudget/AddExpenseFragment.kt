package com.nicolearaya.smartbudget.ui.expenses

import android.os.Bundle
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
import com.nicolearaya.smartbudget.databinding.FragmentAddExpenseBinding
import com.nicolearaya.smartbudget.model.Gastos
import com.nicolearaya.smartbudget.model.GastosFirebase
import com.nicolearaya.smartbudget.ui.home.HomeFragment
import com.nicolearaya.smartbudget.viewmodel.BudgetViewModel
import com.nicolearaya.smartbudget.viewmodel.GastosViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddExpenseFragment : Fragment() {
    // Binding para acceder a las vistas del layout
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    // ViewModel para manejar operaciones de gastos (inyectado con Hilt)
    private val viewModel: GastosViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()

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
        // Configura el selector de categorías
        setupCategorySelector()

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

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // 1. Verificar ANTES de insertar
                    val willExceed = budgetViewModel.checkAndShowExceeded(amount = monto,
                        currentMonthGastos = viewModel.gastos.value )

                    //Lo trae de Firebase ya no de la base de datos local
                    val nuevoGasto = GastosFirebase(
                        nombreGasto = nombre,
                        descripcion = descripcion,
                        categoria = categoria,
                        monto = monto
                    )

                    //Si la categoria viene vacia entonces que la agregue sin categoria
                    if (nuevoGasto.categoria.isNullOrEmpty()) {
                        nuevoGasto.categoria = "Sin categoría"
                    }

                    // 1. Insertar el gasto primero
                    viewModel.insert(nuevoGasto)


                    // 3. Mostrar diálogo si es necesario
                    if (willExceed) {
                        showBudgetExceededDialog()
                    }

                    findNavController().popBackStack()

                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    //Funciones para las categorias y que funcionen para el usuario
    private fun showCategoryDialog() {
        val currentCategory = binding.categoriaGasto.text.toString()
        val categorias = viewModel.categoriasPredeterminadas.toMutableList()
        var selectedCategory = categorias.firstOrNull { it == currentCategory } ?: ""

        /*MaterialAlertDialogBuilder(requireContext())
            .setTitle("Seleccionar categoría")
            .setSingleChoiceItems(categorias.toTypedArray(), categorias.indexOf(selectedCategory)) { dialog, which ->
                selectedCategory = categorias[which]
            }
            .setPositiveButton("Aceptar") { dialog, _ ->
                if (selectedCategory == "Otros") {
                    showCustomCategoryDialog()
                } else {
                    binding.categoriaGasto.setText(selectedCategory)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()*/
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

    private fun setupCategorySelector() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_item,
            viewModel.categoriasPredeterminadas
        )

        binding.categoriaGasto.setAdapter(adapter)
        binding.categoriaGasto.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position)
            if (selected == "Otros") {
                showCustomCategoryDialog()
            }
        }

        // Para evitar que el usuario escriba directamente (excepto en "Otros")
        binding.categoriaGasto.keyListener = null
        binding.categoriaGasto.setOnClickListener {
            showCategoryDialog()
        }
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