package com.sonde.mentalfitness.presentation.ui.pin.set

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.FragmentSetPinBinding
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.presentation.ui.MainActivity
import com.sonde.mentalfitness.presentation.ui.checkIn.CheckInHostActivity

class SetPinFragment :
    BaseFragment<FragmentSetPinBinding, SetPinViewModel>(layoutId = R.layout.fragment_set_pin) {

    companion object {
        fun newInstance() = SetPinFragment()
    }

    override fun initViewModel(): SetPinViewModel {
        return ViewModelProvider(this).get(SetPinViewModel::class.java)
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

    private fun onViewStateChange(viewState: SetPinViewState) {
        when (viewState) {
            is SetPinViewState.ShowError -> {
                showErrorMessage(viewState.messageResId)
            }
        }
    }

    private fun onViewEvent(viewEvent: SetPinViewEvent) {
        when (viewEvent) {
            is SetPinViewEvent.PinSetComplete -> {
                launchCheckInScreen()
            }
            is SetPinViewEvent.NoPinSet -> {
                launchCheckInScreen()
            }
        }
    }

    private fun launchCheckInScreen() {
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }
}