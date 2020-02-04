package com.lush_digital.objanimation

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_example.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ExampleActivity : AppCompatActivity() {

    var map: MutableMap<String, ModelRenderable?> = HashMap()
    val id = ArrayList<Int>()
    var startTime = 0L
    var numOfModelsLoadedTotal = 0
    val batchSize = 25


    private var modelLoader: ModelLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        init()
    }

    fun init() {

        allRawFiles()
        modelLoader = ModelLoader(this)

        startTime = System.currentTimeMillis()
        loadBatchModels()
    }

    private fun allRawFiles(): ArrayList<Int> {
        for (i in 1..100) {
            id.add(resources.getIdentifier("kw$i", "raw", packageName))
        }
        return id
    }

    private fun loadBatchModels() {
        val tempLoaded = numOfModelsLoadedTotal
        for (i in tempLoaded until tempLoaded + batchSize) {
            if (i < 100) {
                modelLoader?.loadModel(i, id[i])
            }
        }
    }

    fun setRenderable(id: Int, renderable: ModelRenderable) {
        numOfModelsLoadedTotal++
        map["kw$id"] = renderable
        theTextView.text = "$id%"
        if (numOfModelsLoadedTotal == 100) {
            theTextView.text = "Loading Complete. Time: ${System.currentTimeMillis() - startTime}"
        }
        if (numOfModelsLoadedTotal % batchSize == 0) {
            loadBatchModels()
        }
    }
}
