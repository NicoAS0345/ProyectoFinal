package com.nicolearaya.smartbudget.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class LoginViewModel @Inject constructor(private val auth:FirebaseAuth): ViewModel()
{

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _currentUser = mutableStateOf<FirebaseUser?>(null)
    val currentUser: State<FirebaseUser?> = _currentUser

    //Es como un constructor
    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun login(email: String, password: String) {
        Log.d("Login", "Intentando login con: $email")
        _uiState.value = AuthUiState.Loading
        Log.d("FIREBASE", "Iniciando autenticación...")

        if (!isValidEmail(email)) {
            _uiState.value = AuthUiState.Error("Por favor ingresa un email válido")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FIREBASE", "Login exitoso: ${auth.currentUser?.email}")
                    _uiState.value = AuthUiState.Success(auth.currentUser)
                } else {
                    Log.e("FIREBASE", "Error en login: ${task.exception?.message}")
                    _uiState.value = AuthUiState.Error(task.exception?.message ?: "Error desconocido")
                }
            }

    }


    fun register(email: String, password: String) {
        _uiState.value = AuthUiState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = AuthUiState.Success(auth.currentUser)
                } else {
                    _uiState.value = AuthUiState.Error(
                        task.exception?.message ?: "Error en el registro"
                    )
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    fun logout() {
        auth.signOut()
        _uiState.value = AuthUiState.Initial
    }

    fun resetError() {
        _uiState.value = AuthUiState.Initial
    }

    sealed class AuthUiState {
        object Initial : AuthUiState()
        object Loading : AuthUiState()
        data class Success(val user: FirebaseUser?) : AuthUiState()
        data class Error(val message: String) : AuthUiState()
    }

}

