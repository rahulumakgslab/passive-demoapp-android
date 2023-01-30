package com.sonde.mentalfitness.presentation.ui.record.countdown

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProvider
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.FragmentCountDownBinding

import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.presentation.ui.record.recording.RecordingFragment


private const val ARG_PASSAGE_DATA = "passage_data"
private const val COUNT_DOWN_FROM = 3

open class CountDownFragment : BaseFragment<FragmentCountDownBinding, CountDownViewModel>(
    layoutId = R.layout.fragment_count_down
) {

    open var passageModel: PassageModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            passageModel = it.getParcelable(ARG_PASSAGE_DATA)
            passageModel?.let {
                viewModel.setPassage(passageModel!!)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(passageModel: PassageModel) =
            CountDownFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PASSAGE_DATA, passageModel)
                }
            }
    }

    override fun initViewModel(): CountDownViewModel {
        return ViewModelProvider(this).get(CountDownViewModel::class.java)
    }

    override fun initDependencies() {

    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe(viewModel.event, ::onViewEvent)
        startCountdownAnimation(COUNT_DOWN_FROM)
    }

    private fun onViewEvent(viewEvent: CountDownViewEvent) {
        when (viewEvent) {
            is CountDownViewEvent.OnCancelClicked -> {
                handleCancelClicked()
            }
        }
    }

    private fun handleCancelClicked() {
        activity?.onBackPressed()
    }

    private fun startCountdownAnimation(countDownFrom: Int) {
        var count = countDownFrom
        viewBinding.textViewCount.text = count.toString()
        val zoomOutAnimation = AnimationUtils.loadAnimation(activity, R.anim.zoom_out)
        viewBinding.textViewCount.startAnimation(zoomOutAnimation)

        zoomOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                if (count > 0) {
                    viewBinding.textViewCount.text = count.toString()
                    zoomOutAnimation.start()
                    count--
                } else if (count < 0) {
                    onCountDownAnimationFinish()
                } else {
                    count--
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        })
    }

    open fun onCountDownAnimationFinish() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(R.id.container, RecordingFragment.newInstance(passageModel!!))
        transaction?.commit()
    }
}