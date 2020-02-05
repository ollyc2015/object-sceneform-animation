package com.lush_digital.objanimation.ui


import androidx.lifecycle.LiveData

import androidx.lifecycle.ViewModel
import com.google.ar.sceneform.rendering.ModelRenderable


class SharedViewModel : ViewModel() {

    lateinit var myMap: LiveData<HashMap<String, ModelRenderable?>>

    fun setModels(map: LiveData<HashMap<String, ModelRenderable?>>) {

        myMap = map

    }

    fun getModels(): LiveData<HashMap<String, ModelRenderable?>> {

        return myMap
    }
}