package com.example.virtualtryon

import android.net.Uri
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    var personImageURL: Uri? = null
    var clothImageURL: Uri? = null
}