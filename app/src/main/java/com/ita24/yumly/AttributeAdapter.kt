package com.ita24.yumly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class AttributeAdapter(private val items: List<AttributeItem>) : RecyclerView.Adapter<AttributeAdapter.ViewHolder>() {

    // Holds the currently selected attribute names
    val selectedAttributes = mutableSetOf<String>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.attributeImageView)
        val overlay: View = view.findViewById(R.id.selectionOverlay)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = items[position]
                    item.isSelected = !item.isSelected
                    notifyItemChanged(position)

                    if (item.isSelected) {
                        selectedAttributes.add(item.name)
                    } else {
                        selectedAttributes.remove(item.name)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attribute, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.imageView.setImageResource(item.drawableId)

        if (item.isSelected) {
            holder.overlay.visibility = View.VISIBLE
        } else {
            holder.overlay.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size
}
