package com.example.android_tp_integrador

import SliderAdapter
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.example.android_tp_integrador.placeholder.PlaceholderContent
import com.example.android_tp_integrador.databinding.FragmentDenunciaDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.json.JSONObject
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import java.util.UUID


class DenunciaDetailFragment : Fragment(), OnMapReadyCallback {

    private var item: PlaceholderContent.PlaceholderItem? = null
    val db = FirebaseFirestore.getInstance();

    lateinit var id: String
    var userId: String = ""
    var userName: String = ""
    var userRole: String = ""
    var userToken: String? = ""
    private val bearerToken = "ya29.c.c0ASRK0GbanyGSRAwwPm9-emKejnIR-zovT7DF4tZLsrITXOxB3PPFTuPERcWZJRjPFvnIW119SYWrKgn5OXuc8RHwLxtsj_oOlLFHGYRz6a71OGjCs44obpNiJphqsMwLqYDp5cxZle4DSEfu9Eyl56tqU3pRqn5mQ8F4qaWVeDq6wp-dL6Y5axp4t7rN3cvBKw9DZzkg-QV_0VaWHO6r-_MHj6w2CTYcpMCXR6XbkZrioBDOSqG-u009TR9-tlNq-jMKNWdVs1P5ekkQ9nYNrgSotZeL-UApcH4VIL0jOuSweRNrmLr4AvgkhMB_NCLzokcMqhYwoJdfyMmAIcMbYp9uTIMw4Ve46sIv1fYYq2e3lZIa6v81A4A1T385C1qk5g4tRqs6gFw4cfMvakgRXgBa4tpJfRdvcenW8dx_Bo55gpd0y_wnaJ9MUjnarMk1w6ld_Q5uvQm5cunntySzRj0tjfSib5Sdr_9Qjlr-eJm452x-495Zv2v0X9vbaBVpOknZz12ka41eMv0s6iek6k-2XpwS7fwVflqURrJbk95-xR6WX6Vj03WZ3r6b-bsSx1e2rv8Zg7scFiti9fIvbOyQU49Rb3hl4z49WFRj8syMVv9tyYkhbeUQyJjeYF_6t13Y-jqRqst55OUsw7hci_IzBxmvqWO25S_rw_wss3nfOScsc_Iugk_tRleFz4b44u7pUsfX27iVFs7voyO68UJvckbq4ZaYO0e8c-nYuy_9WypsJvj3iYQJxvmbBcXmk_xvt0tct3SdIewlxSkvg7fsS1Z-Y7nSXl-lbkwp9ev26w121phgRaFzf3ioeYeheq-rvyXXe0J1xOf32QVR7twsVhqzuypF3lBSk8dRVtX8Y8l-tmaIrhX2JVcWqhwiYY8FMdaOXjvi53rd2m---F2lstFp9IUwYZ3ujbm10s76Yn1Oj5xwFwRuY1i04pixIZSgklMXogpWou_9nZUiuVkBv4yaz7Fqo6c_574JQlUm42zwj5FQdX1"

    private val commentsList: MutableList<PlaceholderContent.Comment> = ArrayList()

    lateinit var itemTitleTextView: TextView
    lateinit var itemDateTextView: TextView
    lateinit var itemIdTextView: TextView
    lateinit var itemDescriptionTextView: TextView
    lateinit var assignedUser: TextView
    lateinit var ubicationLabel: TextView
    lateinit var viewPager: ViewPager2
    lateinit var logoImage: ImageView
    private var toolbarLayout: CollapsingToolbarLayout? = null
    private var mapFragment: View? = null
    lateinit var assignButton: Button
    lateinit var finishButton: Button
    lateinit var chatContainer: LinearLayout
    lateinit var recyclerView: RecyclerView
    lateinit var commentEditText: EditText
    lateinit var saveButton: Button

    private var _binding: FragmentDenunciaDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private lateinit var location: LatLng

    private val dragListener = View.OnDragListener { v, event ->
        if (event.action == DragEvent.ACTION_DROP) {
            val clipDataItem: ClipData.Item = event.clipData.getItemAt(0)
            val dragData = clipDataItem.text
            item = PlaceholderContent.ITEM_MAP[dragData]
            updateContent()
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                id = it.getString(ARG_ITEM_ID).toString()
            }
        }
        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        userRole = sharedPreferences.getString("role", null).toString()
        userId = sharedPreferences.getString("id", null).toString()
        userName = sharedPreferences.getString("name", "").toString() + " " +  sharedPreferences.getString("lastname", "").toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDenunciaDetailBinding.inflate(inflater, container, false)
        val rootView = binding.root

