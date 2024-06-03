package com.example.virtualtryon

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class RecyclerViewAdapterForSavedImages (
    private val fragment: SaveFragment,
    private val imageList: List<Models>,
    var callback:(Int)->Unit
) : RecyclerView.Adapter<RecyclerViewAdapterForSavedImages.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_image_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d("downloadURL", "Size : ${imageList.size}")
        Picasso.get().load(imageList[position].image).into(holder.ivImage)

        holder.cardView.setOnClickListener {
            Toast.makeText(fragment.requireContext(), imageList[position].title, Toast.LENGTH_LONG).show()
            callback(position)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }
}