package com.example.virtualtryon

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.component1
import com.google.firebase.storage.component2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SaveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SaveFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val sharedViewModel: SharedViewModel by activityViewModels()
    private var recyclerView: RecyclerView? = null
    private var recyclerViewMovieAdapter: RecyclerViewAdapterForSavedImages? = null
    private var movieList: List<Movie>? = listOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_save, container, false)

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            movieList = prepareMovieListData()
            coroutineScope.cancel()
            Log.d("result", "movie list : ${movieList.toString()}")
            if (movieList != null) {
                val recyclerView = rootView.findViewById<RecyclerView>(R.id.SavedMovieListsInModels)
                recyclerViewMovieAdapter =
                    RecyclerViewAdapterForSavedImages(
                        fragment = this@SaveFragment,
                        movieList!!
                    ) { position ->
                        sharedViewModel.clothImageURL = movieList!![position].image

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, TryOutResult()).commit()
                    }
                val layoutManager: RecyclerView.LayoutManager =
                    GridLayoutManager(this@SaveFragment.requireContext(), 2)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = recyclerViewMovieAdapter
            } else {
                Toast.makeText(
                    this@SaveFragment.requireContext(),
                    "Movie list is null",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return rootView
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SaveFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SaveFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
//    private fun retrieveImages() {
//        val user = FirebaseAuth.getInstance().currentUser
//        if (user != null) {
//            // Reference to your Firebase storage
//            val storageRef = FirebaseStorage.getInstance().reference
//
//            // Reference to the user's folder (using email as folder name)
//            val userFolderRef: StorageReference = storageRef.child("images/${user.uid}")
//
//            // List all the items (images) in the user's folder
//            userFolderRef.listAll().addOnSuccessListener { result ->
//                result.items.forEach { imageRef ->
//                    // Get the download URL for the image
//                    imageRef.downloadUrl.addOnSuccessListener { url ->
//                        // Now you have the download URL for the image, you can use it to display the image or perform any other operations
//                        println("Image URL: $url")
//                        // Here you can use the URL to display the image in your UI
//                    }.addOnFailureListener { exception ->
//                        // Handle any errors that occurred while retrieving the download URL
//                        println("Error getting download URL: ${exception.message}")
//                    }
//                }
//            }.addOnFailureListener { exception ->
//                // Handle any errors that occurred while listing items in the folder
//                println("Error listing items: ${exception.message}")
//            }
//        }
//    }
    private suspend fun prepareMovieListData(): List<Movie>? {
        // Assuming you have a reference to your Firebase database
        val auth = FirebaseAuth.getInstance()
        val userEmail = auth.currentUser?.email
        return suspendCoroutine { continuation ->
            val list = arrayListOf<Movie>()
            val databaseReference = FirebaseStorage.getInstance().reference.child("images/$userEmail")
            databaseReference.listAll().addOnSuccessListener { (items, prefixes) ->
                for (prefix in prefixes) {
                    Log.d("showImages", "prefix : $prefix")
                }
                val downloadCoroutines = items.map { item ->
                    CoroutineScope(Dispatchers.Main).async {
                        val downloadUrl = item.downloadUrl.await()
                        if (downloadUrl != null) {
                            Log.d("downloadURL", "URL : $downloadUrl")
                            Movie("Image", downloadUrl)
                        } else {
                            null
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    val result = downloadCoroutines.awaitAll().filterNotNull()
                    Log.d("downloadURL", "Result : ${result.size}")
                    continuation.resume(result)
                }
            }.addOnFailureListener { exc ->
                Log.d("showImages", "Exception : $exc")
                continuation.resume(null)
            }
        }

    }
}