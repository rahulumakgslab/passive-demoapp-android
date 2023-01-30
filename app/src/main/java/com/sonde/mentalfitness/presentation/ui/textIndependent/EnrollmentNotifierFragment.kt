package com.sonde.mentalfitness.presentation.ui.textIndependent

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.presentation.ui.record.countdown.CountDownFragment
import com.sonde.mentalfitness.presentation.utils.replaceWithFragment


class EnrollmentNotifierFragment : Fragment(R.layout.enrollment_notifier_fragment) {

    private lateinit var backButton: ImageView
    private lateinit var recordButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get views
        backButton = view.findViewById(R.id.noticeOfTiEnrollBackButton)
        recordButton = view.findViewById(R.id.startRecordButton)

        // Set listeners/observers
        backButton.setOnClickListener {
            it.isClickable = false
            requireActivity().onBackPressed()
        }

        recordButton.setOnClickListener {
            // Go to enroller fragment
            val passageModel=PassageModel("Your enrollment session is about to begin",3)
            replaceWithFragment(R.id.main_container, PassiveCountDownFragment.newInstance(passageModel), true)
        }
    }

}
