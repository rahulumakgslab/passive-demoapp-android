package com.sonde.mentalfitness.presentation.ui.checkIn.feeling

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.FragmentFeelingBinding
import com.sonde.mentalfitness.domain.model.checkin.CheckInConfigModel
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel
import com.sonde.mentalfitness.presentation.ui.checkIn.feeling.adapter.FeelingAdapter
import com.sonde.mentalfitness.presentation.ui.checkIn.feelingreason.FeelingReasonsFragment
import com.sonde.mentalfitness.presentation.ui.record.readytorecord.ReadyToRecordFragment

class FeelingFragment : BaseFragment<FragmentFeelingBinding, FeelingViewModel>(
    layoutId = R.layout.fragment_feeling
) {

    companion object {
        fun newInstance() = FeelingFragment()
    }

    private lateinit var feelingAdapter: FeelingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe(viewModel.checkInConfigModel, ::onViewDataChange)
        observe(viewModel.event, ::onViewEvent)
        observe(viewModel.state, ::onViewStateChange)

        viewModel.getConfigData()
    }

    override fun initViewModel(): FeelingViewModel {
        return ViewModelProvider(this).get(FeelingViewModel::class.java)
    }

    override fun initDependencies() {
        feelingAdapter = FeelingAdapter(viewModel)
    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
        viewBinding.optionsRecyclerView.adapter = feelingAdapter
        val manager = GridLayoutManager(activity, 4)
//        viewBinding.optionsRecyclerView.setHasFixedSize(true)
        viewBinding.optionsRecyclerView.layoutManager = manager
    }

    private fun onViewEvent(viewEvent: FeelingViewEvent) {
        when (viewEvent) {
            is FeelingViewEvent.SelectFeelingOption -> {
                navigateToFeelingReasonFragment(viewEvent.option, viewEvent.configModel)
            }
            is FeelingViewEvent.SkipQuestion -> {
                Toast.makeText(activity, "skip", Toast.LENGTH_SHORT).show()
            }
            is FeelingViewEvent.NavigateToDashboard -> {
                Toast.makeText(activity, "dashboard", Toast.LENGTH_SHORT).show()
            }
            is FeelingViewEvent.HandleBackPress -> {
                Toast.makeText(activity, "back press", Toast.LENGTH_SHORT).show()
            }
            is FeelingViewEvent.NavigateToReadyToRecord -> {
                navigateToReadyToRecord(viewEvent.questionnaireAnswer, viewEvent.passageModel)
            }
        }
    }

    private fun onViewStateChange(viewState: FeelingViewState) {
        when (viewState) {
            is FeelingViewState.Loading ->
                showProgressbar(viewBinding.loadingView.loadingProgressBar)
            is FeelingViewState.ShowError -> {
                hideProgressbar(viewBinding.loadingView.loadingProgressBar)
                showErrorMessage(viewState.messageResId)
            }
            is FeelingViewState.ShowNoInternet -> {
                hideProgressbar(viewBinding.loadingView.loadingProgressBar)
                showNoInternetDialog(R.string.no_internet_connection,
                    R.string.error_msg_no_internet)
            }

            else -> hideProgressbar(viewBinding.loadingView.loadingProgressBar)
        }
    }

    private fun navigateToReadyToRecord(
        questionnaireAnswersModel: QuestionnaireAnswersModel,
        passageModel: PassageModel
    ) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.add(
            R.id.container,
            ReadyToRecordFragment.newInstance(questionnaireAnswersModel, passageModel)
        )
        transaction?.addToBackStack(ReadyToRecordFragment.javaClass.name)
        transaction?.commit()
    }

    private fun onViewDataChange(checkInConfigModel: CheckInConfigModel) {
        updateFeelingQuestionsList(checkInConfigModel.questionnaire.questions[0].options)
    }

    private fun navigateToFeelingReasonFragment(
        option: OptionModel,
        configModel: CheckInConfigModel
    ) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.container,
            FeelingReasonsFragment.newInstance(configModel, option)
        )
        transaction?.addToBackStack(null)?.commit()
    }

    private fun updateFeelingQuestionsList(optionsList: List<OptionModel>) {
        feelingAdapter.setOptionsList(optionsList)
    }

}