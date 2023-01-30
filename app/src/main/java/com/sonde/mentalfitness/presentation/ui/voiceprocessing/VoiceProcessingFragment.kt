package com.sonde.mentalfitness.presentation.ui.voiceprocessing

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProvider
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.base.presentation.utils.extensions.showAlertDialog
import com.sonde.base.presentation.utils.listener.AlertDialogPositiveButtonListener
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.VoiceProcessingFragmentBinding
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.domain.model.checkin.SubmitSessionModel
import com.sonde.mentalfitness.presentation.ui.checkIn.CheckInHostActivity
import com.sonde.mentalfitness.presentation.ui.score.MFScoreFragment
import com.sonde.mentalfitness.presentation.ui.score.ScoreFragment
import com.sonde.mentalfitness.presentation.ui.voiceprocessing.calculationerror.ScoreCalculationErrorFragment
import com.sondeservices.edge.ml.model.VFFinalScore
import java.util.*

private const val ARG_AUDIO_FILE_PATH = "audio_file_path"
private const val ARG_PASSAGE_DATA = "passage_data"
private const val REQUEST_CODE = 101

class VoiceProcessingFragment :
    com.sonde.base.presentation.BaseFragment<VoiceProcessingFragmentBinding, VoiceProcessingViewModel>(
        layoutId = R.layout.voice_processing_fragment
    ), AlertDialogPositiveButtonListener {


    companion object {
        fun newInstance(audioFilePath: String, passageModel: PassageModel) =
            VoiceProcessingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_AUDIO_FILE_PATH, audioFilePath)
                    putParcelable(ARG_PASSAGE_DATA, passageModel)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val passageModel: PassageModel? = it.getParcelable(ARG_PASSAGE_DATA)
            passageModel?.let {
                viewModel.setSelectedPassage(it)
            }
            viewModel.calculateScore(it.getString(ARG_AUDIO_FILE_PATH, ""))
        }
    }

    override fun initViewModel(): VoiceProcessingViewModel {
        return ViewModelProvider(this).get(VoiceProcessingViewModel::class.java)
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

    private fun onViewStateChange(viewState: VoiceProcessingViewState) {
    }

    private fun onViewEvent(viewEvent: VoiceProcessingViewEvent) {
        when (viewEvent) {
            is VoiceProcessingViewEvent.OnVoiceProcessSuccess -> {
                navigateToScoreFragment(
                    viewEvent.sessionData,
                    viewEvent.selectedFeelingAnswerOption,
                    viewEvent.selectedFeelingReasonList
                )

            }
            is VoiceProcessingViewEvent.OnMFScoreSuccess -> {
                navigateToMFScore(viewEvent.sessionData, viewEvent.vfFinalScore)
            }
            is VoiceProcessingViewEvent.OnVoiceProcessError -> {
                showErrorScreen(viewEvent.mainCheckInHealthCheckError)
            }
            is VoiceProcessingViewEvent.OnElckFailed -> {
                activity?.showAlertDialog(
                    requestCode = REQUEST_CODE,
                    messageResId = R.string.error_message_elck_failed,
                    alertDialogPositiveButtonListener = this
                )
            }

        }
    }

    private fun navigateToMFScore(sessionData: SubmitSessionModel, vfFinalScore: VFFinalScore) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, MFScoreFragment.newInstance(sessionData, vfFinalScore))
            ?.commit()
    }

    private fun navigateToScoreFragment(
        sessionData: SubmitSessionModel,
        selectedFeelingAnswerOption: OptionModel?,
        selectedFeelingReasonList: ArrayList<OptionModel>?
    ) {
        val scoreFragmentIntent = ScoreFragment.newInstance(
            sessionData,
            selectedFeelingAnswerOption,
            selectedFeelingReasonList
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
        val checkInHostActivityIntent = Intent(activity, CheckInHostActivity::class.java)
        checkInHostActivityIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(checkInHostActivityIntent)
    }

}