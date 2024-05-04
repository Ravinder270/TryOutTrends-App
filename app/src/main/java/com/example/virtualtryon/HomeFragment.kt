package com.example.virtualtryon

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private lateinit var pickButton: Button
    private lateinit var pickButtonnew: Button
    private lateinit var imgGallery: ImageView
    private var mImageUri: Uri? = null
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference
    val sharedViewModel: SharedViewModel by activityViewModels()
    private val Gallery_REQ_CODE = 1000
    private val PICK_AN_IMAGE_REQUEST = 444

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        mStorageRef = FirebaseStorage.getInstance().getReference("PersonImageUpload")
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("PersonImageUpload")


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        pickButton = rootView.findViewById(R.id.button_pick)
        pickButtonnew = rootView.findViewById(R.id.button_upload)
        imgGallery = rootView.findViewById(R.id.imageView)
        val btnGallery = rootView.findViewById<Button>(R.id.button_choose_file)

        btnGallery.setOnClickListener {
            val iGallery = Intent(Intent.ACTION_PICK)
            iGallery.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(iGallery, Gallery_REQ_CODE)
        }
        pickButton.setOnClickListener {
//            val intent = Intent(requireContext(), ModelsPhotoPick::class.java)
//            startActivity(intent)
            // Instantiate Fragment B
            val fragmentB = ModelsFragment()

// Begin Fragment Transaction
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

// Replace Fragment A with Fragment B
            transaction.replace(R.id.fragment_container, fragmentB)

// Add Fragment B to the back stack (optional)
            transaction.addToBackStack(null)

// Commit the transaction
            transaction.commit()
        }

        pickButtonnew.setOnClickListener {
//            val intent = Intent(requireContext(), ClothImageupload::class.java)
            val uploadCoroutine = CoroutineScope(Dispatchers.Main)
            uploadCoroutine.launch {

                val uploadedURI = uploadFile()
                uploadCoroutine.cancel()
                if(uploadedURI != null){
                    sharedViewModel.personImageURL = uploadedURI
                    val fragmentB = ClothFragment()

// Begin Fragment Transaction
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()

// Replace Fragment A with Fragment B
                    transaction.replace(R.id.fragment_container, fragmentB)

// Add Fragment B to the back stack (optional)
                    transaction.addToBackStack(null)

// Commit the transaction
                    transaction.commit()
                }else{
                    Toast.makeText(context,"Could not upload your image",Toast.LENGTH_LONG).show()
                }
            }
//            startActivity(intent)
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
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Gallery_REQ_CODE) {
                // Handle result from gallery
                mImageUri = data?.data
                if (mImageUri != null) {
                    // Load selected image into ImageView
                    Picasso.get().load(mImageUri).into(imgGallery)
                } else {
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == PICK_AN_IMAGE_REQUEST) {
            // Handle result from PickAnImage activity
            mImageUri = data?.getStringExtra("imageUri")?.toUri()
            if (mImageUri != null) {
                // Load selected image into ImageView
                Picasso.get().load(mImageUri).into(imgGallery)
            } else {
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val cR = requireContext().contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private suspend fun uploadFile() : Uri? {
        return suspendCoroutine {continuation->
            mImageUri?.let { uri ->
                val fileReference =
                    mStorageRef.child("${System.currentTimeMillis()}.${getFileExtension(uri)}")
                val urlCoroutine = CoroutineScope(Dispatchers.Main)
                val uploadTask = fileReference.putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_LONG)
                            .show()
                        urlCoroutine.async {
                            val downloadURL = taskSnapshot.storage.downloadUrl.await()
                            if(downloadURL != null){
                                val upload = Upload()
                                val uploadId = mDatabaseRef.push().key
                                mDatabaseRef.child(uploadId ?: "").setValue(upload)
                                continuation.resume(downloadURL)
                            }else{
                                continuation.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                        continuation.resume(null)
                    }
            } ?: run {
                Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_SHORT).show()
                continuation.resume(null)
            }
        }
    }
}