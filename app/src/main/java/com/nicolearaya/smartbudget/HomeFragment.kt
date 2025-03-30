package com.nicolearaya.smartbudget.ui.home  // Ajusta el paquete según tu estructura

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nicolearaya.smartbudget.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        // Configuración inicial del Fragment
        setupUI()
    }

    private fun setupUI() {


        // Aquí puedes cargar datos (ej: lista de gastos desde ViewModel)
    }

    private fun getUserName(): String {
        // Lógica para obtener el nombre del usuario (ej: SharedPreferences)
        return "Usuario"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Limpiar binding para evitar leaks de memoria
    }

    override fun onPause() {
        super.onPause()
        view?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        view?.visibility = View.VISIBLE
    }
}