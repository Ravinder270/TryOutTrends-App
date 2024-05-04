package com.example.virtualtryon

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class ClothImageupload : AppCompatActivity() {
    private lateinit var picknewButton: Button
    private lateinit var pickButtonnew: Button
    private val Gallery_REQ_CODE = 1000
    private lateinit var imgGallery: ImageView
    private var mImageUri: Uri? = null


    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloth_imageupload)


        picknewButton = findViewById(R.id.button_pick)
        pickButtonnew = findViewById(R.id.button_upload)

        mStorageRef = FirebaseStorage.getInstance().getReference("clothuploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("clothuploads");



        imgGallery = findViewById<ImageView>(R.id.imageView) // Fix: Initialize the global variable

        val btnGallery = findViewById<Button>(R.id.button_choose_file)

        btnGallery.setOnClickListener {
            val iGallery = Intent(Intent.ACTION_PICK)
            iGallery.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(iGallery, Gallery_REQ_CODE)
        }


        picknewButton.setOnClickListener {
            val intent = Intent(this@ClothImageupload, Pickacloth::class.java)
            startActivity(intent)
        }

        pickButtonnew.setOnClickListener {
            val intent = Intent(this@ClothImageupload, TryOutResult::class.java)
            uploadFile()
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Gallery_REQ_CODE) {
                mImageUri = data?.data
                Picasso.get().load(mImageUri).into(imgGallery)
            }
        }
    }
    private fun getFileExtension(uri: Uri): String? {
        val cR = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun uploadFile() {
        mImageUri?.let { uri ->
            val fileReference = mStorageRef.child("${System.currentTimeMillis()}.${getFileExtension(uri)}")

            val uploadTask = fileReference.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    Toast.makeText(this@ClothImageupload, "Upload successful", Toast.LENGTH_LONG).show()

                    val upload = Upload(taskSnapshot.storage.downloadUrl.toString())
                    val uploadId = mDatabaseRef.push().key
                    mDatabaseRef.child(uploadId ?: "").setValue(upload)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@ClothImageupload, e.message, Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }
}
