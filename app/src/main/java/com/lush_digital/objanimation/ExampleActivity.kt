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
import kotlinx.coroutines.withContext


class ExampleActivity : AppCompatActivity() {

    var arFragment: ArFragment? = null
    var map: MutableMap<String, ModelRenderable?> = HashMap()
    val id = ArrayList<Int>()
    var anchorNode: AnchorNode? = null
    var firstKWFrame: SkeletonNode? = null
    var animationFrame: SkeletonNode? = null
    val delayTime = 100L
    var frameNumber: Int = 1
    var uniqueId: Int = 1
    var modelLoaderCount = 0
    var userResetAnimation = false
    var pausedClicked = false
    var pausedFrame = 0
    var resumeAnimation = false

    private var modelLoader: ModelLoader? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        init()
    }


    fun init() {

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?

        getAllRawFiles()
        placeNode()
        animate()
        restartAnimation()
        pauseAnimation()

        modelLoader = ModelLoader(this)

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

        arFragment?.setOnTapArPlaneListener { hitResult: com.google.ar.core.HitResult?, plane: com.google.ar.core.Plane?, motionEvent: MotionEvent? ->

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

        firstKWFrame = SkeletonNode()
        firstKWFrame?.setParent(anchorNode)
        firstKWFrame?.renderable = map["kw1"]

    }

    private fun animate() {

        btn_animate.setOnClickListener {

            userResetAnimation = false

            if (modelLoaderCount != 99) {

                snackbar("Loading Models $modelLoaderCount/100, Please Wait...")

            } else {

                btn_animate.hide()
                btn_pause.show()

                if (pausedClicked) {

                    resumeAnimations()



                } else {

                    Handler().postDelayed({
                        anchorNode?.removeChild(firstKWFrame)
                        animateFrames()
                    }, delayTime)
                }
            }
        }
    }


    private fun animateFrames() {

        if (!pausedClicked) {

            if (frameNumber == 100 && !userResetAnimation) {

                animationFrame = SkeletonNode()
                animationFrame?.setParent(anchorNode)

                frameNumber = 1
                animationFrame?.renderable = map["kw1"]

            } else if (!userResetAnimation) {

                animationFrame = SkeletonNode()
                animationFrame?.setParent(anchorNode)

                animationFrame?.renderable = map["kw$frameNumber"]

            }

            if (!userResetAnimation) {


                Handler().postDelayed({
                    anchorNode?.removeChild(animationFrame)
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
            anchorNode?.removeChild(animationFrame)
            callFirstFrame()

        }
    }

    fun pauseAnimation() {

        btn_pause.setOnClickListener {

            btn_pause.hide()
            btn_animate.show()
            pausedClicked = true
            pausedFrame = frameNumber

            animationFrame = SkeletonNode()
            animationFrame?.setParent(anchorNode)
            animationFrame?.renderable = map["kw$pausedFrame"]
        }
    }

    fun resumeAnimations(){

        if (!userResetAnimation) {

            resumeAnimation = true
            anchorNode?.removeChild(animationFrame)
            resumeFromPausedFrame()

        }
    }

    private fun resumeFromPausedFrame() {

        if (!userResetAnimation && resumeAnimation) {

            animationFrame = SkeletonNode()
            animationFrame?.setParent(anchorNode)

            animationFrame?.renderable = map["kw$pausedFrame"]

        }
        if (resumeAnimation) {

            Handler().postDelayed({
                anchorNode?.removeChild(animationFrame)
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

}
