package com.sonde.mentalfitness.presentation.ui.textIndependent

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.presentation.ui.textIndependent.EnrollerView.State.*
import com.sonde.mentalfitness.presentation.utils.replaceWithFragment

class EnrollerFragment : Fragment(R.layout.ti_enroller_fragment) {

    private lateinit var enrollerView: EnrollerView
    private lateinit var backButton: ImageView
    private lateinit var textView_time_count: TextView

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModel: EnrollerViewModel by viewModels()
    val sharedPreferenceHelper =
        SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!viewModel.isInitialized) {
            viewModel.init(
                requireContext(),
                sharedViewModel.voiceTemplateFactory,
                sharedViewModel.templateFileCreator,
                sharedViewModel.speechSummaryEngine
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get views
        enrollerView = view.findViewById(R.id.enrollmentView)
        backButton = view.findViewById(R.id.tiEnrollBackButton)
        textView_time_count = view.findViewById(R.id.textView_time_count)

        // Set listeners/observers
        viewModel.warningMessageForUser.observe(viewLifecycleOwner) { message ->
            message ?: return@observe

            Toast.makeText(requireContext(), message, LENGTH_LONG).show()
        }

        viewModel.enrollmentProgress.observe(viewLifecycleOwner) { progress ->
            enrollerView.setProgress(progress)
            val seconds=(progress*10)/100
            textView_time_count.text="${seconds}s"
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            state ?: return@observe

            // To animate view changes
            TransitionManager.beginDelayedTransition(requireView() as ViewGroup)

            enrollerView.state = state
            backButton.isVisible = (state == Record)

            if (state == ProcessIsFinished) {
                sharedPreferenceHelper.setVoiceEnrollmentDone()
//                activity?.setResult(AppCompatActivity.RESULT_OK)
//                // Clear back stack
//                parentFragmentManager.popBackStack(null, POP_BACK_STACK_INCLUSIVE)
//                activity?.onBackPressed()

                // Go to start fragment
                replaceWithFragment(R.id.main_container, EnrollmentSuccessFragment(), false)
            }
        }

        backButton.setOnClickListener {
            it.isClickable = false
            requireActivity().onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()

        if (viewModel.audioRecordIsPaused) {
            viewModel.resumeRecord()
        } else {
            viewModel.startRecord()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.pauseRecord()
    }

    companion object {
        private val TAG = EnrollerFragment::class.simpleName
    }
}
