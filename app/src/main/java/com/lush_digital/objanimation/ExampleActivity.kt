package com.lush_digital.objanimation

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    //var animationFrame: SkeletonNode? = null
    val delayTime = 100L
    var frameNumber: Int = 1
    var uniqueId: Int = 1

    private var modelLoader: ModelLoader? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        init()
    }

    init {
        getAllRawFiles()
    }

    fun init() {

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?

        placeNode()
        animate()


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

        for (i in 1 until 70) {

            id.add(resources.getIdentifier("kw$i", "raw", packageName))

        }
        return id
    }


    private fun loadModels(id: ArrayList<Int>) {

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().

        for (i in id.indices) {

        val handler = Handler()
        handler.postDelayed({
            // Actions to do after 1 seconds
            val myUniqueID = getMyUniqueId()
            modelLoader?.loadModel(myUniqueID, id[i])
        }, 1000)





        }
    }

    private fun placeNode() {

        arFragment?.setOnTapArPlaneListener { hitResult: com.google.ar.core.HitResult?, plane: com.google.ar.core.Plane?, motionEvent: MotionEvent? ->
            Log.d("olly", "map: $map")
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

            Handler().postDelayed({
                anchorNode?.removeChild(firstKWFrame)
                animateFrames()
            }, delayTime)

        }
    }


    private fun animateFrames() {

        val animationFrame = SkeletonNode()
        animationFrame.setParent(anchorNode)


        if (frameNumber == 70) {

            frameNumber = 1
            animationFrame.renderable = map["kw1"]

        } else {
            animationFrame.renderable = map["kw$frameNumber"]
        }

        Handler().postDelayed({
            anchorNode?.removeChild(animationFrame)
            frameNumber++
            animateFrames()
        }, delayTime)

    }


    fun setRenderable(id: Int, renderable: ModelRenderable) {

        map.set("kw$id", renderable)

    }

    fun onException(id: Int, throwable: Throwable?) {
        val toast =
            Toast.makeText(this, "Unable to load renderable: $id", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
        Log.e("Error", "Unable to load andy renderable", throwable)
    }

    fun getMyUniqueId(): Int {
        return uniqueId++
    }
}
