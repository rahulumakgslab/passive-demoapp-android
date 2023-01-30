package com.twilio.voipcall.voiceprocessing

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProvider
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.base.presentation.utils.extensions.showAlertDialog
import com.sonde.base.presentation.utils.listener.AlertDialogPositiveButtonListener
import com.twilio.voipcall.R
import com.twilio.voipcall.databinding.WavProcessingFragmentBinding
import com.twilio.voipcall.score.ScoreFragment
import com.twilio.voipcall.voiceprocessing.calculationerror.ScoreCalculationErrorFragment

private const val ARG_AUDIO_FILE_PATH = "audio_file_path"
private const val ARG_PASSAGE_DATA = "passage_data"
private const val REQUEST_CODE = 101

class WavProcessingFragment :
    BaseFragment<WavProcessingFragmentBinding, WavProcessingViewModel>(
        layoutId = R.layout.wav_processing_fragment
    ), AlertDialogPositiveButtonListener {


    companion object {
        fun newInstance(audioFilePath: String) =
            WavProcessingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_AUDIO_FILE_PATH, audioFilePath)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            viewModel.calculateScore(it.getString(ARG_AUDIO_FILE_PATH, ""))
        }
    }

    override fun initViewModel(): WavProcessingViewModel {
        return ViewModelProvider(this).get(WavProcessingViewModel::class.java)
    }

    override fun initDependencies() {

    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe(viewModel.voiceProcessingStatus, ::onViewDataChange)
        observe(viewModel.state, ::onViewStateChange)
        observe(viewModel.event, ::onViewEvent)
    }

    private fun onViewDataChange(voiceProcessingStatus: Int) {
        viewBinding.textViewOldStatus.text = viewBinding.textViewNewStatus.text
        viewBinding.textViewNewStatus.text = activity?.getString(voiceProcessingStatus)

        val bottomToTop: Animation =
            AnimationUtils.loadAnimation(activity, R.anim.slide_up_fade_out)
        viewBinding.textViewOldStatus.startAnimation(bottomToTop)
        val bottomToTop1: Animation =
            AnimationUtils.loadAnimation(activity, R.anim.slide_up_fade_in)
        viewBinding.textViewNewStatus.startAnimation(bottomToTop1)
    }

    private fun onViewStateChange(viewState: WavProcessingViewState) {
    }

    private fun onViewEvent(viewEvent: WavProcessingViewEvent) {
        when (viewEvent) {
            is WavProcessingViewEvent.OnWavProcessSuccess -> {
                navigateToScoreFragment(
                    viewEvent.score
                )
            }
            is WavProcessingViewEvent.OnWavProcessError -> {
                showErrorScreen(viewEvent.mainCheckInHealthCheckError)
            }
            is WavProcessingViewEvent.OnElckFailed -> {
                activity?.showAlertDialog(
                    requestCode = REQUEST_CODE,
                    messageResId = R.string.error_message_elck_failed,
                    alertDialogPositiveButtonListener = this
                )
            }

        }
    }

    private fun navigateToScoreFragment(
        score: Int
    ) {
        val scoreFragmentIntent = ScoreFragment.newInstance(
            score
        )

        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, scoreFragmentIntent)?.commit()

    }

    private fun showErrorScreen(mainCheckInHealthCheckError: String) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.container,
            ScoreCalculationErrorFragment.newInstance(mainCheckInHealthCheckError)
        )
        transaction?.commit()
    }

    override fun OnPositiveButtonClicked(requestCode: Int) {
        navigateToCheckIn()
    }

    private fun navigateToCheckIn() {
        activity?.finish()
//        val checkInHostActivityIntent = Intent(activity, CheckInHostActivity::class.java)
//        checkInHostActivityIntent.flags =
//            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(checkInHostActivityIntent)
    }

}