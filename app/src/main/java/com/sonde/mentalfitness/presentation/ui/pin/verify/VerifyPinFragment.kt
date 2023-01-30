package com.sonde.mentalfitness.presentation.ui.pin.verify

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.FragmentVerifyPinBinding
import com.sonde.mentalfitness.presentation.ui.MainActivity
import com.sonde.mentalfitness.presentation.ui.checkIn.CheckInHostActivity

class VerifyPinFragment :
    com.sonde.base.presentation.BaseFragment<FragmentVerifyPinBinding, VerifyPinViewModel>(layoutId = R.layout.fragment_verify_pin) {
    companion object {
        fun newInstance() = VerifyPinFragment()
    }

    override fun initViewModel(): VerifyPinViewModel {
        return ViewModelProvider(this).get(VerifyPinViewModel::class.java)
    }

    override fun initDependencies() {

    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe(viewModel.state, ::onViewStateChange)
        observe(viewModel.event, ::onViewEvent)
    }


    private fun onViewStateChange(viewState: VerifyPinViewState) {
        when (viewState) {
            is VerifyPinViewState.ShowError -> {
                showErrorMessage(viewState.messageResId)
            }
        }
    }

    private fun onViewEvent(viewEvent: VerifyPinViewEvent) {
        when (viewEvent) {
            is VerifyPinViewEvent.Verified -> {
                launchMainScreen()
            }
        }
    }

    private fun launchCheckInScreen() {
        startActivity(Intent(requireContext(), CheckInHostActivity::class.java))
        requireActivity().finish()
    }

    private fun launchMainScreen() {
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }
}