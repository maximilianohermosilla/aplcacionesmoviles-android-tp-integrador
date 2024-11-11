package com.example.android_tp_integrador

import android.content.ClipData
import android.os.Bundle
import android.view.DragEvent
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.android_tp_integrador.DenunciaListFragment.SimpleItemRecyclerViewAdapter
import com.example.android_tp_integrador.placeholder.PlaceholderContent
import com.example.android_tp_integrador.databinding.FragmentDenunciaDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A fragment representing a single Denuncia detail screen.
 * This fragment is either contained in a [DenunciaListFragment]
 * in two-pane mode (on larger screen devices) or self-contained
 * on handsets.
 */
class DenunciaDetailFragment : Fragment() {

    /**
     * The placeholder content this fragment is presenting.
     */
    private var item: PlaceholderContent.PlaceholderItem? = null

    lateinit var id: String

    lateinit var itemTitleTextView: TextView
    lateinit var itemDateTextView: TextView
    lateinit var itemIdTextView: TextView
    lateinit var itemDescriptionTextView: TextView
    private var toolbarLayout: CollapsingToolbarLayout? = null

    private var _binding: FragmentDenunciaDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
        itemDescriptionTextView = binding.denunciaDescription!!

        updateContent()
        rootView.setOnDragListener(dragListener)

        return rootView
    }

    private fun updateContent() {
        toolbarLayout?.title = "Protección Animal" //item?.title
        val db = FirebaseFirestore.getInstance();
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
                            itemDateTextView.text = "Fecha: " + it.dateCreation
                            itemIdTextView.text = it.id
                            itemDescriptionTextView.text = "Descripción: \n\n" + it.description
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
}