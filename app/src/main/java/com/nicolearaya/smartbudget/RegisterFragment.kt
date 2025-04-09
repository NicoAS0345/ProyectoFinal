package com.nicolearaya.smartbudget.ui.auth

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nicolearaya.smartbudget.R
import com.nicolearaya.smartbudget.databinding.FragmentRegisterBinding
import com.nicolearaya.smartbudget.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

// Fragmento para el registro de nuevos usuarios
@AndroidEntryPoint // Hilt para inyección de dependencias
class RegisterFragment : Fragment() {
    // Binding para vistas
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // ViewModel para manejar la lógica de autenticación
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout del fragmento
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura listeners y observadores cuando la vista está creada
        setupListeners()
        setupObservers()
    }

    // Configura los listeners de los botones
    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            // Obtiene los valores de los campos de texto
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // Valida los inputs antes de registrar
            if (validateInputs(email, password, confirmPassword)) {
                viewModel.register(email, password) // Llama al ViewModel para registrar
            }
        }
    }

    //Valida los campos de entrada del formulario
    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Validación de email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email inválido"
            isValid = false
        }

        // Validación de contraseña (mínimo 6 caracteres)
        if (password.length < 6) {
            binding.etPassword.error = "Mínimo 6 caracteres"
            isValid = false
        }

        // Validación de coincidencia de contraseñas
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Las contraseñas no coinciden"
            isValid = false
        }

        return isValid
    }

    // Configura los observadores del ViewModel
    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                Log.d("OBSERVER", "Estado actualizado: $state") // Log para depuración

                when (state) {
                    is LoginViewModel.AuthUiState.Loading -> {
                        // Muestra progreso y deshabilita botón durante carga
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnRegister.isEnabled = false
                    }
                    is LoginViewModel.AuthUiState.Success -> {
                        Log.d("OBSERVER", "Redireccionando a Home")
                        binding.progressBar.visibility = View.GONE
                        // Navega al login después de registro exitoso
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    is LoginViewModel.AuthUiState.Error -> {
                        // Muestra error y reactiva el botón
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        binding.tvError.text = state.message
                        binding.tvError.visibility = View.VISIBLE
                    }
                    LoginViewModel.AuthUiState.Initial -> {
                        // Estado inicial: oculta progreso y errores
                        binding.progressBar.visibility = View.GONE
                        binding.tvError.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpia el binding para evitar leaks de memoria
    }
}