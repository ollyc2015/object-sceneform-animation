package com.lush_digital.objanimation.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.ar.sceneform.rendering.ModelRenderable
import com.lush_digital.objanimation.data.ModelLoader


class LoadingViewModel(application: Application) : AndroidViewModel(application) {


    private var modelLoader: ModelLoader? = null
    var numOfModelsLoadedTotal = 0
    val batchSize = 25
    var map: HashMap<String, ModelRenderable?> =  HashMap()
    val id = ArrayList<Int>()

    private var modelIDCount: MutableLiveData<Int>? = null



    fun getModelIDCount(): MutableLiveData<Int>? {
        if (modelIDCount == null) {
            modelIDCount = MutableLiveData()

        }
        return modelIDCount
    }

    fun loadRawFiles(){

        allRawFiles()
        loadBatchModels()
    }

    fun loadModel() {
        modelLoader = ModelLoader(this)
    }

    private fun allRawFiles(): ArrayList<Int> {
        for (i in 1..50) {
            id.add(getApplication<Application>().resources.getIdentifier("kw$i", "raw", getApplication<Application>().packageName))
        }
        return id
    }


    private fun loadBatchModels() {
        val tempLoaded = numOfModelsLoadedTotal
        for (i in tempLoaded .. tempLoaded + batchSize) {
            if (i < 50) {
                modelLoader?.loadModel(i+1, id[i])
            }
        }
    }

    fun setRenderable(id: Int, renderable: ModelRenderable) {
        numOfModelsLoadedTotal++
        map.set("kw$id", renderable)

        modelIDCount?.value = id

        Log.d("olly", "my id: $id")


        if (numOfModelsLoadedTotal == 50) {

           // AndroidUtils.animateView(progress_overlay, View.GONE, 0f, 200)
            Log.d("olly", "got to 100")

            try {

                // viewModel.setModels(map)
                saveMap(map)


            } catch (e: java.lang.Exception) {

                Log.d("olly", "Error Here: $e")
            }


        }
        if (numOfModelsLoadedTotal % batchSize == 0) {
            loadBatchModels()
        }
    }

    companion object{

        var myMap: HashMap<String, ModelRenderable?> =  HashMap()

        fun saveMap(map: HashMap<String, ModelRenderable?>) {

            myMap = map
        }


        fun getMap(): HashMap<String, ModelRenderable?> {

            return myMap
        }

    }
}
