package com.sonde.mentalfitness.presentation.ui.record.foregroundrecording

import android.os.Bundle
import android.view.View
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.presentation.ui.record.countdown.CountDownFragment
import com.sonde.mentalfitness.presentation.utils.replaceWithFragment

class PassiveForegroundCountDownFragment : CountDownFragment() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        invisiblePrompt()
    }
    companion object {
        private const val ARG_PASSAGE_DATA = "passage_data"
        private const val COUNT_DOWN_FROM = 3
        @JvmStatic
        fun newInstance(passageModel: PassageModel) =
            PassiveForegroundCountDownFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PASSAGE_DATA, passageModel)
                }
            }
    }

    private fun invisiblePrompt() {
        viewBinding.textView17.visibility=View.GONE
        viewBinding.textViewPassage.visibility=View.VISIBLE
    }

    override fun onCountDownAnimationFinish() {
        replaceWithFragment(R.id.main_container, ForegroundRecordingFragment(), false)
    }
}