package com.sonde.mentalfitness.presentation.ui.howitwork

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.FragmentHowItWorksBinding
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.presentation.ui.checkIn.CheckInHostActivity


class HowItWorksFragment : BaseFragment<FragmentHowItWorksBinding, HowItWorksViewModel>(
    layoutId = R.layout.fragment_how_it_works
) {
    override fun initViewModel(): HowItWorksViewModel {
        return ViewModelProvider(this).get(HowItWorksViewModel::class.java)
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

    private fun onViewEvent(viewEvent: HowItWorksViewEvent) {
        when (viewEvent) {
            is HowItWorksViewEvent.NavigateToCheckIn -> {
                navigateToCheckInScreen()
            }
        }
    }

    private fun navigateToCheckInScreen() {
        activity?.finish()
        val checkInHostActivityIntent = Intent(activity, CheckInHostActivity::class.java)
        checkInHostActivityIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(checkInHostActivityIntent)
    }

}