package com.example.android_tp_integrador

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android_tp_integrador.placeholder.PlaceholderContent;
import com.example.android_tp_integrador.databinding.FragmentDenunciaListBinding
import com.example.android_tp_integrador.databinding.DenunciaListContentBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class DenunciaListFragment : Fragment() {

    val db = FirebaseFirestore.getInstance();
    private lateinit var stateSpinner: Spinner
    lateinit var userId: String
    lateinit var userRole: String
    var state: String = ""

    private val unhandledKeyEventListenerCompat =
        ViewCompat.OnUnhandledKeyEventListenerCompat { v, event ->
            if (event.keyCode == KeyEvent.KEYCODE_Z && event.isCtrlPressed) {
                Toast.makeText(
                    v.context,
                    "Undo (Ctrl + Z) shortcut triggered",
                    Toast.LENGTH_LONG
                ).show()
                true
            } else if (event.keyCode == KeyEvent.KEYCODE_F && event.isCtrlPressed) {
                Toast.makeText(
                    v.context,
                    "Find (Ctrl + F) shortcut triggered",
                    Toast.LENGTH_LONG
                ).show()
                true
            }
            false
        }

    private var _binding: FragmentDenunciaListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        userId = prefs.getString("id", null).toString()
        userRole = prefs.getString("role", null).toString()

        _binding = FragmentDenunciaListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.addOnUnhandledKeyEventListener(view, unhandledKeyEventListenerCompat)

        val recyclerView: RecyclerView = binding.denunciaList

        // Leaving this not using view binding as it relies on if the view is visible the current
        // layout configuration (layout, layout-sw600dp)
        val itemDetailFragmentContainer: View? =
            view.findViewById(R.id.denuncia_detail_nav_container)

        val volverButton: Button = view.findViewById<View>(R.id.volverButton) as Button
        volverButton.setOnClickListener {
            //parentFragmentManager.popBackStack()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav_list
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    println("Home presionado")
                    startActivity(Intent(getActivity(), HomeActivity::class.java))
                    true
                }
                R.id.nav_list -> {
                    println("nav_list presionado")
                    startActivity(Intent(getActivity(), DenunciaDetailHostActivity::class.java))
                    true
                }
                R.id.nav_notification -> {
                    println("nav_notification presionado")
                    startActivity(Intent(getActivity(), NuevaDenunciaActivity::class.java))
                    true
                }
                R.id.nav_user -> {
                    println("nav_user presionado")
                    startActivity(Intent(activity, ProfileActivity::class.java))
//                    if (this !is NotificationsActivity) {
//                        startActivity(Intent(this, NotificationsActivity::class.java))
//                    }
                    true
                }
                else -> false
            }
        }

        // Configurar listener de selección
        stateSpinner = view.findViewById(R.id.stateSpinner)
        val states = listOf("", "Pendiente", "Asignado", "Finalizado")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stateSpinner.adapter = adapter

        stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedState = states[position]
                setupRecyclerView(recyclerView, itemDetailFragmentContainer, selectedState)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejo opcional cuando no se selecciona nada
            }
        }

        setupRecyclerView(recyclerView, itemDetailFragmentContainer, state)
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        itemDetailFragmentContainer: View?,
        state: String
    ) {
        if(userRole == "Protector"){
            queryProtector(recyclerView, itemDetailFragmentContainer, state);
        }else{
            queryDenunciante(recyclerView, itemDetailFragmentContainer, state);
        }
    }

    fun queryDenunciante(recyclerView: RecyclerView, itemDetailFragmentContainer: View?, state: String){
        if(state == ""){
            println("Filtro por denunciante")
            db.collection("denuncias")
                .whereEqualTo("userCreation", userId)
                //.orderBy("dateCreation", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Aquí obtienes una lista de documentos que cumplen con la condición
                    val denunciasList = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(PlaceholderContent.PlaceholderItem::class.java)
                    }

                    // Procesa la lista de denuncias
                    println("Documentos encontrados: ${denunciasList.size}")
                    recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                        denunciasList, itemDetailFragmentContainer, this.requireContext(), userRole, userId
                    )
                }
                .addOnFailureListener { exception ->
                    println("Error al obtener las denuncias: ${exception.message}")
                }
        }
        else{
            println("Filtro por estado y rol denunciante")
            db.collection("denuncias")
                .whereEqualTo("userCreation", userId)
                .whereEqualTo("state", state)
                //.orderBy("dateCreation", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Aquí obtienes una lista de documentos que cumplen con la condición
                    val denunciasList = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(PlaceholderContent.PlaceholderItem::class.java)
                    }

                    // Procesa la lista de denuncias
                    println("Documentos encontrados: ${denunciasList.size}")
                    recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                        denunciasList, itemDetailFragmentContainer, this.requireContext(), userRole, userId
                    )
                }
                .addOnFailureListener { exception ->
                    println("Error al obtener las denuncias: ${exception.message}")
                }
        }

    }

    fun queryProtector(recyclerView: RecyclerView, itemDetailFragmentContainer: View?, state: String){
        val results = mutableListOf<PlaceholderContent.PlaceholderItem>()

        if(state == ""){
            println("Filtro por rol protector")
            db.collection("denuncias")
                .whereEqualTo("userAsignation", userId)
                //.orderBy("dateCreation", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Aquí obtienes una lista de documentos que cumplen con la condición
                    val denunciasAsignadasList = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(PlaceholderContent.PlaceholderItem::class.java)
                    }
                    println("Documentos asignados encontrados: ${denunciasAsignadasList.size}")
                    results.addAll(denunciasAsignadasList)

                    db.collection("denuncias")
                        .whereEqualTo("state", "Pendiente")
                        //.orderBy("dateCreation", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val denunciasPendientesList = querySnapshot.documents.mapNotNull { document ->
                                document.toObject(PlaceholderContent.PlaceholderItem::class.java)
                            }
                            println("Documentos pendientes encontrados: ${denunciasPendientesList.size}")
                            results.addAll(denunciasPendientesList)

                            // Elimina duplicados si es necesario
                            println("Documentos total: ${results.size}")
                            val uniqueResults = results.distinctBy { it.id }
                            println("Documentos a renderizar: ${uniqueResults.size}")
                            recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                                uniqueResults, itemDetailFragmentContainer, this.requireContext(), userRole, userId,
                            )
                        }
                }
                .addOnFailureListener { exception ->
                    println("Error al obtener las denuncias: ${exception.message}")
                }
        }
        else if(state == "Pendiente") {
            db.collection("denuncias")
                .whereEqualTo("state", "Pendiente")
                //.orderBy("dateCreation", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val denunciasPendientesList = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(PlaceholderContent.PlaceholderItem::class.java)
                    }
                    println("Documentos pendientes encontrados: ${denunciasPendientesList.size}")

                    recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                        denunciasPendientesList, itemDetailFragmentContainer, this.requireContext(), userRole, userId,
                    )
                }
        }
        else{
            println("Filtro por estado y rol protector")
            db.collection("denuncias")
                .whereEqualTo("userAsignation", userId)
                .whereEqualTo("state", state)
                //.orderBy("dateCreation", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Aquí obtienes una lista de documentos que cumplen con la condición
                    val denunciasList = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(PlaceholderContent.PlaceholderItem::class.java)
                    }

                    // Procesa la lista de denuncias
                    println("Documentos encontrados: ${denunciasList.size}")
                    recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                        denunciasList, itemDetailFragmentContainer, this.requireContext(), userRole, userId
                    )
                }
                .addOnFailureListener { exception ->
                    println("Error al obtener las denuncias: ${exception.message}")
                }
        }

    }

    class SimpleItemRecyclerViewAdapter(
        private val values: List<PlaceholderContent.PlaceholderItem>,
        private val itemDetailFragmentContainer: View?,
        private val context: Context,
        private val userRole: String,
        private val userId: String,
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val binding = DenunciaListContentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)

        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val db = FirebaseFirestore.getInstance();
            val priorityText = context.getString(R.string.priorityText)

            val item = values[position]
            holder.textState.text = item.state
            holder.textDate.text = item.dateCreation
            holder.textTitle.text = item.title
            holder.textPriority.text = priorityText + ": " + item.priority

            if(userRole == "Protector"){
                holder.deleteButton.visibility = View.GONE
                holder.editButton.visibility = View.GONE
            }

            if(item.state == "Asignado"){
                holder.layoutCard.setBackgroundResource(R.drawable.rounded_button_primary_dark)
                holder.deleteButton.visibility = View.GONE
                holder.editButton.visibility = View.GONE
                holder.logoState.setImageResource(R.drawable.icon_timelapse_light);
            }

            if(item.state == "Finalizado"){
                holder.layoutCard.setBackgroundResource(R.drawable.rounded_button_aside)
                holder.deleteButton.visibility = View.GONE
                holder.editButton.visibility = View.GONE
                holder.logoState.setImageResource(R.drawable.icon_check_light);
            }

            holder.deleteButton.setOnClickListener {
                showConfirmationDialog(
                    context = context,
                    message = "¿Estás seguro que quieres eliminar la denuncia?"
                ) { isConfirmed ->
                    if (isConfirmed) {
                        if(userId != "") {
                            val docRef = db.collection("denuncias").document(item.id)

                            docRef.delete()
                                .addOnSuccessListener {
                                    // Documento eliminado exitosamente
                                    Log.d(
                                        "Firestore",
                                        "Documento con ID $item.id eliminado exitosamente"
                                    )

                                    val intent = Intent(context, DenunciaDetailHostActivity::class.java)
                                    context.startActivity(intent)
                                }
                                .addOnFailureListener { e ->
                                    // Error al eliminar el documento
                                    Log.e("Firestore", "Error al eliminar documento", e)
                                }
                        }
                    } else {}
                }
            }

            holder.editButton.setOnClickListener {
                val intent = Intent(context, NuevaDenunciaActivity::class.java)
                intent.putExtra("uuid", item.id)
                context.startActivity(intent)
            }

            with(holder.itemView) {
                tag = item
                setOnClickListener { itemView ->
                    //val item = itemView.tag as PlaceholderContent.PlaceholderItem
                    val bundle = Bundle()
                    bundle.putString(
                        DenunciaDetailFragment.ARG_ITEM_ID,
                        item.id
                    )
                    if (itemDetailFragmentContainer != null) {
                        itemDetailFragmentContainer.findNavController()
                            .navigate(R.id.fragment_denuncia_detail, bundle)
                    } else {
                        itemView.findNavController().navigate(R.id.show_denuncia_detail, bundle)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    /**
                     * Context click listener to handle Right click events
                     * from mice and trackpad input to provide a more native
                     * experience on larger screen devices
                     */
                    setOnContextClickListener { v ->
                        val item = v.tag as PlaceholderContent.PlaceholderItem
                        Toast.makeText(
                            v.context,
                            "Context click of item " + item.id,
                            Toast.LENGTH_LONG
                        ).show()
                        true
                    }
                }

                setOnLongClickListener { v ->
                    // Setting the item id as the clip data so that the drop target is able to
                    // identify the id of the content
                    val clipItem = ClipData.Item(item.id)
                    val dragData = ClipData(
                        v.tag as? CharSequence,
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        clipItem
                    )

                    if (Build.VERSION.SDK_INT >= 24) {
                        v.startDragAndDrop(
                            dragData,
                            View.DragShadowBuilder(v),
                            null,
                            0
                        )
                    } else {
                        v.startDrag(
                            dragData,
                            View.DragShadowBuilder(v),
                            null,
                            0
                        )
                    }
                }
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(binding: DenunciaListContentBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val layoutCard: LinearLayout = binding.layoutCard
            val textState: TextView = binding.textState
            val textDate: TextView = binding.textDate
            val textTitle: TextView = binding.textTitle
            val textPriority: TextView = binding.textPriority
            val deleteButton: Button = binding.deleteButton
            val editButton: Button = binding.editButton
            val logoState: ImageView = binding.logoState
        }

        fun showConfirmationDialog(
            context: Context,
            message: String,
            onResult: (Boolean) -> Unit
        ) {
            val dialog = AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Sí") { _, _ ->
                    onResult(true)
                }
                .setNegativeButton("No") { _, _ ->
                    onResult(false)
                }
                .create()

            dialog.show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}