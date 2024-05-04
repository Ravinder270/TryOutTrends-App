package com.example.virtualtryon

class Upload  {

        var imageUrl: String? = null

        constructor() {
            // Empty constructor needed for Firebase
        }

        constructor( imageUrl: String) {

            this.imageUrl = imageUrl
        }
}