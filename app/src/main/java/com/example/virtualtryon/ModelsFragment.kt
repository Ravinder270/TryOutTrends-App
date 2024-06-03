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
 * Use the [ModelsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class ModelsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val sharedViewModel:SharedViewModel by activityViewModels()
    private var recyclerView: RecyclerView? = null
    private var recyclerViewImageAdapter: RecyclerViewAdapterforModelsFragment? = null
    private var imageList: List<Models>? = listOf<Models>()

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
       val rootView = inflater.inflate(R.layout.fragment_models, container, false)

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {

            imageList = prepareImageList()
            coroutineScope.cancel()
            Log.d("result","Image list : ${imageList.toString()}")
            if (imageList != null) {
                val recyclerView = rootView.findViewById<RecyclerView>(R.id.rvImageListsInModels)
                recyclerViewImageAdapter =
                    RecyclerViewAdapterforModelsFragment(
                        fragment = this@ModelsFragment,
                        imageList!!,
                        callback = {position->
                            sharedViewModel.personImageURL =  imageList!![position].image

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, ClothFragment()).commit()
                        }
                    )
                val layoutManager: RecyclerView.LayoutManager =
                    GridLayoutManager(this@ModelsFragment.requireContext(), 2)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = recyclerViewImageAdapter
            } else {
                Toast.makeText(this@ModelsFragment.requireContext(), "Image list is null", Toast.LENGTH_LONG).show()
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
         * @return A new instance of fragment ModelsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ModelsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private suspend fun prepareImageList(): List<Models>? {
        // Assuming you have a reference to your Firebase database
        return suspendCoroutine { continuation ->
            val list = arrayListOf<Models>()
            val databaseReference = FirebaseStorage.getInstance().reference.child("Models")
            databaseReference.listAll().addOnSuccessListener { (items, prefixes) ->
                for (prefix in prefixes) {
                    Log.d("showImages", "prefix : $prefix")
                }
                val downloadCoroutines = items.map { item ->
                    CoroutineScope(Dispatchers.Main).async {
                        val downloadUrl = item.downloadUrl.await()
                        if (downloadUrl != null) {
                            Log.d("downloadURL","URL : $downloadUrl")
                            Models("Image", downloadUrl)
                        } else {
                            null
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    val result = downloadCoroutines.awaitAll().filterNotNull()
                    Log.d("downloadURL","Result : ${result.size}")
                    continuation.resume(result)
                }
            }.addOnFailureListener { exc ->
                Log.d("showImages", "Exception : $exc")
                continuation.resume(null)
            }
        }

    }
}