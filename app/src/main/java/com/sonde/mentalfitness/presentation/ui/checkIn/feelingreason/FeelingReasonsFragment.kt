package com.sonde.mentalfitness.presentation.ui.checkIn.feelingreason

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.FragmentFeelingReasonsBinding
import com.sonde.mentalfitness.domain.model.checkin.CheckInConfigModel
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel
import com.sonde.mentalfitness.presentation.ui.checkIn.feelingreason.adapter.FeelingReasonAdapter
import com.sonde.mentalfitness.presentation.ui.record.readytorecord.ReadyToRecordFragment

private const val ARG_CHECK_IN_CONFIG = "check_in_config"
private const val ARG_SELECTED_OPTION = "selected_option"

class FeelingReasonsFragment :
    BaseFragment<FragmentFeelingReasonsBinding, FeelingReasonsViewModel>(
        layoutId = R.layout.fragment_feeling_reasons
    ) {

    lateinit var feelingReasonAdapter: FeelingReasonAdapter

    companion object {
        @JvmStatic
        fun newInstance(
            checkInConfigModel: CheckInConfigModel,
            selectedFeelingOption: OptionModel
        ) = FeelingReasonsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_CHECK_IN_CONFIG, checkInConfigModel)
                putParcelable(ARG_SELECTED_OPTION, selectedFeelingOption)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val checkInConfigModel: CheckInConfigModel? = it.getParcelable(ARG_CHECK_IN_CONFIG)
            val selectedFeelingOption: OptionModel? = it.getParcelable(ARG_SELECTED_OPTION)

            viewModel.setData(selectedFeelingOption, checkInConfigModel)
        }

        activity?.window?.getDecorView()
            ?.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        activity?.window?.setStatusBarColor(Color.TRANSPARENT)
    }

    override fun initViewModel(): FeelingReasonsViewModel {
        return ViewModelProvider(this).get(FeelingReasonsViewModel::class.java)
    }

    override fun initDependencies() {
        feelingReasonAdapter = FeelingReasonAdapter(viewModel)
    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel

        viewBinding.feelingReasonOptionsRecyclerView.adapter = feelingReasonAdapter
        viewBinding.feelingReasonOptionsRecyclerView.setHasFixedSize(true);
        val manager = FlexboxLayoutManager(activity)
        manager.justifyContent = JustifyContent.CENTER
        viewBinding.feelingReasonOptionsRecyclerView.layoutManager = manager
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe(viewModel.feelingReasonOptions, ::onViewDataChange)
        observe(viewModel.event, ::onViewEvent)
        observe(viewModel.state, ::onViewStateChange)
    }

    private fun onViewStateChange(viewState: FeelingReasonsViewState) {
        when (viewState) {
            is FeelingReasonsViewState.ShowError -> {
                showErrorMessage(viewState.messageResId)
            }
        }
    }

    private fun onViewDataChange(options: List<OptionModel>) {
        updateFeelingReasonList(options)
    }

    private fun onViewEvent(viewEvent: FeelingReasonViewEvent) {
        when (viewEvent) {
            is FeelingReasonViewEvent.NavigateToReadyToRecord -> {
                navigateToReadyToRecord(
                    viewEvent.questionnaireAnswersModel,
                    viewEvent.passageModel
                )
            }
            is FeelingReasonViewEvent.HandleBackPress -> {
                activity?.onBackPressed()
            }
        }
    }

    private fun navigateToReadyToRecord(
        questionnaireAnswersModel: QuestionnaireAnswersModel,
        passageModel: PassageModel
    ) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        val readyToRecordFragment =
            ReadyToRecordFragment.newInstance(
                questionnaireAnswersModel,
                passageModel
            )
        transaction?.add(R.id.container, readyToRecordFragment)
        transaction?.addToBackStack(ReadyToRecordFragment::class.java.name)
        transaction?.commit()
    }

    private fun updateFeelingReasonList(options: List<OptionModel>) {
        feelingReasonAdapter.setOptionsList(options)
    }
}