        toolbarLayout = binding.toolbarLayout
        itemTitleTextView = binding.denunciaTitle!!
        itemDateTextView = binding.denunciaDate!!
        itemIdTextView = binding.denunciaId!!
        ubicationLabel = binding.ubicationLabel!!
        itemDescriptionTextView = binding.denunciaDescription!!
        assignedUser = binding.assignedUser!!
        viewPager = binding.viewPager!!
        logoImage = binding.logoImage!!
        mapFragment = rootView.findViewById<View>(R.id.mapFragment)!!
        assignButton = binding.assignButton!!
        finishButton = binding.finishButton!!
        chatContainer = binding.chatContainer!!
        recyclerView = binding.commentsRecyclerView!!
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        commentEditText = binding.commentEditText!!
        saveButton = binding.saveButton!!

        updateContent()
        rootView.setOnDragListener(dragListener)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        assignButton.setOnClickListener {
            showConfirmationDialog(
                context = requireContext(),
                message = "¿Estás seguro que quieres asignarte la denuncia?"
            ) { isConfirmed ->
                if (isConfirmed) {
                    if(userId != ""){
                        db.collection("denuncias").document(id).set(hashMapOf(
                            "userAsignation" to userId.toString(),
                            "state" to "Asignado"
                        ), SetOptions.merge())
                    }
                    assignedUser.text = "Asignado: \n" + userName
                    assignButton.visibility = View.GONE
                    chatContainer.visibility = View.VISIBLE
                    sendNotificationByComplaintID(id, "Tu denuncia ya está siendo atendida por un responsable!", "$userName es el protector asignado")
                } else {}
            }
        }

        finishButton.setOnClickListener {
            showConfirmationDialog(
                context = requireContext(),
                message = "¿Estás seguro que desea finalizar la denuncia?"
            ) { isConfirmed ->
                if (isConfirmed) {
                    if(userId != ""){
                        db.collection("denuncias").document(id).set(hashMapOf(
                            "state" to "Finalizado"
                        ), SetOptions.merge())
                    }
                    finishButton.visibility = View.GONE
                    sendNotificationByComplaintID(id, "Tu denuncia ha sido finalizada", "")

                    startActivity(Intent(getActivity(), DenunciaDetailHostActivity::class.java))
                } else {}
            }
        }

        saveButton.setOnClickListener {
            val uuid: UUID = UUID.randomUUID();
            val fechaActual = LocalDateTime.now()
            val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val fechaFormateada = fechaActual.format(formato)

            if(userId != ""){
                var comment: PlaceholderContent.Comment =
                    PlaceholderContent.Comment(uuid.toString(),fechaFormateada, userId, userName,
                        commentEditText.text.toString()
                    )
                commentsList.add(comment)
                db.collection("denuncias").document(id).set(hashMapOf(
                    "comments" to commentsList
                ), SetOptions.merge())
            }

            commentEditText.setText("")
            recyclerView.adapter = CommentsAdapter(commentsList)
        }

