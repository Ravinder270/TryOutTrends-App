package com.example.virtualtryon

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class PickAnImage : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var recyclerViewMovieAdapter: RecyclerViewAdapterforPickancloth? = null
    private var movieList = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickacloth)

        movieList = ArrayList()

        recyclerView = findViewById<View>(R.id.rvMovieLists) as RecyclerView
        //Must uncomment this before execution
        //recyclerViewMovieAdapter = RecyclerViewAdapterforPickancloth(getActivity = this@PickAnImage, movieList)
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 2)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = recyclerViewMovieAdapter

        prepareMovieListData()

    }

    private fun prepareMovieListData() {
//        var movie = Movie("Image1", R.drawable.image1)
//        movieList.add(movie)
//        movie = Movie("Image2", R.drawable.image2)
//        movieList.add(movie)
//
//        movie = Movie("Image3", R.drawable.image3)
//        movieList.add(movie)
//        movie = Movie("Image4", R.drawable.image4)
//        movieList.add(movie)
//        movie = Movie("Image5", R.drawable.image5)
//        movieList.add(movie)
//        movie = Movie("Image6", R.drawable.image6)
//        movieList.add(movie)
//        movie = Movie("Image7", R.drawable.image7)
//        movieList.add(movie)
//        movie = Movie("Image8", R.drawable.image8)
//        movieList.add(movie)
//        movie = Movie("Image9", R.drawable.image9)
//        movieList.add(movie)
//        movie = Movie("Image10", R.drawable.image10)
//        movieList.add(movie)
//
//        recyclerViewMovieAdapter!!.notifyDataSetChanged()

        //All Code is done let's run the app
    }
}
