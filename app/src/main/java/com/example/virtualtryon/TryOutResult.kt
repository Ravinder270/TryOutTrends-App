package com.example.virtualtryon

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.virtualtryon.databinding.FragmentTryOutResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.ByteArrayOutputStream


class TryOutResult : Fragment() {
    lateinit var binding:FragmentTryOutResultBinding
    private val sharedViewModel : SharedViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTryOutResultBinding.inflate(inflater,container,false)

        Picasso.get().load(sharedViewModel.personImageURL).into(binding.personImageViewInTryOutResultFragment)
        Picasso.get().load(sharedViewModel.clothImageURL).into(binding.clothImageViewInTryOutResultFragment)
        uploadImages()
        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null){
            Log.d("tryOut","Email : ${auth.currentUser!!.email.toString()}")
            Toast.makeText(requireContext(),"Current user : ${auth.currentUser!!.email.toString()}",Toast.LENGTH_LONG).show()
        }
        return binding.root
    }

    private fun uploadImages() {

        //Remember Ravinder, 1 error- It does not upload the photo taken from gallery to the gmail id


        val auth = FirebaseAuth.getInstance()
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            // Reference to your Firebase storage
            val storageRef = FirebaseStorage.getInstance().reference

            // Reference to the user's folder (using email as folder name)
            val userFolderRef = storageRef.child("images/$userEmail")

            // Example image URLs (replace these with your actual URLs)
            val personImageUrl = sharedViewModel.personImageURL
            val clothImageUrl = sharedViewModel.clothImageURL

            val timestamp = System.currentTimeMillis()
            val personImageFilename = "person_image_$timestamp.jpg"
            val clothImageFilename = "cloth_image_$timestamp.jpg"

            // Create targets for Picasso to load images
            val personImageTarget = createImageTarget(personImageFilename, userFolderRef)
            val clothImageTarget = createImageTarget(clothImageFilename, userFolderRef)

            // Load images using Picasso
            Picasso.get().load(personImageUrl).into(personImageTarget)
            Picasso.get().load(clothImageUrl).into(clothImageTarget)
        }
    }

    private fun createImageTarget(filename: String, userFolderRef: StorageReference): Target {
        return object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.let {
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    val imageRef = userFolderRef.child(filename)
                    imageRef.putBytes(data).addOnSuccessListener {
                        Log.d("tryOut", "Uploaded image $filename successfully")
                    }.addOnFailureListener { e ->
                        Log.e("tryOut", "Failed to upload image $filename: ${e.message}")
                    }
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                Log.e("tryOut", "Failed to load image: $filename")
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                // Do nothing
            }
        }
    }

}

