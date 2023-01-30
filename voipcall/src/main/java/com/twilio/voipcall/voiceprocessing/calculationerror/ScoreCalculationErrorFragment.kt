package com.twilio.voipcall.voiceprocessing.calculationerror

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.twilio.voipcall.R
import com.twilio.voipcall.databinding.ScoreCalculationErrorFragmentBinding


private const val ARG_ERROR_MESSAGE = "error_message"

class ScoreCalculationErrorFragment :
    BaseFragment<ScoreCalculationErrorFragmentBinding, ScoreCalculationErrorViewModel>(
        layoutId = R.layout.score_calculation_error_fragment
    ) {

    companion object {
        fun newInstance(errorMessage: String) =
            ScoreCalculationErrorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ERROR_MESSAGE, errorMessage)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val errorMessage: String? = it.getString(ARG_ERROR_MESSAGE)
            errorMessage?.let {
                viewModel.setErrorMessage(errorMessage)
            }
        }
    }

    override fun initViewModel(): ScoreCalculationErrorViewModel {
        return ViewModelProvider(this).get(ScoreCalculationErrorViewModel::class.java)
    }

    override fun initDependencies() {

    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe(viewModel.event, ::onViewEvent)
    }

    private fun onViewEvent(viewEvent: ScoreCalculationErrorViewEvent) {
        when (viewEvent) {
            is ScoreCalculationErrorViewEvent.OnGoBackAndTryAgain -> {
                navigateToCheckIn()
            }
            is ScoreCalculationErrorViewEvent.OnCancel -> {
                navigateToDashboard()
            }
        }
    }

    private fun navigateToDashboard() {
        Toast.makeText(activity, "navigateToDashboard", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCheckIn() {
//        activity?.finish()
//        val checkInHostActivityIntent = Intent(activity, CheckInHostActivity::class.java)
//        checkInHostActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(checkInHostActivityIntent)
    }
}