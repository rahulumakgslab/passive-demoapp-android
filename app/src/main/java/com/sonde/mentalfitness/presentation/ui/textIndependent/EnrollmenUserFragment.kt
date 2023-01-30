package com.sonde.mentalfitness.presentation.ui.textIndependent

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.presentation.ui.MainActivity
import com.sonde.mentalfitness.presentation.utils.replaceWithFragment


class EnrollmenUserFragment : Fragment(R.layout.enrollment_user_fragment) {

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
        Log.d("EnrollmenUserFragment","DEMO TYPE==>${activity?.intent?.extras?.getInt(MainActivity.DEMO_TYPE)}")
        recordButton.setOnClickListener {
            // Go to enroller fragment
            replaceWithFragment(R.id.main_container, EnrollmentNotifierFragment(), true)
        }
    }
}
