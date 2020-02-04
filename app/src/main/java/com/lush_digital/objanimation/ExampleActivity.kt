package com.lush_digital.objanimation

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_example.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ExampleActivity : AppCompatActivity() {

    var arFragment: ArFragment? = null
    var map: MutableMap<String, ModelRenderable?> = HashMap()
    val id = ArrayList<Int>()
    var anchorNode: AnchorNode? = null
    var firstAnimationFrame: SkeletonNode? = null
    var animationFrames: SkeletonNode? = null
    var resumedAnimationFrame: SkeletonNode? = null
    val delayTime = 100L
    var frameNumber: Int = 1
    var uniqueId: Int = 1
    var modelLoaderCount = 0
    var userResetAnimation = false
    var pausedClicked = false
    var pausedFrame = 0
    var resumeAnimation = false
    private var initialized = false

    private var modelLoader: ModelLoader? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        arFragment = ux_fragment as ArFragment?
        init()
}



    fun init() {

        frameListener()
        getAllRawFiles()
        placeNode()
        animate()
        restartAnimation()
        pauseAnimation()

        modelLoader = ModelLoader(this)
    }



    private fun frameListener() {

        // Add a frame update listener to the scene to decide when to show/hide plane detection.
        arFragment?.arSceneView?.scene?.addOnUpdateListener {

            if(!initialized) {
                if (arFragment?.arSceneView?.session != null) {
                    disablePlaneFinding()
                    initialized = true
                }
            }
        }
    }


    private fun getAllRawFiles() {

        GlobalScope.launch(Dispatchers.Main) {

            val rawFiles = withContext(Dispatchers.IO) {
                allRawFiles()
            }

            loadModels(rawFiles)

        }
    }

    private fun allRawFiles(): ArrayList<Int> {

        for (i in 1 until 100) {

            id.add(resources.getIdentifier("kw$i", "raw", packageName))

        }

        return id
    }


    private fun loadModels(id: ArrayList<Int>) {

        val handler = Handler()
        handler.postDelayed({

            val myUniqueID = getMyUniqueId()
            modelLoader?.loadModel(myUniqueID, id[modelLoaderCount])
            modelLoaderCount++

            if (modelLoaderCount != 99) {
                loadModels(id)
            }
        }, 250)


    }

    private fun placeNode() {

        arFragment?.setOnTapArPlaneListener { hitResult: HitResult?, plane: Plane?, motionEvent: MotionEvent? ->

            if (map["kw1"] == null) {
                return@setOnTapArPlaneListener
            }

            // Create the Anchor.
            val anchor: Anchor? = hitResult?.createAnchor()
            anchorNode = AnchorNode(anchor)
            anchorNode?.setParent(arFragment!!.arSceneView.scene)

            callFirstFrame()
        }
    }

    private fun callFirstFrame() {

        firstAnimationFrame = SkeletonNode()
        firstAnimationFrame?.setParent(anchorNode)
        firstAnimationFrame?.renderable = map["kw1"]

    }

    private fun animate() {

        btn_animate.setOnClickListener {

            userResetAnimation = false

            if (modelLoaderCount != 99) {

                snackbar("Loading Models $modelLoaderCount/100, Please Wait...")

            } else {

                enablePlaneFinding()

                if (anchorNode != null) {


                    btn_animate.hide()
                    btn_pause.show()

                    if (pausedClicked) {

                        Handler().postDelayed({
                            resumeAnimations()
                        }, delayTime)

                    } else {

                        Handler().postDelayed({
                            anchorNode?.removeChild(firstAnimationFrame)
                            animateFrames()
                        }, delayTime)
                    }
                } else {
                    snackbar("Please Place Knot Wrap before Clicking Play")
                }
            }
        }
    }


    private fun animateFrames() {


        if (!pausedClicked) {

            if (frameNumber == 100 && !userResetAnimation) {

                animationFrames = SkeletonNode()
                animationFrames?.setParent(anchorNode)

                frameNumber = 1
                animationFrames?.renderable = map["kw1"]

            } else if (!userResetAnimation) {

                animationFrames = SkeletonNode()
                animationFrames?.setParent(anchorNode)

                animationFrames?.renderable = map["kw$frameNumber"]

            }

            if (!userResetAnimation) {

                Handler().postDelayed({
                    anchorNode?.removeChild(animationFrames)
                    frameNumber++
                    animateFrames()
                }, delayTime)
            }
        }
    }

    fun restartAnimation() {

        btn_restart.setOnClickListener {

            userResetAnimation = true
            frameNumber = 1
            anchorNode?.removeChild(animationFrames)
            callFirstFrame()

        }
    }

    fun pauseAnimation() {

        btn_pause.setOnClickListener {

            btn_pause.hide()
            btn_animate.show()
            pausedClicked = true
            pausedFrame = frameNumber

            resumedAnimationFrame = SkeletonNode()
            resumedAnimationFrame?.setParent(anchorNode)
            resumedAnimationFrame?.renderable = map["kw$pausedFrame"]
        }
    }

    fun resumeAnimations() {

        if (!userResetAnimation) {

            resumeAnimation = true

            Handler().postDelayed({
                anchorNode?.removeChild(resumedAnimationFrame)
                resumeFromPausedFrame()
            }, delayTime)

        }
    }

    private fun resumeFromPausedFrame() {


        if (!userResetAnimation && resumeAnimation) {

            resumedAnimationFrame = SkeletonNode()
            resumedAnimationFrame?.setParent(anchorNode)

            resumedAnimationFrame?.renderable = map["kw$pausedFrame"]

        }
        if (resumeAnimation) {

            Handler().postDelayed({
                anchorNode?.removeChild(resumedAnimationFrame)
                pausedFrame++
                resumeFromPausedFrame()
            }, delayTime)
        }

    }


    fun setRenderable(id: Int, renderable: ModelRenderable) {

        map.set("kw$id", renderable)

    }

    fun onException(id: Int, throwable: Throwable?) {

        snackbar("Unable to load knot wrap renderable $id due to: $throwable")
        Log.e("Error", "Unable to load knot wrap renderable", throwable)
    }

    fun getMyUniqueId(): Int {
        return uniqueId++
    }

    fun snackbar(
        message: String
    ) {
        Snackbar.make(btn_animate, message, Snackbar.LENGTH_LONG).show()
    }

    fun disablePlaneFinding() {

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


    fun enablePlaneFinding() {
        if (arFragment?.arSceneView?.session != null) {

            arFragment?.planeDiscoveryController?.show()
            val config = Config(arFragment?.arSceneView?.session)
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            arFragment?.arSceneView?.session?.configure(config)
            arFragment?.arSceneView?.planeRenderer?.isEnabled = true
        }
    }
}
