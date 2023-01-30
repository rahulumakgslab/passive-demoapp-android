package com.sonde.mentalfitness.presentation.ui.signup

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.FragmentSignupStepBinding
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.presentation.ui.pin.PinCodeActivity
import com.sonde.mentalfitness.presentation.utils.Common


class SignupStepFragment :
    BaseFragment<FragmentSignupStepBinding, SignupStepViewModel>(layoutId = R.layout.fragment_signup_step) {
    companion object {
        fun newInstance() = SignupStepFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe(viewModel.state, ::onViewStateChange)
        viewBinding.birthYear.setOnClickListener({
            showBirthYearDialog()
        })
    }

    private fun showBirthYearDialog() {
        val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alertBuilder.setTitle(R.string.select_year_of_birth)

        val arrayAdapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.select_dialog_item)
        val years: List<String> = Common.getYears()
        arrayAdapter.addAll(years)
        alertBuilder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }

        alertBuilder.setAdapter(
            arrayAdapter
        ) { dialog, which ->
            val year = arrayAdapter.getItem(which)
            viewBinding.birthYear.setText(year)
        }
        alertBuilder.show()
    }


    override fun initViewModel(): SignupStepViewModel {
        return ViewModelProvider(this).get(SignupStepViewModel::class.java)
    }

    override fun initDependencies() {
        //nothing
    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
    }

    private fun onViewStateChange(viewState: SignupViewState) {
        when (viewState) {
            is SignupViewState.Loading -> {
                showProgressbar(viewBinding.loadingView.root)
            }
            is SignupViewState.ShowError -> {
                hideProgressbar(viewBinding.loadingView.root)
                showErrorMessage(viewState.messageResId)
            }
            is SignupViewState.SignupComplete -> {
                hideProgressbar(viewBinding.loadingView.root)
                launchSetPinScreen()
            }
        }
    }

    private fun launchSetPinScreen() {
        startActivity(PinCodeActivity.newInstance(requireContext(), true))
        requireActivity().finish()
    }

}