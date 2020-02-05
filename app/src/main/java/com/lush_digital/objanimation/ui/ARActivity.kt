package com.lush_digital.objanimation.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.ar.core.Config
import com.google.ar.sceneform.ux.ArFragment
import com.lush_digital.objanimation.R
import com.lush_digital.objanimation.ui.viewmodel.LoadingViewModel
import kotlinx.android.synthetic.main.activity_ar.*


class ARActivity : AppCompatActivity() {

    var arFragment: ArFragment? = null
    private var initialized = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)
        arFragment = ux_fragment as ArFragment?
        init()
    }


    private fun init() {

        frameListener()

    }

    private fun frameListener() {

        // Add a frame update listener to the scene to decide when to show/hide plane detection.
        arFragment?.arSceneView?.scene?.addOnUpdateListener {

            if (!initialized) {
                if (arFragment?.arSceneView?.session != null) {
                    disablePlaneFinding()
                    Log.d("olly", "${LoadingViewModel.getMap()}")
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


/*
    private fun observeViewModel(viewModel:LoadingViewModel) {
        viewModel.myMap.observe(this, Observer {


        })
    }

 */

}
