package com.lush_digital.objanimation.ui



import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.ar.core.Config
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.lush_digital.objanimation.R
import com.lush_digital.objanimation.data.ModelLoader
import com.lush_digital.objanimation.utils.AndroidUtils
import kotlinx.android.synthetic.main.include_progress_overlay.*


class MainActivity :AppCompatActivity() {

    var map: HashMap<String, ModelRenderable?> = HashMap()
    var arFragment: ArFragment? = null
    val id = ArrayList<Int>()
    var startTime = 0L
    var numOfModelsLoadedTotal = 0
    val batchSize = 25
    private var modelLoader: ModelLoader? = null
    private var initialized = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)
        obtainViewModel()

        val progressOverlay = progress_overlay
        // Show progress overlay (with animation):
        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 0)
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
       // theTextView.text = "$id%"

        Log.d("olly", "$numOfModelsLoadedTotal")
        if (numOfModelsLoadedTotal == 100) {

            Log.d("olly", "gets here")
            setupAR()


        }
        if (numOfModelsLoadedTotal % batchSize == 0) {
            loadBatchModels()
        }
    }


    private fun setupAR(){

        val progressOverlay = progress_overlay
        AndroidUtils.animateView(progressOverlay, View.GONE, 0f, 200)

        val ux_fragment = ArFragment()
        val fm: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fm.beginTransaction()
        fragmentTransaction.add(R.id.frag_container, ux_fragment)
        fragmentTransaction.commit()

        arFragment = ux_fragment as ArFragment?

        frameListener()
    }


    private fun obtainViewModel(): SharedViewModel {
        return ViewModelProvider(this).get(SharedViewModel::class.java)
    }

    private fun frameListener() {

        // Add a frame update listener to the scene to decide when to show/hide plane detection.
        arFragment?.arSceneView?.scene?.addOnUpdateListener {

            if (!initialized) {
                if (arFragment?.arSceneView?.session != null) {
                    disablePlaneFinding()
                    initialized = true
                }
            }
        }
    }

    private fun disablePlaneFinding() {

        if (arFragment?.arSceneView?.session != null) {

            arFragment?.planeDiscoveryController?.hide()
            arFragment?.planeDiscoveryController?.setInstructionView(null)
            arFragment?.arSceneView?.planeRenderer?.isEnabled = false


            val config = Config(arFragment?.arSceneView?.session)
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            config.planeFindingMode = Config.PlaneFindingMode.DISABLED
            arFragment?.arSceneView?.session?.configure(config)
        }
    }
}
