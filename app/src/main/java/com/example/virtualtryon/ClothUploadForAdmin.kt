package com.example.virtualtryon

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ClothUploadForAdmin.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClothUploadForAdmin : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var pickButtonnew: Button
    private lateinit var btnCamera: Button
    private lateinit var imgGallery: ImageView
    private var mImageUri: Uri? = null
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference
    val sharedViewModel: SharedViewModel by activityViewModels()
    private val Gallery_REQ_CODE = 1000
    private val PICK_AN_IMAGE_REQUEST = 444
    private val CAMERA_REQUEST_CODE = 1001
    private var isAdmin: Boolean = false



    private var param1: String? = null
    private var param2: String? = null
    private lateinit var uploadProgressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        mStorageRef = FirebaseStorage.getInstance().getReference("clothuploads")
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("clothuploads")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_cloth_for_admin, container, false)
//        pickButton = rootView.findViewById(R.id.button_gallery)
        pickButtonnew = rootView.findViewById(R.id.button_upload)
        btnCamera = rootView.findViewById(R.id.button_camera)

        imgGallery = rootView.findViewById(R.id.imageView)
        val btnGallery = rootView.findViewById<Button>(R.id.button_gallery)

        btnGallery.setOnClickListener {
            val iGallery = Intent(Intent.ACTION_PICK)
            iGallery.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(iGallery, Gallery_REQ_CODE)
        }
//        pickButton.setOnClickListener {
//            val fragmentB = ModelsFragment()
//            val transaction = requireActivity().supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.fragment_container, fragmentB)
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }

        btnCamera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
        }

        // Initialize the ProgressBar
        uploadProgressBar = rootView.findViewById(R.id.progressBar)

        // Your existing code

        pickButtonnew.setOnClickListener {
            // Show the progress bar
            uploadProgressBar.visibility = View.VISIBLE

            val uploadCoroutine = CoroutineScope(Dispatchers.Main)
            uploadCoroutine.launch {
                val uploadedURI = uploadFile()
                // Hide the progress bar when upload is complete
                uploadProgressBar.visibility = View.GONE

                if (uploadedURI != null) {
                    //sharedViewModel.personImageURL = uploadedURI
                    val fragmentB = ClothFragment()
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, fragmentB)
                    transaction.addToBackStack(null)
                    transaction.commit()
                } else {
                    Toast.makeText(context, "Could not upload your image", Toast.LENGTH_LONG).show()
                }
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
         * @return A new instance of fragment ClothFragmentForAdmin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ClothUploadForAdmin().apply {
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
            if (requestCode == CAMERA_REQUEST_CODE) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                // Convert Bitmap to Uri
                mImageUri = getImageUri(requireContext(), imageBitmap)
                // Load selected image into ImageView
                //Picasso.get().load(mImageUri).into(imgGallery)
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
    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
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