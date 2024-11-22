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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.json.JSONObject
import android.app.AlertDialog
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class DenunciaDetailFragment : Fragment(), OnMapReadyCallback {

    private var item: PlaceholderContent.PlaceholderItem? = null
    val db = FirebaseFirestore.getInstance();

    lateinit var id: String
    var userId: String = ""
    var userRole: String = ""
    var userToken: String? = ""
    private val bearerToken = "ya29.c.c0ASRK0GY6Ksg_I8M2wLA-DuXvd2EeE-NR9WKAuP42dNQucineo4ru55qL7-BNekiOZi6IZZfU8lKYaPQCcGnqjS9-MQcvEE9ssHggdvBtDWzMOuOc3gwoyjKLYjbSGcFY4UwmIHixo_y8CyWM6uoNldwH43ITIWsPZ7gz6ivsu3lpRwkRFNAfTlGOOlpM7fLIe5zQ5k55gRoL_Kd2ePx40HBs0KbXucvq4c9333ndDEx6UrhiD-oLk93LESAChXoPBen0H_cpD8QOj4qwaWUHU3ijrJn45T4IiFI8VLc9YdT3iYCYT5DFMJjo5eE6e4SBfDCkdWFEAGDxtebiqAY0T5NY5BYhYkh4i6GuLkCjmlFjCw1N4mLVbNUL384KujuI1f7943a05rafSv857cWgS7ya5-YF_zs9Uu3-YS1e6_ihvXIQXan9mzI6pzM2a3j1ruue3kUUmZQOcpbhFgoJWUr1pFFStuXchWJMiQcir9Zp0x3zrI0g2B2mv5Is7F0QfdQJrVRv0sshrFiI8OY5pqQ4UO3k49gsak51O97gy--ZOIi_WSa-InxXpwoe1z16IO1V5MqBWJaRdZtrjpkkh3Xqxgu6ZeVUxsg5lFUmin8l6oySRXR-Wt3MwpvgWjV15onxbVvy_rkVuiMStiI1z9ZecIkFi7MpBc5zJrtYOzgxbWspJg9nc9XeyYfUejhrBVahUSY_yVjnFF4vB1ic2coU8WX2lknYgMxnR0ot-7dfFf3R2WU0xv358jeoU7r0YnooFQzd6Z-Igyu0qh1ZyksM8htiIi-dxu405MMIluc_SnxSRd6qg-wOsnMQWM_oo65_VFbFYsczZ1cyzmZihfuIr72BOMYgjO4nFp224ROpW_tcxWc-b5tfaVkc4aMmczi4tJsbqo3Ri4Q8QdZQs9uuzb-Mjal-FWyRb2r9o3Vwxo0RpeVcZ7IWmzfW8B50hnfcj-qqphRQQcQfnMtRSfMg-dYXiR-304Zfmulh7l_S-6xqZwqWzd6"

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
                //item = PlaceholderContent.ITEM_MAP[it.getString(ARG_ITEM_ID)]
            }
        }
        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        userRole = sharedPreferences.getString("role", null).toString()
        userId = sharedPreferences.getString("id", null).toString()
    }

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
                    assignedUser.text = userId ?: "Sin asignar"
                    assignButton.visibility = View.GONE
                    sendNotificationByComplaintID(id)
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
                    sendNotificationByComplaintID(id)

                    startActivity(Intent(getActivity(), DenunciaDetailHostActivity::class.java))
                } else {}
            }
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
                        //var userId = denuncia.userCreation
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
                                logoImage.visibility = View.INVISIBLE;
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

    private fun sendNotificationByComplaintID(complaintID: String) {
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
                                    val userName = user.getString("name")

                                    if (userToken != null) {
                                        sendNotification(userToken, userName)
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

    private fun sendNotification(token: String?, name: String?) {
        val fcmUrl = "https://fcm.googleapis.com/v1/projects/62704752521/messages:send"

        val notificationPayload = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", token)
                put("data", JSONObject().apply {
                    put("title", "Tu denuncia ya está siendo atendida por un responsable!")
                    put("body", "$name es el protecto asignado")
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