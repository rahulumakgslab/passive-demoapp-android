package com.sonde.mentalfitness.presentation.ui.record.recording

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.RecordingFragmentBinding
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.presentation.ui.voiceprocessing.VoiceProcessingFragment

private const val ARG_PASSAGE_DATA = "passage_data"

class RecordingFragment : BaseFragment<RecordingFragmentBinding, RecordingViewModel>(
    layoutId = R.layout.recording_fragment
) {

    private var passageModel: PassageModel? = null

    companion object {
        fun newInstance(passageModel: PassageModel) = RecordingFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PASSAGE_DATA, passageModel)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            passageModel = it.getParcelable(ARG_PASSAGE_DATA)
            passageModel?.let {
                viewModel.setPassage(passageModel!!)
            }
        }
    }

    override fun initViewModel(): RecordingViewModel {
        return ViewModelProvider(this).get(RecordingViewModel::class.java)
    }

    override fun initDependencies() {

    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)
        observe(viewModel.event, ::onViewEvent)

        viewModel.initRecording()
    }

    private fun onViewEvent(viewEvent: RecordingViewEvent) {
        when (viewEvent) {
            is RecordingViewEvent.OnRecordingInterrupted -> {
                navigateToBack()
            }
            is RecordingViewEvent.OnCancelClicked -> {
                handleCancelClicked()
            }
            is RecordingViewEvent.OnRecordingFinished -> {
                launchVoiceProcessing(viewEvent.audioFilePath, viewEvent.passageModel)
            }
        }
    }

    private fun launchVoiceProcessing(audioFilePath: String, passageModel: PassageModel) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.container,
            VoiceProcessingFragment.newInstance(audioFilePath, passageModel)
        )
        transaction?.commit()
    }

    private fun handleCancelClicked() {
        activity?.onBackPressed()
    }

    private fun navigateToBack() {
        Log.d("!!", "navigateToBack: ")
        activity?.finish()
    }

}