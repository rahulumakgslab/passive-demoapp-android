package com.sonde.mentalfitness.presentation.ui.record.readytorecord

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.ReadyToRecordFragmentBinding
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.presentation.ui.record.recording.RecordingHostActivity


private const val ARG_QUESTIONNAIRE_ANSWERS = "questionnaire_answer"
private const val ARG_PASSAGE = "passage_data"
private const val RECORD_REQUEST_CODE = 101

class ReadyToRecordFragment : BaseFragment<ReadyToRecordFragmentBinding, ReadyToRecordViewModel>(
    layoutId = R.layout.ready_to_record_fragment
) {

    private var mQuestionnaireAnswer: QuestionnaireAnswersModel? = null
    private var mPassageModel: PassageModel? = null

    private val TAG = ReadyToRecordFragment::class.java.name


    companion object {
        fun newInstance(
            questionnaireAnswer: QuestionnaireAnswersModel,
            passageModel: PassageModel
        ) =
            ReadyToRecordFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_QUESTIONNAIRE_ANSWERS, questionnaireAnswer)
                    putParcelable(ARG_PASSAGE, passageModel)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mQuestionnaireAnswer = it.getParcelable(ARG_QUESTIONNAIRE_ANSWERS)
            mPassageModel = it.getParcelable(ARG_PASSAGE)
            viewModel.setAnswersData(mQuestionnaireAnswer)
            viewModel.setPassage(mPassageModel!!)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe(viewModel.event, ::onViewEvent)
        observe(viewModel.state, ::onViewStateChange)
    }

    private fun onViewEvent(viewEvent: ReadyToRecordViewEvent) {
        when (viewEvent) {
            is ReadyToRecordViewEvent.OnBeginRecordingClicked -> {
                viewModel.submitQuestionnaireAnswer()
            }
            is ReadyToRecordViewEvent.HandleBackPress -> {
                activity?.onBackPressed()
            }
            is ReadyToRecordViewEvent.OnQuestionnaireAnswerSubmitted -> {
                checkMicroPhonePermission()
            }

        }
    }

    private fun navigateToCountDownScreen(passageModel: PassageModel?) {
        activity?.let {
            startActivity(
                RecordingHostActivity.newInstanceCountdownMode(
                    it,
                    passageModel!!
                )
            )
        }
    }

    private fun onViewStateChange(viewState: ReadyToRecordViewState) {
        when (viewState) {
            is ReadyToRecordViewState.Loading ->
                showProgressbar(viewBinding.loadingView.loadingProgressBar)
            is ReadyToRecordViewState.ShowError -> {
                hideProgressbar(viewBinding.loadingView.loadingProgressBar)
                showErrorMessage(viewState.messageResId)
            }
            is ReadyToRecordViewState.ShowNoInternet -> {
                hideProgressbar(viewBinding.loadingView.loadingProgressBar)
                showNoInternetDialog(R.string.no_internet_connection,
                    R.string.error_msg_no_internet)
            }

            else -> hideProgressbar(viewBinding.loadingView.loadingProgressBar)
        }
    }

    override fun initViewModel(): ReadyToRecordViewModel {
        return ViewModelProvider(this).get(ReadyToRecordViewModel::class.java)
    }

    override fun initDependencies() {
    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
    }


    private fun checkMicroPhonePermission() {
        val context: Context = this.context ?: return
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                navigateToCountDownScreen(mPassageModel)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Log.i(TAG, "shouldShowRequestPermissionRationale")
                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_REQUEST_CODE
                )
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Microphone permission has been denied by user")
                } else {
                    Log.i(TAG, "Microphone permission has been granted by user")
                    navigateToCountDownScreen(mPassageModel)
                }
            }
        }
    }


}