        return rootView
    }

    @SuppressLint("SetTextI18n")
    private fun updateContent() {
        val title: String = getString(R.string.textLogo)
        val descriptionText: String = getString(R.string.descriptionText)
        val dateText: String = getString(R.string.dateText)

        toolbarLayout?.title = title //"Protección Animal" //item?.title
        db.collection("denuncias")
            .document(id.toString())
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Convierte el documento a un objeto PlaceholderItem
                    val denuncia =
                        documentSnapshot.toObject(PlaceholderContent.PlaceholderItem::class.java)

                    // Procesa el objeto obtenido
                    if (denuncia != null) {
                        println("Denuncia encontrada: $denuncia")

                        item = denuncia
                        item?.let {
                            itemTitleTextView.text = it.title
                            itemDateTextView.text = dateText + ": " + it.dateCreation
                            itemIdTextView.text = it.state
                            itemDescriptionTextView.text = descriptionText + ": \n\n" + it.description

                            println(it.userAsignation)
                            if(userRole == "Protector" && it.userAsignation == ""){
                                println("Es protector y no tiene asignacion")
                                assignButton.visibility = View.VISIBLE
                            }
                            else{
                                println("No es protector o esta asignado")
                                lifecycleScope.launch {
                                    val user = obtenerUsuarioPorId(it.userAsignation.toString())
                                    if (user != null) {
                                        assignedUser.text = "Asignado: \n" + user.name + " " + user.lastname
                                    } else {
                                        println("Usuario no encontrado.")
                                    }
                                }
                                assignButton.visibility = View.GONE
                            }

                            if(it.userCreation == userId || it.userAsignation == userId){
                                chatContainer.visibility = View.VISIBLE
                                finishButton.visibility = View.VISIBLE
                            }
                            else{
                                chatContainer.visibility = View.GONE
                            }

                            if(it.state == "Finalizado"){
                                finishButton.visibility = View.GONE
                            }

                            if(it.images != null && it.images.isNotEmpty()) {
                                val adapter = SliderAdapter(it.images)
                                viewPager.adapter = adapter
                                viewPager.visibility = View.VISIBLE;
                                logoImage.visibility = View.GONE;
                            }
                            else{
                                viewPager.visibility = View.GONE
                                logoImage.visibility = View.VISIBLE;
                            }

                            if (it.ubication != null) {
                                // Usa la ubicación para centrar el mapa
                                val location: String = it.ubication.toString().replace("\"", "");
                                val latitude = location.split(",").first()
                                val longitude = location.split(",").last()
                                val currentLatLng = LatLng(latitude.toDouble(), longitude.toDouble())
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                                googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Ubicación actual"))
                                mapFragment!!.visibility = View.VISIBLE
                                ubicationLabel!!.visibility = View.VISIBLE
                            }
                            else{
                                mapFragment!!.visibility = View.GONE
                                ubicationLabel!!.visibility = View.GONE
                            }

                            if(it.comments != null && it.comments.isNotEmpty()) {

                                commentsList.addAll(it.comments)
                                recyclerView.adapter = CommentsAdapter(commentsList)
                            }
//                            else{
//                                viewPager.visibility = View.GONE
//                                logoImage.visibility = View.VISIBLE;
//                            }

                        }
                    } else {
                        println("No se pudo convertir el documento a PlaceholderItem.")
                    }
                }
                else {
                    println("No se encontró ningún documento con el ID especificado.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error al obtener las denuncias: ${exception.message}")
            }
        // Show the placeholder content as text in a TextView.

    }

    private fun sendNotificationByComplaintID(complaintID: String, title: String? = "", body: String?  = "") {
        db.collection("denuncias")
            .document(complaintID)
            .get()
            .addOnSuccessListener { complaint ->
                if (complaint != null && complaint.exists()) {
                    val userID = complaint.getString("userCreation")

                    if (userID != null) {
                        db.collection("users")
                            .document(userID)
                            .get()
                            .addOnSuccessListener { user ->
                                if (user != null && user.exists()) {
                                    val userToken = user.getString("token")
                                    //val userName = user.getString("name")

                                    if (userToken != null) {
                                        sendNotification(userToken, userName, title, body)
                                    } else {
                                        Log.e("Firebase", "El token del usuario no está disponible.")
                                    }
                                } else {
                                    Log.e("Firebase", "No se encontró el usuario con ID: $userID.")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Firebase", "Error al obtener el usuario: ${exception.message}")
                            }
                    } else {
                        Log.e("Firebase", "No se encontró el ID del usuario en la denuncia.")
                    }
                } else {
                    Log.e("Firebase", "No se encontró la denuncia con ID: $complaintID.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error al obtener la denuncia: ${exception.message}")
            }
    }

    private fun sendNotification(token: String?, name: String?, title: String? = "", body: String?  = "") {
        val fcmUrl = "https://fcm.googleapis.com/v1/projects/62704752521/messages:send"

        val notificationPayload = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", token)
                put("data", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                })
            })
        }

        // Creación solicitud HTTP
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,
            fcmUrl,
            notificationPayload,
            Response.Listener { response ->
                Log.d("FCM", "Notificación enviada con éxito: $response")
            },
            Response.ErrorListener { error ->
                Log.e("FCM", "Error al enviar la notificación: ${error.message}")
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $bearerToken"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // Enviar la solicitud
        val requestQueue: RequestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(jsonObjectRequest)
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

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(map: GoogleMap) {
        println("Inicializando onMapReady")
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    suspend fun obtenerUsuarioPorId(id: String): PlaceholderContent.UserItem? {
        return try {
            val documentSnapshot = db.collection("users")
                .document(id)
                .get()
                .await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject( PlaceholderContent.UserItem::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error al obtener el usuario: ${e.message}")
            null
        }
    }
}

class CommentsAdapter(private val comments: List<PlaceholderContent.Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val userTextView: TextView = itemView.findViewById(R.id.userTextView)
        val commentTextView: TextView = itemView.findViewById(R.id.commentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.dateTextView.text = comment.dateTime
        holder.userTextView.text = comment.userName
        holder.commentTextView.text = comment.comment
    }

    override fun getItemCount(): Int = comments.size
}