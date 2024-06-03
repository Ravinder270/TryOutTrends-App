package com.example.virtualtryon


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
 * Use the [ClothFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClothFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val sharedViewModel: SharedViewModel by activityViewModels()
    private var recyclerView: RecyclerView? = null
    private var recyclerViewImageAdapter: RecyclerViewImageAdapter? = null
    private var imageList: List<Models>? = listOf<Models>()
    private lateinit var progressBar: ProgressBar
    private var isAdmin: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        //val auth = FirebaseAuth.getInstance()
//        if(auth.currentUser != null){
//            Log.d("tryOut","Email : ${auth.currentUser!!.email.toString()}")
//            Toast.makeText(requireContext(),"Current user : ${auth.currentUser!!.email.toString()}",Toast.LENGTH_LONG).show()
//        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            isAdmin = it.email == "admin123@gmail.com"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_models, container, false)
        progressBar = rootView.findViewById(R.id.progressBar)

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            progressBar.visibility = View.VISIBLE // Show the ProgressBar while loading

            imageList = prepareImageListData()

            progressBar.visibility = View.GONE // Hide the ProgressBar after loading

            coroutineScope.cancel()
            Log.d("result", "Image list : ${imageList.toString()}")
            if (imageList != null) {
                val recyclerView = rootView.findViewById<RecyclerView>(R.id.rvImageListsInModels)
                recyclerViewImageAdapter =
                    RecyclerViewImageAdapter(
                        fragment = this@ClothFragment,
                        imageList!!
                    ){position->
//                        sharedViewModel.clothImageURL = imageList!![position].image
//
//                        parentFragmentManager.beginTransaction()
//                            .replace(R.id.fragment_container, TryOutResult()).commit()
                        if (isAdmin) {
                            //Toast.makeText(this@ClothFragment.requireContext(), "image", Toast.LENGTH_SHORT).show()
                        } else {
                            sharedViewModel.clothImageURL = imageList!![position].image
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, TryOutResult()).commit()
                        }
                    }
                val layoutManager: RecyclerView.LayoutManager =
                    GridLayoutManager(this@ClothFragment.requireContext(), 2)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = recyclerViewImageAdapter
            } else {
                Toast.makeText(
                    this@ClothFragment.requireContext(),
                    "Image list is null",
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
         * @return A new instance of fragment ClothFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ClothFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private suspend fun prepareImageListData(): List<Models>? {
        // Assuming you have a reference to your Firebase database
        return suspendCoroutine { continuation ->
            val list = arrayListOf<Models>()
            val databaseReference = FirebaseStorage.getInstance().reference.child("clothuploads")
            databaseReference.listAll().addOnSuccessListener { (items, prefixes) ->
                for (prefix in prefixes) {
                    Log.d("showImages", "prefix : $prefix")
                }
                val downloadCoroutines = items.map { item ->
                    CoroutineScope(Dispatchers.Main).async {
                        val downloadUrl = item.downloadUrl.await()
                        if (downloadUrl != null) {
                            Log.d("downloadURL", "URL : $downloadUrl")
                            Models("Image", downloadUrl)
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