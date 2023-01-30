package com.sonde.mentalfitness.presentation.ui.setting

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProvider
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.FragmentCountDownBinding
import com.sonde.mentalfitness.databinding.FragmentSettingBinding
import com.sonde.mentalfitness.domain.model.checkin.PassageModel


open class SettingFragment : BaseFragment<FragmentSettingBinding, SettingViewModel>(
    layoutId = R.layout.fragment_setting
) {

    open var passageModel: PassageModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            passageModel = it.getParcelable(ARG_PASSAGE_DATA)
//            passageModel?.let {
//                viewModel.setPassage(passageModel!!)
//            }
//        }
    }

    companion object {
        private const val ARG_PASSAGE_DATA = "passage_data"
        @JvmStatic
        fun newInstance(passageModel: PassageModel) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PASSAGE_DATA, passageModel)
                }
            }
    }

    override fun initViewModel(): SettingViewModel {
        return ViewModelProvider(this).get(SettingViewModel::class.java)
    }

    override fun initDependencies() {

    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        observe(viewModel.event, ::onViewEvent)

    }


    private fun handleCancelClicked() {
        activity?.onBackPressed()
    }



}