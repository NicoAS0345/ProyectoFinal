package com.nicolearaya.smartbudget.ui.home  // Ajusta el paquete según tu estructura

import GastosAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.nicolearaya.smartbudget.databinding.FragmentHomeBinding
import com.nicolearaya.smartbudget.R
import com.nicolearaya.smartbudget.viewmodel.GastosViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.app.AlertDialog
import android.content.Context
import android.icu.text.NumberFormat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.search.SearchView
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.firestore.firestore
import com.google.firebase.options
import com.nicolearaya.smartbudget.DateUtils
import com.nicolearaya.smartbudget.PantallaPrincipalActivity
import com.nicolearaya.smartbudget.model.Budget
import com.nicolearaya.smartbudget.model.GastosFirebase
import com.nicolearaya.smartbudget.viewmodel.BudgetViewModel


//Constantes para no volver a mostrar el dialogo de no molestar
private const val PREFS_NAME = "BudgetPrefs"
private const val KEY_DONT_SHOW_ALERT = "dont_show_budget_alert"


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GastosViewModel by viewModels()
    private val budgetviewmodel: BudgetViewModel by viewModels()
    private var gastosOriginales: List<GastosFirebase> = emptyList()
    private var categoriasSeleccionadas: MutableSet<String> = mutableSetOf()


    private val adapter = GastosAdapter(
        // Navega al EditExpenseFragment con el gasto seleccionado
        onEditClick = { gasto ->
            val bundle = Bundle().apply {
                putParcelable("gasto", gasto)
            }
            findNavController().navigate(
                R.id.action_homeFragment_to_editExpenseFragment,bundle)
        },
        //Indica que pasa cuando se le dal icono del basurero
        onDeleteClick = { gasto ->
            // Muestra diálogo de confirmación para eliminar
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar gasto")
                .setMessage("¿Estás seguro de eliminar ${gasto.nombreGasto}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.delete(gasto)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        },
        onItemClick = { gasto ->
            // Aquí puedes agregar acción al hacer click en la tarjeta si es necesario
            Toast.makeText(requireContext(), "Seleccionado: ${gasto.nombreGasto}", Toast.LENGTH_SHORT).show()
        }
    )


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
        setupSearch()
        setupFilterButton()


        // Configura RecyclerView y observa cambios en la lista de gastos
        binding.recyclerGastos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
            setHasFixedSize(true)
        }

        // Configura el botón de cerrar sesión
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.tvWelcome.text = "¡Bienvenido a Smart Budget! (${DateUtils.getCurrentMonthYear()})"

        //revisa los cambios que han sucedido en el recycler view
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gastos.collect { gastos ->
                    Log.d("HomeFragment", "Total gastos a mostrar: ${gastos.size}")
                    if (gastos.isEmpty()) {
                        Log.d("HomeFragment", "La lista de gastos está vacía")
                    } else {
                        gastos.forEach { gasto ->
                            Log.d("HomeFragment", "Mostrando: ${gasto.nombreGasto}")
                        }
                    }
                    gastosOriginales = gastos

                    // Actualiza tu RecyclerView aquí
                    adapter.submitList(gastos)
                }
            }
        }

        //Esta checkeando el presupuesto
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                budgetviewmodel.budget.collect { budget ->
                    budget?.let {
                        checkBudgetExceeded(it)
                    }
                }
            }
        }


    }

    private fun setupSearch() {
        binding.Busqueda.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterGastos(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.Busqueda.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun filterGastos(query: String) {
        Log.d("BUSQUEDA", "Buscando: '$query'")
        Log.d("BUSQUEDA", "Total gastos: ${gastosOriginales.size}")

        val filtered = gastosOriginales.filter { gasto ->
            val matchesSearch = query.isEmpty() || gasto.nombreGasto.contains(query, true)
            val matchesCategories = categoriasSeleccionadas.isEmpty() ||
                    (gasto.categoria?.let { categoriasSeleccionadas.contains(it) } ?: false)

            matchesSearch && matchesCategories
        }

        adapter.submitList(filtered)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.Busqueda.windowToken, 0)
    }

    //Funcion para poder cerrar la sesión actiba
    private fun showLogoutConfirmationDialog() {
        //muestra mensaje de si esta seguro de cerrar la sesión
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres salir?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.signOut()
                navigateToLogin()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun navigateToLogin() {
        // Navega al login eliminando el back stack
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    //Funciones para manejar el filtro por categoria
    private fun setupFilterButton() {
        binding.filtro.setOnClickListener {
            showCategoryFilterDialog()
        }
    }
    private fun showCategoryFilterDialog() {
        val categorias = viewModel.categoriasPredeterminadas.toTypedArray()
        val checkedItems = BooleanArray(categorias.size) { index ->
            categoriasSeleccionadas.contains(categorias[index])
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar por categorías")
            .setMultiChoiceItems(categorias, checkedItems) { _, which, isChecked ->
                val categoria = categorias[which]
                if (isChecked) {
                    categoriasSeleccionadas.add(categoria)
                } else {
                    categoriasSeleccionadas.remove(categoria)
                }
            }
            .setPositiveButton("Aplicar") { _, _ ->
                applyCategoryFilter()
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Limpiar") { _, _ ->
                categoriasSeleccionadas.clear()
                applyCategoryFilter()
            }
            .show()
    }

    private fun applyCategoryFilter() {
        val query = binding.Busqueda.text.toString()

        val filtered = gastosOriginales.filter { gasto ->
            val matchesSearch = query.isEmpty() || gasto.nombreGasto.contains(query, true)
            val matchesCategories = categoriasSeleccionadas.isEmpty() ||
                    (gasto.categoria?.let { categoriasSeleccionadas.contains(it) } ?: false)

            matchesSearch && matchesCategories
        }

        adapter.submitList(filtered)

        // Mostrar feedback al usuario
        val filterStatus = if (categoriasSeleccionadas.isEmpty()) {
            "Mostrando todos los gastos"
        } else {
            "Filtrado por: ${categoriasSeleccionadas.joinToString(", ")}"
        }
        Toast.makeText(requireContext(), filterStatus, Toast.LENGTH_SHORT).show()
    }

    //Para verificar cambios en el presupuesto
    private fun checkBudgetExceeded(budget: Budget) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alertDisabled = prefs.getBoolean(KEY_DONT_SHOW_ALERT, false)

        if (!alertDisabled && budget.monthlyBudget > 0 && budget.currentSpending > budget.monthlyBudget) {
            val exceso = budget.currentSpending - budget.monthlyBudget
            showBudgetAlert(
                "¡Presupuesto excedido!",
                "Has superado tu presupuesto por ${NumberFormat.getCurrencyInstance().format(exceso)}"
            )
        }

        if (budget.currentSpending <= budget.monthlyBudget) {
            prefs.edit().putBoolean(KEY_DONT_SHOW_ALERT, false).apply()
        }

    }

    private fun showBudgetAlert(title: String, message: String) {
        if (!isAdded || isHidden) return

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_DONT_SHOW_ALERT, false)) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_budget_alert, null)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setView(dialogView)
            .setBackgroundInsetTop(0)  // Elimina el padding superior del diálogo
            .setBackgroundInsetBottom(0) // Elimina el padding inferior
            .setPositiveButton("Entendido") { dialog, _ ->
                if (dialogView.findViewById<CheckBox>(R.id.cb_dont_show_again).isChecked) {
                    prefs.edit().putBoolean(KEY_DONT_SHOW_ALERT, true).apply()
                }
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }


    //Maneja la visibilidad del boton de agregar
    override fun onResume() {
        super.onResume()
        (activity as? PantallaPrincipalActivity)?.showHideFab(true)
    }

    override fun onPause() {
        (activity as? PantallaPrincipalActivity)?.showHideFab(false)
        super.onPause()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Limpiar binding para evitar leaks de memoria
    }

    //Funciones para el teclado
    // Extensiones para manejo del teclado
    fun Fragment.showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun Fragment.hideKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }



}