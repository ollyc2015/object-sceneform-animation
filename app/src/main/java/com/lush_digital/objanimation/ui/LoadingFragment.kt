package com.lush_digital.objanimation.ui


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lush_digital.objanimation.R
import com.lush_digital.objanimation.ui.viewmodel.LoadingViewModel
import com.lush_digital.objanimation.utils.AndroidUtils
import kotlinx.android.synthetic.main.include_progress_overlay.*


class LoadingFragment : Fragment() {


    companion object {
        fun newInstance() = LoadingFragment()
    }

    private lateinit var viewModel: LoadingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.loading_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = activity?.let { ViewModelProvider(it).get(LoadingViewModel::class.java) }!!
        init()
    }


    fun init() {

        AndroidUtils.animateView(progress_overlay, View.VISIBLE, 0.4f, 0)

        initialiseModels()

        viewModel.loadRawFiles()

        //startTime = System.currentTimeMillis()
        viewModel.getModelIDCount()?.observe(viewLifecycleOwner, Observer {

            theTextView.text = "$it"

            if (it > 90) {

                AndroidUtils.animateView(progress_overlay, View.GONE, 0f, 200)

                val intent = Intent(activity, ARActivity::class.java)
                activity?.startActivity(intent)
            }
        })
    }

    private fun initialiseModels() {

        viewModel.loadModel()

    }
}
