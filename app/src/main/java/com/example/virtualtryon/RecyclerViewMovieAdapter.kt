package com.example.virtualtryon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class RecyclerViewMovieAdapter constructor(
    private val fragment: ClothFragment,
    private val movieList: List<Movie>,
    var callback:(Int)->Unit
) :
    RecyclerView.Adapter<RecyclerViewMovieAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_movie_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //holder.ivMovieImg.setImageResource(movieList[position].image)
        Picasso.get().load(movieList[position].image).into(holder.ivMovieImg)

        holder.cardView.setOnClickListener {
            Toast.makeText(fragment.requireContext(), movieList[position].title, Toast.LENGTH_LONG).show()
            callback(position)
        }
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMovieImg: ImageView = itemView.findViewById(R.id.ivMovieImg)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }

}