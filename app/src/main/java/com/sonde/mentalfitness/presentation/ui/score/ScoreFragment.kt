package com.sonde.mentalfitness.presentation.ui.score

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.ScoreFragmentBinding
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.domain.model.checkin.SubmitSessionModel
import com.sonde.mentalfitness.domain.model.checkin.score.ScoreFactorAdapterModel
import com.sonde.mentalfitness.domain.model.checkin.score.ScoreRepresentationModel
import com.sonde.mentalfitness.domain.model.checkin.score.TipModel
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.base.presentation.utils.extensions.showAlertDialog
import com.sonde.mentalfitness.presentation.ui.MainActivity
import com.sonde.mentalfitness.presentation.ui.checkIn.CheckInHostActivity
import com.sonde.mentalfitness.presentation.ui.score.adapter.ScoreFactorAdapter
import com.sonde.mentalfitness.presentation.ui.score.adapter.TipsAdapter
import java.util.*


private const val ARG_SELECTED_FEELING_ANSWER_OPTION = "selected_feeling_answer_option"
private const val ARG_SELECTED_FEELING_REASON_LIST = "selected_feeling_reason_list"
private const val ARG_SESSION_DATA = "session_data"

class ScoreFragment : BaseFragment<ScoreFragmentBinding, ScoreViewModel>(
    layoutId = R.layout.score_fragment
) {

    lateinit var scoreFactorAdapter: ScoreFactorAdapter
    lateinit var tipAdapter: TipsAdapter

    companion object {
        fun newInstance(
            sessionData: SubmitSessionModel,
            selectedFeelingAnswerOption: OptionModel?,
            selectedFeelingReasonList: ArrayList<OptionModel>?
        ) = ScoreFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_SESSION_DATA, sessionData)
                putParcelable(ARG_SELECTED_FEELING_ANSWER_OPTION, selectedFeelingAnswerOption)
                putParcelableArrayList(ARG_SELECTED_FEELING_REASON_LIST, selectedFeelingReasonList)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            val sessionData: SubmitSessionModel? = it.getParcelable(ARG_SESSION_DATA)
            val selectedFeelingAnswerOption: OptionModel? =
                it.getParcelable(ARG_SELECTED_FEELING_ANSWER_OPTION)
            val selectedFeelingReasonList: ArrayList<OptionModel>? =
                it.getParcelableArrayList(ARG_SELECTED_FEELING_REASON_LIST)

            if (sessionData != null) {
                viewModel.prepareScoreRepresentationModel(
                    sessionData,
                    selectedFeelingAnswerOption,
                    selectedFeelingReasonList
                )
            }
        }
    }

    override fun initViewModel(): ScoreViewModel {
        return ViewModelProvider(this).get(ScoreViewModel::class.java)
    }

    override fun initDependencies() {
        scoreFactorAdapter = ScoreFactorAdapter()
        tipAdapter = TipsAdapter()
    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
        viewBinding.scoreRepresentationModel = viewModel.scoreRepresentationModel.value

        viewBinding.recyclerViewFactors.adapter = scoreFactorAdapter
        val manager = LinearLayoutManager(activity)
        viewBinding.recyclerViewFactors.layoutManager = manager

        viewBinding.viewpagerTips.adapter = tipAdapter
        viewBinding.viewpagerTips.setPadding(130, 0, 130, 0)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe(viewModel.scoreRepresentationModel, ::onViewDataChange)
        observe(viewModel.tipModelList, ::onViewTipDataChange)
        observe(viewModel.event, ::onViewEvent)
    }

    private fun onViewDataChange(scoreRepresentationModel: ScoreRepresentationModel) {
        updateScoreFactorsList(scoreRepresentationModel.scoreFactorAdapterList)
    }

    private fun onViewTipDataChange(tipsList: List<TipModel>) {
        tipAdapter.updateTipsList(tipsList)
    }

    private fun updateScoreFactorsList(scoreFactorList: List<ScoreFactorAdapterModel>) {
        scoreFactorAdapter.setScoreFactorsList(scoreFactorList)
    }

    private fun onViewEvent(viewEvent: ScoreViewEvent) {
        when (viewEvent) {
            is ScoreViewEvent.OnDoneClicked -> {
                onDoneButtonClicked()
            }
            is ScoreViewEvent.OnScoreInfoClicked -> {
                showScoreInfoDialog()
            }
        }
    }

    private fun showScoreInfoDialog() {
        activity?.showAlertDialog(
            titleResId = R.string.score_info,
            messageResId = R.string.score_info_detail
        )
    }

    private fun onDoneButtonClicked() {
        navigateToCheckIn()
    }

    private fun navigateToCheckIn() {
        activity?.finish()
        val checkInHostActivityIntent = Intent(activity, MainActivity::class.java)
        checkInHostActivityIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(checkInHostActivityIntent)
    }
}