package com.example.virtualtryon

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.virtualtryon.databinding.FragmentTryOutResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit


class TryOutResult : Fragment() {
    lateinit var binding:FragmentTryOutResultBinding
    private val sharedViewModel : SharedViewModel by activityViewModels()
    private lateinit var saveimage: Button
    private lateinit var uploadProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTryOutResultBinding.inflate(inflater,container,false)
        saveimage = binding.root.findViewById(R.id.savegallery)
        uploadProgressBar = binding.root.findViewById(R.id.progressBar)
        // Request permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        upload()
        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null){
            Log.d("tryOut","Email : ${auth.currentUser!!.email.toString()}")
            //Toast.makeText(requireContext(),"Current user : ${auth.currentUser!!.email.toString()}",Toast.LENGTH_LONG).show()
        }
        return binding.root
    }
    private fun upload() {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            // Increase the timeout duration
            .connectTimeout(6000, TimeUnit.SECONDS) // Set the connection timeout
            .readTimeout(6000, TimeUnit.SECONDS)    // Set the read timeout
            .writeTimeout(6000, TimeUnit.SECONDS)   // Set the write timeout

        //Add ngrok link here
        //I added ethernet's IPAddress
        val retrofit = Retrofit.Builder()
            .baseUrl("https://6cae-34-143-246-207.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()
            .create(UploadService::class.java)

        uploadProgressBar.visibility = View.VISIBLE


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val personImageBitmap = loadImageFromUrl(sharedViewModel.personImageURL)
                val clothImageBitmap = loadImageFromUrl(sharedViewModel.clothImageURL)

                // Convert bitmaps to files
                val file1 = bitmapToFile(personImageBitmap, "image1.png")
                val file2 = bitmapToFile(clothImageBitmap, "image2.png")

                // Create request bodies
                val requestBody1 = file1.asRequestBody("image/*".toMediaTypeOrNull())
                val requestBody2 = file2.asRequestBody("image/*".toMediaTypeOrNull())

                // Create multipart parts
                val part1 = MultipartBody.Part.createFormData("image1", file1.name, requestBody1)
                val part2 = MultipartBody.Part.createFormData("image2", file2.name, requestBody2)

                // Upload images
                val response = retrofit.uploadImages(part1, part2)
                Log.e("Mustsee", "Image URLs: ${response.url}")
                // Update UI or perform any other actions based on the response

                val imageUrl = response.url
                withContext(Dispatchers.Main) {
                    try {
                        Picasso.get().load(imageUrl).into(binding.personImageViewInTryOutResultFragment)
                        uploadProgressBar.visibility = View.GONE
                        //ye add kara h 27 may
                        saveimage.setOnClickListener {
                            val imageView = binding.root.findViewById<ImageView>(R.id.personImageViewInTryOutResultFragment)
                            saveToGallery(imageView)
                        }
                        //
                        uploadImageslasttryonresult(imageUrl)
                    } catch (e: Exception) {
                        Log.e("ImageLoadingError", "Error loading image: ${e.message}", e)
                    }
                }

            } catch (e: Exception) {
                Log.e("Coder", "Error uploading images: ${e.message}", e)
                // Display an error message to the user
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error uploading images", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun loadImageFromUrl(imageUrl: Uri?): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                if (imageUrl != null) {
                    val url = URL(imageUrl.toString())
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val inputStream = connection.inputStream
                    BitmapFactory.decodeStream(inputStream)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("Coder", "Error loading image from URL: ${e.message}", e)
                null
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap?, filename: String): File {
        val file = File(context?.cacheDir, filename)
        file.createNewFile()
        val outputStream = FileOutputStream(file)
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }

    private fun uploadImageslasttryonresult(imageUrl: String) {

        //Remember Ravinder, 1 error- It does not upload the photo taken from gallery to the gmail id

        val auth = FirebaseAuth.getInstance()
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            // Reference to your Firebase storage
            val storageRef = FirebaseStorage.getInstance().reference

            // Reference to the user's folder (using email as folder name)
            val userFolderRef = storageRef.child("images/$userEmail")

            // Example image URLs (replace these with your actual URLs)
            val personImageUrl = imageUrl

            val timestamp = System.currentTimeMillis()
            val personImageFilename = "person_image_$timestamp.jpg"

            // Load images using Picasso
            Picasso.get().load(personImageUrl).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    bitmap?.let {
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                        val data = baos.toByteArray()
                        val imageRef = userFolderRef.child(personImageFilename)
                        imageRef.putBytes(data)
                            .addOnSuccessListener {
                                Log.d("tryOut", "Uploaded image $personImageFilename successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("tryOut", "Failed to upload image $personImageFilename: ${e.message}")
                            }
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    Log.e("tryOut", "Failed to load image: $personImageFilename")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    // Do nothing
                }
            })
        }

    }
    private fun saveToGallery(imageView: ImageView) {
        val drawable = imageView.drawable
        if (drawable == null || drawable !is BitmapDrawable) {
            Toast.makeText(requireContext(), "Failed to save image: No image to save", Toast.LENGTH_LONG).show()
            Log.e("saveToGallery", "No image to save")
            return
        }

        val bitmap = drawable.bitmap

        // Save to the public directory
        val picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val albumDir = File(picturesDirectory, "TryOutTrends")
        if (!albumDir.exists()) {
            if (!albumDir.mkdirs()) {
                Toast.makeText(requireContext(), "Failed to create directory", Toast.LENGTH_LONG).show()
                Log.e("saveToGallery", "Failed to create directory: ${albumDir.absolutePath}")
                return
            }
        }

        val filename = String.format("%d.png", System.currentTimeMillis())
        val outFile = File(albumDir, filename)
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(outFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Make the image available in the gallery
            MediaScannerConnection.scanFile(requireContext(), arrayOf(outFile.toString()), null, null)
            Toast.makeText(requireContext(), "Image saved to gallery", Toast.LENGTH_LONG).show()
            Log.d("saveToGallery", "Image saved successfully: ${outFile.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_LONG).show()
            Log.e("saveToGallery", "Failed to save image: ${e.message}", e)
            try {
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("saveToGallery", "Failed to close output stream: ${e.message}", e)
            }
        }
    }

//    private fun saveToGallery(imageView: ImageView) {
//        val drawable = imageView.drawable
//        if (drawable == null || drawable !is BitmapDrawable) {
//            Toast.makeText(requireContext(), "Failed to save image: No image to save", Toast.LENGTH_LONG).show()
//            Log.e("saveToGallery", "No image to save")
//            return
//        }
//
//        val bitmap = drawable.bitmap
//
//        val file = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        val dir = File(file, "MyPics")
//        if (!dir.exists() && !dir.mkdirs()) {
//            Toast.makeText(requireContext(), "Failed to create directory", Toast.LENGTH_LONG).show()
//            Log.e("saveToGallery", "Failed to create directory: ${dir.absolutePath}")
//            return
//        }
//
//        val filename = String.format("%d.png", System.currentTimeMillis())
//        val outFile = File(dir, filename)
//        var outputStream: FileOutputStream? = null
//
//        try {
//            outputStream = FileOutputStream(outFile)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//            outputStream.flush()
//            outputStream.close()
//            MediaScannerConnection.scanFile(requireContext(), arrayOf(outFile.toString()), null, null)
//            Toast.makeText(requireContext(), "Image saved to gallery", Toast.LENGTH_LONG).show()
//            Log.d("saveToGallery", "Image saved successfully: ${outFile.absolutePath}")
//        } catch (e: IOException) {
//            e.printStackTrace()
//            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_LONG).show()
//            Log.e("saveToGallery", "Failed to save image: ${e.message}", e)
//            try {
//                outputStream?.close()
//            } catch (e: IOException) {
//                e.printStackTrace()
//                Log.e("saveToGallery", "Failed to close output stream: ${e.message}", e)
//            }
//        }
//    }

//    private fun createImageTarget(filename: String, userFolderRef: StorageReference): Target {
//        return object : Target {
//            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                bitmap?.let {
//                    val baos = ByteArrayOutputStream()
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
//                    val data = baos.toByteArray()
//                    val imageRef = userFolderRef.child(filename)
//                    imageRef.putBytes(data).addOnSuccessListener {
//                        Log.d("tryOut", "Uploaded image $filename successfully")
//                    }.addOnFailureListener { e ->
//                        Log.e("tryOut", "Failed to upload image $filename: ${e.message}")
//                    }
//                }
//            }
//
//            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
//                Log.e("tryOut", "Failed to load image: $filename")
//            }
//
//            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
//                // Do nothing
//            }
//        }
//    }

}

