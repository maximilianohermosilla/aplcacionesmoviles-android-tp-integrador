package com.example.android_tp_integrador.placeholder

import java.util.ArrayList
import java.util.HashMap

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
        // Add some sample items.
//        for (i in 1..COUNT) {
//            addItem(createPlaceholderItem(i))
//        }
    }

    private fun addItem(item: PlaceholderItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

//    private fun createPlaceholderItem(position: Int): PlaceholderItem {
//        return PlaceholderItem(position.toString(), "Item " + position, makeDetails(position))
//    }
//
//    private fun makeDetails(position: Int): String {
//        val builder = StringBuilder()
//        builder.append("Details about Item: ").append(position)
//        for (i in 0..position - 1) {
//            builder.append("\nMore details information here.")
//        }
//        return builder.toString()
//    }

    /**
     * A placeholder item representing a piece of content.
     */
    data class PlaceholderItem(val id: String, val title: String, val description: String, val dateCreation: String?, val dateResolution: String?, val state: String?
                               , val priority: String?, val ubication: String?, val type: String?, val images: List<Any>?, val states: List<Any>?, val comments: List<Any>?) {
        override fun toString(): String = title
    }
}