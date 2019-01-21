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