package com.example.android_tp_integrador.placeholder

import android.content.Context
import android.content.SharedPreferences
import com.example.android_tp_integrador.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList
import java.util.HashMap
import com.google.android.gms.maps.model.LatLng

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<PlaceholderItem> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, PlaceholderItem> = HashMap()

    private val COUNT = 25

    init {
        addItem(PlaceholderItem("#001", "Perro abandonado sin comida", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut vitae  accumsan sem, non fermentum libero. Mauris ex nisi, lobortis eu ipsum  id, finibus dictum leo. ", "22/10/2024", null, "Nuevo", "Media", null, "Perro", null, null, null));
        addItem(PlaceholderItem("#002", "Caballo utilizado para transporte de carro", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut vitae  accumsan sem, non fermentum libero. Mauris ex nisi, lobortis eu ipsum  id, finibus dictum leo. ", "27/10/2024", null, "Asignado", "Baja", null, "Caballo", null, null, null));
        addItem(PlaceholderItem("#003", "Comida con veneno en plaza pública", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut vitae  accumsan sem, non fermentum libero. Mauris ex nisi, lobortis eu ipsum  id, finibus dictum leo.", "28/09/2024", "29/10/2024", "Resuelto", "Alta", null, null, null, null, null));
    }

    private fun addItem(item: PlaceholderItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    /**
     * A placeholder item representing a piece of content.
     */
    data class PlaceholderItem(val id: String = "",
                               val title: String = "",
                               val description: String = "",
                               val dateCreation: String? = "",
                               val dateResolution: String? = "",
                               val state: String? = "",
                               val priority: String? = "",
                               val ubication: Any? = null,
                               val category: String? = "",
                               val address: String? = "",
                               val addressCity: String? = "",
                               val userCreation: String? = "",
                               val userAsignation: String? = "",
                               val images: List<String>? = null,
                               val states: List<Any>? = null,
                               val comments: List<Comment>? = null) {
        override fun toString(): String = title
    }

    data class UserItem(val id: String = "",
                               val email: String = "",
                               val name: String = "",
                               val lastname: String? = "",
                               val phone: String? = "",
                               val password: String? = "",
                               val token: String? = "",
                               val role: String? = ""){
        override fun toString(): String = email
    }

    data class Comment(
        val id: String = "",
        val dateTime: String = "",
        val userId: String = "",
        val userName: String = "",
        val comment: String = ""
    )
}