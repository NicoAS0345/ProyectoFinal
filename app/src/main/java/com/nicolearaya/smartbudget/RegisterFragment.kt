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

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (validateInputs(email, password, confirmPassword)) {
                viewModel.register(email, password)
            }
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email inválido"
            isValid = false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Mínimo 6 caracteres"
            isValid = false
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Las contraseñas no coinciden"
            isValid = false
        }

        return isValid
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                Log.d("OBSERVER", "Estado actualizado: $state") // Debug
                when (state) {
                    is LoginViewModel.AuthUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnRegister.isEnabled = false
                    }
                    is LoginViewModel.AuthUiState.Success -> {
                        Log.d("OBSERVER", "Redireccionando a Home")
                        binding.progressBar.visibility = View.GONE
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    is LoginViewModel.AuthUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.isEnabled = true
                        binding.tvError.text = state.message
                        binding.tvError.visibility = View.VISIBLE
                    }
                    LoginViewModel.AuthUiState.Initial -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvError.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}