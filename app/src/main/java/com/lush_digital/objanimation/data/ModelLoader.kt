package com.lush_digital.objanimation.data

import android.util.Log
import android.util.SparseArray
import com.google.ar.sceneform.rendering.ModelRenderable
import com.lush_digital.objanimation.ui.viewmodel.LoadingViewModel
import java.lang.ref.WeakReference
import java.util.concurrent.CompletableFuture

class ModelLoader internal constructor(owner: LoadingViewModel){
    private val futureSet = SparseArray<CompletableFuture<ModelRenderable>>()
    private val owner: WeakReference<LoadingViewModel> = WeakReference(owner)
    /**
     * Starts loading the model specified. The result of the loading is returned asynchrounously via
     * {@link ARPresenter#setRenderable(int, ModelRenderable)} or {@link
     * ARPresenter#onException(int, Throwable)}.
     *
     * <p>Multiple models can be loaded at a time by specifying separate ids to differentiate the
     * result on callback.
     *
     * @param id the id for this call to loadModel.
     * @param resourceId the resource id of the .sfb to load.
     * @return true if loading was initiated.
     */
    fun loadModel(id: Int, resourceId: Int) {

        val activity: LoadingViewModel? = owner.get()

        if (activity == null) {
            Log.d("Error", "Activity is null.  Cannot load model.")
            return
        }
        val future =
            ModelRenderable.builder()
                .setSource(owner.get()?.getApplication(), resourceId)
                .build()
                .thenApply { renderable: ModelRenderable ->
                    setRenderable(id, renderable)
                }
                .exceptionally { throwable: Throwable ->
                    onException(
                        id,
                        throwable
                    )
                }
        if (future != null) {
            futureSet.put(id, future)
        }
    }

    private fun onException(id: Int, throwable: Throwable): ModelRenderable? {

//        owner.get()?.onException(id, throwable)
        futureSet.remove(id)
        return null
    }

    private fun setRenderable(id: Int, modelRenderable: ModelRenderable): ModelRenderable {

        owner.get()?.setRenderable(id, modelRenderable)
        futureSet.remove(id)
        return modelRenderable
    }
}
