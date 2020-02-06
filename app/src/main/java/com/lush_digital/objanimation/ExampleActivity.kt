package com.lush_digital.objanimation


import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
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
import com.lush_digital.objanimation.utils.AndroidUtils
import kotlinx.android.synthetic.main.activity_example.*
import kotlinx.android.synthetic.main.include_progress_overlay.*


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
    var userResetAnimation = false
    var pausedClicked = false
    var pausedFrame = 0
    var resumeAnimation = false
    private var initialized = false
    var numOfModelsLoadedTotal = 0
    val batchSize = 4


    private var modelLoader: ModelLoader? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        init()
    }


    fun init() {

        arFragment = ux_fragment as ArFragment?
        frameListener()

    }

    fun initARFunctions() {


        placeNode()
        animate()
        restartAnimation()
        pauseAnimation()
    }


    private fun frameListener() {

        // Add a frame update listener to the scene to decide when to show/hide plane detection.
        arFragment?.arSceneView?.scene?.addOnUpdateListener {

            if (!initialized) {
                if (arFragment?.arSceneView?.session != null) {

                    Log.d("olly", "gets here")
                    disablePlaneFinding()

                    val progressOverlay = progress_overlay
                    AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 0)

                    modelLoader = ModelLoader(this)
                    allRawFiles()
                    loadBatchModels()
                    initialized = true
                }
            }
        }
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
                Log.d("olly", "${i} ${id[i]}")
                modelLoader?.loadModel(i, id[i])
            }
        }
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

    private fun pauseAnimation() {

        btn_pause.setOnClickListener {

            btn_pause.hide()
            btn_animate.show()
            pausedClicked = true
            resumeAnimation = false
            pausedFrame = frameNumber

            resumedAnimationFrame = SkeletonNode()
            resumedAnimationFrame?.setParent(anchorNode)
            resumedAnimationFrame?.renderable = map["kw$pausedFrame"]
        }
    }

    fun resumeAnimations() {

        if (!userResetAnimation) {

            Handler().postDelayed({
                anchorNode?.removeChild(resumedAnimationFrame)
                resumeAnimation = true
                resumeFromPausedFrame()
            }, delayTime)

        }
    }

    private fun resumeFromPausedFrame() {

        if (pausedFrame == 100 && !userResetAnimation && resumeAnimation) {

            resumedAnimationFrame = SkeletonNode()
            resumedAnimationFrame?.setParent(anchorNode)

            pausedFrame = 1
            resumedAnimationFrame?.renderable = map["kw1"]

        } else if (!userResetAnimation && resumeAnimation) {

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
        numOfModelsLoadedTotal++
        map.set("kw$id", renderable)

        theTextView.text = "$id"

        if (numOfModelsLoadedTotal == 100) {

            AndroidUtils.animateView(progress_overlay, View.GONE, 0f, 200)
            initARFunctions()
        }

        if (numOfModelsLoadedTotal % batchSize == 0) {
            loadBatchModels()
        }

    }

    fun onException(id: Int, throwable: Throwable?) {

        snackbar("Unable to load knot wrap renderable $id due to: $throwable")
        Log.e("Error", "Unable to load knot wrap renderable", throwable)
    }


    fun snackbar(
        message: String
    ) {
        Snackbar.make(btn_animate, message, Snackbar.LENGTH_LONG).show()
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


    private fun enablePlaneFinding() {
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
