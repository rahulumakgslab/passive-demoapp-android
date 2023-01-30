package com.sonde.mentalfitness.presentation.ui.textIndependent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.presentation.ui.MainActivity
import com.sonde.mentalfitness.presentation.ui.record.foregroundrecording.PassiveForegroundRecordingActivity


class EnrollmentSuccessFragment : Fragment(R.layout.enrollment_success_fragment) {

    private lateinit var backButton: ImageView
    private lateinit var recordButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get views
//        backButton = view.findViewById(R.id.noticeOfTiEnrollBackButton)
        recordButton = view.findViewById(R.id.btn_enroll_user)

        // Set listeners/observers
//        backButton.setOnClickListener {
//            it.isClickable = false
//            requireActivity().onBackPressed()
//        }

        recordButton.setOnClickListener {
            // Go to enroller fragment
//            replaceWithFragment(R.id.main_container, EnrollmentNotifierFragment(), true)


            if (activity?.intent?.extras?.getInt(MainActivity.DEMO_TYPE) == MainActivity.REQUEST_CODE_PASSIVE_MODE_FOREGROUND) {
                activity?.finish()
                startActivity(Intent(activity, PassiveForegroundRecordingActivity::class.java))
            } else {
                activity?.setResult(AppCompatActivity.RESULT_OK)
//                // Clear back stack
                parentFragmentManager.popBackStack(null, POP_BACK_STACK_INCLUSIVE)
                activity?.onBackPressed()
            }
        }
    }
}
