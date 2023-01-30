package com.twilio.voipcall.score

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.base.presentation.utils.extensions.showAlertDialog
import com.twilio.voipcall.R
import com.twilio.voipcall.databinding.VoipScoreFragmentBinding


private const val ARG_SESSION_DATA = "session_data"

class ScoreFragment : BaseFragment<VoipScoreFragmentBinding, ScoreViewModel>(
    layoutId = R.layout.voip_score_fragment
) {


    companion object {
        fun newInstance(
            score: Int
        ) = ScoreFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_SESSION_DATA, score)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            val score: Int = it.getInt(ARG_SESSION_DATA)
            if (score >= 0) {
                viewModel.prepareScoreRepresentationModel(
                    score
                )
            }
        }
    }

    override fun initViewModel(): ScoreViewModel {
        return ViewModelProvider(this).get(ScoreViewModel::class.java)
    }

    override fun initDependencies() {
//        scoreFactorAdapter = ScoreFactorAdapter()
//        tipAdapter = TipsAdapter()
    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
        viewBinding.scoreRepresentationModel = viewModel.scoreRepresentationModel.value

//        viewBinding.recyclerViewFactors.adapter = scoreFactorAdapter
//        val manager = LinearLayoutManager(activity)
//        viewBinding.recyclerViewFactors.layoutManager = manager
//
//        viewBinding.viewpagerTips.adapter = tipAdapter
//        viewBinding.viewpagerTips.setPadding(130, 0, 130, 0)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        observe(viewModel.scoreRepresentationModel, ::onViewDataChange)
//        observe(viewModel.tipModelList, ::onViewTipDataChange)
        observe(viewModel.event, ::onViewEvent)
    }

//    private fun onViewDataChange(scoreRepresentationModel: ScoreRepresentationModel) {
//        updateScoreFactorsList(scoreRepresentationModel.scoreFactorAdapterList)
//    }

//    private fun onViewTipDataChange(tipsList: List<TipModel>) {
//        tipAdapter.updateTipsList(tipsList)
//    }
//
//    private fun updateScoreFactorsList(scoreFactorList: List<ScoreFactorAdapterModel>) {
//        scoreFactorAdapter.setScoreFactorsList(scoreFactorList)
//    }

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
//        val checkInHostActivityIntent = Intent(activity, CheckInHostActivity::class.java)
//        checkInHostActivityIntent.flags =
//            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(checkInHostActivityIntent)
    }
}