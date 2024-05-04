package com.example.virtualtryon

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class ModelsPhotoPick : AppCompatActivity() {
//    private var recyclerView: RecyclerView? = null
//    private var recyclerViewMovieAdapter: RecyclerViewAdapterforPickancloth? = null
//    private var movieList: List<Movie>? = listOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_models_photo_pick)
//        val coroutineScope = CoroutineScope(Dispatchers.Main)
//        coroutineScope.launch {
//
//            movieList = prepareMovieListData()
//            coroutineScope.cancel()
//            Log.d("result","movie list : ${movieList.toString()}")
//            if (movieList != null) {
//                recyclerView = findViewById<RecyclerView>(R.id.rvMovieListsInModels)
//                recyclerViewMovieAdapter =
//                    RecyclerViewAdapterforPickancloth(
//                        getActivity = this@ModelsPhotoPick,
//                        movieList!!
//                    )
//                val layoutManager: RecyclerView.LayoutManager =
//                    GridLayoutManager(this@ModelsPhotoPick, 2)
//                recyclerView!!.layoutManager = layoutManager
//                recyclerView!!.adapter = recyclerViewMovieAdapter
//            } else {
//                Toast.makeText(this@ModelsPhotoPick, "Movie list is null", Toast.LENGTH_LONG).show()
//            }
//        }


    }

//    private suspend fun prepareMovieListData(): List<Movie>? {
//        // Assuming you have a reference to your Firebase database
//        return suspendCoroutine { continuation ->
//            val list = arrayListOf<Movie>()
//            val databaseReference = FirebaseStorage.getInstance().reference.child("Models")
//            databaseReference.listAll().addOnSuccessListener { (items, prefixes) ->
//                for (prefix in prefixes) {
//                    Log.d("showImages", "prefix : $prefix")
//                }
//                val downloadCoroutines = items.map { item ->
//                    CoroutineScope(Dispatchers.Main).async {
//                        val downloadUrl = item.downloadUrl.await()
//                        if (downloadUrl != null) {
//                            Log.d("downloadURL","URL : $downloadUrl")
//                            Movie("Image", downloadUrl)
//                        } else {
//                            null
//                        }
//                    }
//                }
//                CoroutineScope(Dispatchers.Main).launch {
//                    val result = downloadCoroutines.awaitAll().filterNotNull()
//                    Log.d("downloadURL","Result : ${result.size}")
//                    continuation.resume(result)
//                }
//            }.addOnFailureListener { exc ->
//                Log.d("showImages", "Exception : $exc")
//                continuation.resume(null)
//            }
//        }
//
//    }
}
