package io.github.szczepanskikrs.rxnodeanimators

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.NodeParent
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.math.Vector3Evaluator
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import java.util.concurrent.atomic.AtomicInteger

object RxNodeAnimators {
    /**
     * Performs rotation animation on given nodes.
     *
     * @param quaternion represents quaternion that will be applied during animation.
     * @param duration time for animation to finish in milliseconds.
     * @param interpolator android interpolator that will be used for animating for more info check
     * https://robots.thoughtbot.com/android-interpolators-a-visual-guide for more info.
     * @param nodes list of nodes that animation will be applied to.
     *
     * @return Completable to be subscribed to. If you want to start multiple transformations at once use
     * Completable.mergeArray(). If you wish to perform animations in a sequence use Completable.andThen().
     */
    fun animateNodeRotation(
        quaternion: Quaternion,
        duration: Long = 1000,
        interpolator: Interpolator = LinearInterpolator(),
        nodes: List<NodeParent>
    ): Completable {
        return Completable.create {
            if (nodes.isEmpty()) {
                it.onComplete()
                return@create
            }
            nodes.forEach { node ->
                animateNode(
                    value = quaternion,
                    evaluator = QuaternionEvaluator(),
                    property = LOC_ROTATION,
                    animationInterpolator = interpolator,
                    node = node,
                    animationDuration = duration,
                    emitter = it,
                    targetsCounter = AtomicInteger(nodes.size)
                )
            }
        }
    }

    /**
     * Performs movement animation on given nodes.
     *
     * @param vector represents vector that will be applied during animation.
     * @param duration time for animation to finish in milliseconds.
     * @param interpolator android interpolator that will be used for animating for more info check
     * https://robots.thoughtbot.com/android-interpolators-a-visual-guide for more info.
     * @param nodes list of nodes that animation will be applied to.
     *
     * @return Completable to be subscribed to. If you want to start multiple transformations at once use
     * Completable.mergeArray(). If you wish to perform animations in a sequence use Completable.andThen().
     */
    fun animateNodeMovement(
        vector: Vector3,
        duration: Long = 1000,
        interpolator: Interpolator = LinearInterpolator(),
        nodes: List<NodeParent>
    ): Completable {
        return Completable.create {
            if (nodes.isEmpty()) {
                it.onComplete()
                return@create
            }
            nodes.forEach { node ->
                animateNode(
                    value = vector,
                    evaluator = Vector3Evaluator(),
                    property = LOC_POSITION,
                    animationInterpolator = interpolator,
                    node = node,
                    animationDuration = duration,
                    emitter = it,
                    targetsCounter = AtomicInteger(nodes.size)
                )
            }
        }
    }

    /**
     * Performs scale animation on given nodes.
     *
     * @param vector represents vector that will be applied during animation.
     * @param duration time for animation to finish in milliseconds.
     * @param interpolator android interpolator that will be used for animating for more info check
     * https://robots.thoughtbot.com/android-interpolators-a-visual-guide for more info.
     * @param nodes list of nodes that animation will be applied to.
     *
     * @return Completable to be subscribed to. If you want to start multiple transformations at once use
     * Completable.mergeArray(). If you wish to perform animations in a sequence use Completable.andThen().
     */
    fun animateNodeScale(
        vector: Vector3,
        duration: Long = 1000,
        interpolator: Interpolator = LinearInterpolator(),
        nodes: List<NodeParent>
    ): Completable {
        return Completable.create {
            if (nodes.isEmpty()) {
                it.onComplete()
                return@create
            }
            nodes.forEach { node ->
                animateNode(
                    value = vector,
                    evaluator = Vector3Evaluator(),
                    property = LOC_SCALE,
                    animationInterpolator = interpolator,
                    node = node,
                    animationDuration = duration,
                    emitter = it,
                    targetsCounter = AtomicInteger(nodes.size)
                )
            }
        }
    }

    private inline fun ObjectAnimator.onAnimationEnd(crossinline continuation: (Animator) -> Unit) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                continuation(animation)
            }
        })
    }

    private fun animateNode(
        value: Any,
        evaluator: TypeEvaluator<*>,
        property: String,
        animationInterpolator: Interpolator,
        node: NodeParent,
        animationDuration: Long,
        emitter: CompletableEmitter,
        targetsCounter: AtomicInteger
    ) {
        ObjectAnimator().apply {
            setObjectValues(value)
            propertyName = property
            target = node
            duration = animationDuration
            setEvaluator(evaluator)
            interpolator = animationInterpolator
            start()
            onAnimationEnd {
                if (targetsCounter.decrementAndGet() == 0)
                    emitter.onComplete()
            }
        }
    }

    private const val LOC_SCALE = "localScale"
    private const val LOC_POSITION = "localPosition"
    private const val LOC_ROTATION = "localRotation"
}
