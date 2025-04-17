package com.nicolearaya.smartbudget.ui.budget

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.nicolearaya.smartbudget.databinding.FragmentBudgetBinding
import com.nicolearaya.smartbudget.model.Budget
import com.nicolearaya.smartbudget.viewmodel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.budget.collect { budget ->
                    budget?.let { updateUI(it) }
                    Log.d("BudgetFragment", "Budget actualizado: ") // Para debug
                }
            }
        }

        binding.btnSetBudget.setOnClickListener {
            showSetBudgetDialog()
        }

        binding.btnResetBudget.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    private fun updateUI(budget: Budget) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

        binding.tvMonthlyBudget.text = currencyFormat.format(budget.monthlyBudget)
        binding.tvCurrentSpending.text = currencyFormat.format(budget.currentSpending)

        val remaining = budget.monthlyBudget - budget.currentSpending
        binding.tvRemainingBudget.text = currencyFormat.format(remaining)

        // Establece colores (opcional)
        binding.tvRemainingBudget.setTextColor(
            if (remaining < 0) resources.getColor(android.R.color.holo_red_dark)
            else resources.getColor(android.R.color.holo_green_dark)
        )

        // Actualiza el progreso (si lo usas)
        binding.progressBudget.progress = calculateProgress(budget)
        binding.tvPercentage.text = "${calculateProgress(budget)}%"
    }

    private fun calculateProgress(budget: Budget): Int {
        return if (budget.monthlyBudget > 0) {
            ((budget.currentSpending / budget.monthlyBudget) * 100).toInt()
        } else {
            0
        }
    }

    private fun showSetBudgetDialog() {
        val inputEditText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Ej: 5000.00"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Establecer presupuesto mensual")
            .setMessage("Ingresa tu presupuesto mensual:")
            .setView(inputEditText)
            .setPositiveButton("Guardar") { dialog, _ ->
                val inputText = inputEditText.text.toString()
                val newBudget = inputText.toDoubleOrNull() ?: 0.0

                if (newBudget > 0) {
                    viewModel.updateBudget(newBudget) // Eliminamos el userId como parámetro
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ingresa un valor válido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    //Para reiniciar los montos
    private fun showResetConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reiniciar Presupuesto")
            .setMessage("¿Estás seguro que deseas reiniciar tu presupuesto?")
            .setPositiveButton("Reiniciar") { dialog, _ ->
                Log.d("BudgetFragment", "Iniciando reinicio...")
                viewModel.resetBudget()
                Toast.makeText(requireContext(), "Presupuesto reiniciado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}