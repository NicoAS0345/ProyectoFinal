package com.nicolearaya.smartbudget.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nicolearaya.smartbudget.databinding.FragmentAddExpenseBinding

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

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
            saveExpense()
        }
    }

    private fun saveExpense() {
        val name = binding.etExpenseName.text.toString()
        val amount = binding.etExpenseAmount.text.toString()

        if (name.isNotEmpty() && amount.isNotEmpty()) {
            // Aquí iría la lógica para guardar el gasto (ViewModel, base de datos, etc.)
            // Por ahora solo mostramos un mensaje en consola
            println("Gasto guardado: $name - $amount")

            // Opcional: regresar al fragment anterior después de guardar
            parentFragmentManager.popBackStack()
        } else {
            // Mostrar error si los campos están vacíos
            println("Por favor completa todos los campos")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}