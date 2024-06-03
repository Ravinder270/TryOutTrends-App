package com.example.virtualtryon

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService {

    @Multipart
    @POST("/")
    suspend fun uploadImages(@Part image1: MultipartBody.Part,@Part image2: MultipartBody.Part ):ImageResponse
}