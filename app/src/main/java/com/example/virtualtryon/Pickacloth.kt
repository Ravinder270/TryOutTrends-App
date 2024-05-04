package com.example.virtualtryon

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView


class Pickacloth : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var recyclerViewMovieAdapter: RecyclerViewMovieAdapter? = null
    private var movieList = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickacloth)

//        movieList = ArrayList()
//
//        recyclerView = findViewById<View>(R.id.rvMovieLists) as RecyclerView
//        recyclerViewMovieAdapter = RecyclerViewMovieAdapter(getActivity = this@Pickacloth, movieList)
//        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 2)
//        recyclerView!!.layoutManager = layoutManager
//        recyclerView!!.adapter = recyclerViewMovieAdapter
//
//        prepareMovieListData()

    }

    private fun prepareMovieListData() {
//        var movie = Movie("Image1", R.drawable.cloth1)
//        movieList.add(movie)
//        movie = Movie("Image2", R.drawable.cloth2)
//        movieList.add(movie)
//
//        movie = Movie("Image3", R.drawable.cloth3)
//        movieList.add(movie)
//        movie = Movie("Image4", R.drawable.cloth4)
//        movieList.add(movie)
//        movie = Movie("Image5", R.drawable.cloth5)
//        movieList.add(movie)
//        movie = Movie("Image6", R.drawable.cloth6)
//        movieList.add(movie)
//        movie = Movie("Image7", R.drawable.cloth7)
//        movieList.add(movie)
//        movie = Movie("Image8", R.drawable.cloth8)
//        movieList.add(movie)
//        movie = Movie("Image9", R.drawable.cloth9)
//        movieList.add(movie)
//        movie = Movie("Image10", R.drawable.cloth10)
//        movieList.add(movie)
//        recyclerViewMovieAdapter!!.notifyDataSetChanged()

        //All Code is done let's run the app
    }
}
