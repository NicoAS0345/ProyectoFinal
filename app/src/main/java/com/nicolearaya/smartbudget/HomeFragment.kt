package com.nicolearaya.smartbudget.ui.home  // Ajusta el paquete según tu estructura

import GastosAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.nicolearaya.smartbudget.databinding.FragmentHomeBinding
import com.nicolearaya.smartbudget.R
import com.nicolearaya.smartbudget.viewmodel.GastosViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GastosViewModel by viewModels()

    private val adapter = GastosAdapter { gasto ->
        Toast.makeText(requireContext(), "Clic en: ${gasto.nombreGasto}", Toast.LENGTH_SHORT).show()
    }


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

        // 2️⃣ Configura el RecyclerView PRIMERO
        binding.recyclerGastos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
            setHasFixedSize(true) // Optimización
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allItems.collect { gastos ->
                adapter.submitList(gastos)
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Limpiar binding para evitar leaks de memoria
    }


}