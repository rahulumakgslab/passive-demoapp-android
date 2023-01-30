package com.sonde.mentalfitness.presentation.ui.record.foregroundrecording

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.sonde.base.presentation.BaseFragment
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.Constants
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.databinding.ForegroundRecordingBinding
import com.sonde.mentalfitness.databinding.FragmentFeelingBinding
import com.sonde.mentalfitness.domain.DailyRecordingWorker
import com.sonde.mentalfitness.domain.RecordingService
import com.sonde.mentalfitness.domain.model.SegmentScore
import com.sonde.mentalfitness.presentation.ui.MainActivity
import com.sonde.mentalfitness.presentation.ui.checkIn.feeling.FeelingViewModel
import com.sonde.mentalfitness.presentation.utils.ArithmeticUtils
import com.sonde.mentalfitness.presentation.utils.Common
import com.sonde.mentalfitness.presentation.utils.OnDialogOkButtonClickedListener
import com.sonde.mentalfitness.presentation.utils.showDialog
import com.sonde.mentalfitness.presentation.utils.util.getTimeInReadableFormat
import com.sonde.voip_vad.RecordingData
import com.sonde.voip_vad.RecordingDataType
import com.sondeservices.edge.ml.model.Score
import com.sondeservices.edge.ml.model.VFFinalScore
import com.sondeservices.edge.ml.model.VFScore
import com.twilio.voipcall.voiceprocessing.WavProcessingActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.TimeUnit


class ForegroundRecordingFragment :
    BaseFragment<ForegroundRecordingBinding, ForegroundRecordingViewModel>(
        layoutId = R.layout.foreground_recording
    ), OnDialogOkButtonClickedListener {
    private val TAG = ForegroundRecordingFragment::class.java.simpleName
    private lateinit var micImageView: ImageView
    private lateinit var textView_time_count: TextView
    private lateinit var timeCircularProgressIndicator: CircularProgressIndicator
    private lateinit var processingImage2: LottieAnimationView
    private lateinit var messageAboutProcess2: TextView
    private lateinit var textView_demo_type: TextView
    private lateinit var btn_checkScore: Button
    private lateinit var finalScore: Score
    val sharedPreferenceHelper =
        SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
    private lateinit var segmentScoreAdapter: SegmentScoreAdapter
    private lateinit var segmentScoreBlockAdapter: SegmentScoringBlockAdapter
    private var isScoreReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(
            TAG,
            "isRecordingServiceRunning==>${sharedPreferenceHelper.isRecordingServiceRunning()}"
        )

        observe(viewModel.segmentScore, ::onNewSegmentScore)
        // Get views
        micImageView = view.findViewById(R.id.imageView_mic)
        timeCircularProgressIndicator = view.findViewById(R.id.timer_CircularProgressIndicator)
        textView_time_count = view.findViewById(R.id.textView_time_count)
        processingImage2 = view.findViewById(R.id.processingImage2)
        messageAboutProcess2 = view.findViewById(R.id.messageAboutProcess2)
        btn_checkScore = view.findViewById(R.id.btn_checkScore)
        textView_demo_type = view.findViewById(R.id.textView_demo_type)
//        textViewSegmentScoring = view.findViewById(R.id.textViewSegmentScoring)
//        segmentScoreDetails = view.findViewById(R.id.segmentScoreDetails)

        if (!(sharedPreferenceHelper.isRecordingServiceRunning())) {
            sharedPreferenceHelper.setDemoType(Constants.APP_LEVEL_DEMO)
            startRecordingService()
        }
        textView_demo_type.text = sharedPreferenceHelper.getDemoType()
        btn_checkScore.setOnClickListener {
            requireActivity().startActivity(
                WavProcessingActivity.newInstance(
                    requireActivity(),
                    finalScore as VFFinalScore,
                    0
                )
            )
            requireActivity().finish()
        }
    }

    private fun onNewSegmentScore(segmentScore: SegmentScore) {
        segmentScoreAdapter.setSegmentScoreList(segmentScore)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    private fun startRecordingService() {
        val timeDiff = 0L
        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyRecordingWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS).build()
        WorkManager.getInstance(requireActivity()).enqueue(dailyWorkRequest)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onScoreIsReadyEvent(score: Score) {
        isScoreReady = true
        sharedPreferenceHelper.clearSegmentScoreList()
        hideProcessingUI()
        btn_checkScore.visibility = View.VISIBLE
        finalScore = score

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecordingDataEvent(event: RecordingData) {
        Log.d(TAG, "Recording Data noOfSecond==>${event.noOfSecond}")
        Log.d(TAG, "Recording Data isUserVerified==>${event.isUserVerified}")
        updateUI(event)
        if (event.noOfSecond >= 30) {
            segmentScoreBlockAdapter.deleteWhiteBlockAtStart()
            segmentScoreBlockAdapter.notifyDataSetChanged()
            showProcessingUI()
            Handler().postDelayed(this::showErrorDialog, 15000)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSegmentScoreEvent(segmentScore: SegmentScore) {
        if (viewBinding.cardSegmentScore.visibility == View.GONE) {
            viewBinding.cardSegmentScore.visibility = View.VISIBLE
        }
        viewBinding.textViewSegmentScoring.text =
            "Segment ${segmentScore.segmentNumber}, Score:${segmentScore.score.getValue()}"
        showDetailsScore(segmentScore.score as VFFinalScore)
        viewModel.addSegmentScore(segmentScore)
    }

    private fun showErrorDialog() {
        if (!isScoreReady) {
            showDialog(requireContext(), "Sorry, there was an error in calculating score. \n Please try again..", this)
        }
    }

    override fun onDialogOkButtonClicked() {
        //Redirect to menu Screen.
        navigateToMenuScreen()
    }

    private fun showDetailsScore(vfFinalScore: VFFinalScore) {
        viewBinding.segmentScoreDetails.text = ""
        for (item in vfFinalScore.getVFScores()) {
            viewBinding.segmentScoreDetails.text =
                "${viewBinding.segmentScoreDetails.text} \n ${item.getName()}: ${
                    getScoreWithUnit(
                        item,
                        item.getCode()
                    )
                }"
        }
    }


    private fun updateUI(event: RecordingData) {
        setProgress(event.noOfSecond)
        displayMicImageAccordingUserVerification(event.isUserVerified)
        setSecondsForUser(event)
        segmentScoreBlockAdapter.setSegmentRecordingList(event)
        viewBinding.recyclerViewSegmentScoringBlock.smoothScrollToPosition(segmentScoreBlockAdapter.itemCount - 1)
    }

    private fun setSecondsForUser(event: RecordingData) {
        showTotalElapsedTime(event)
        if (event.isUserVerified) {
            textView_time_count.text = "${event.noOfSecond}s"
            textView_time_count.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            if (event.recordingDataType == RecordingDataType.NO_VOICE) {
                textView_time_count.text = "No Voice"
                textView_time_count.setTextColor(Color.parseColor("#FFFFFF"))
            } else if (event.recordingDataType == RecordingDataType.INSUFFICIENT_VOICE) {
                textView_time_count.text = "Insufficient Voice"
                textView_time_count.setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                textView_time_count.text = "Unverified Voice"
                textView_time_count.setTextColor(Color.parseColor("#DC4D55"))
            }
        }
    }

    private fun showTotalElapsedTime(event: RecordingData) {
        if (viewBinding.textViewTotalTimeCount.visibility == View.GONE) {
            viewBinding.textViewTotalTimeCount.visibility = View.VISIBLE
        }
        val totalTime = (event.noOfSecond)
        var totalTimePercentage = 0
        if (totalTime != null) {

            totalTimePercentage =
                ArithmeticUtils.getPercentage(totalTime, getTotalTimeElapsed().toInt())
            viewBinding.textViewTotalTimeCount.text =
                "${totalTime}sec/${getTimeInReadableFormat(getTotalTimeElapsed().toInt())} (${totalTimePercentage}%)"
        }
    }

    private fun displayMicImageAccordingUserVerification(userVerified: Boolean) {
        if (userVerified) {
            micImageView.setImageResource(R.drawable.ic_mic_filled)
        } else {
            micImageView.setImageResource(R.drawable.ic_baseline_mic_off_24)
        }
    }

    private fun setProgress(noOfSecond: Int) {
        val progress = ((100 * noOfSecond) / 30)
        Log.d(TAG, "Progress==>$progress")
        timeCircularProgressIndicator.progress = progress
    }

    private fun showProcessingUI() {
        messageAboutProcess2.visibility = View.VISIBLE
        processingImage2.visibility = View.VISIBLE
    }

    private fun hideProcessingUI() {
        messageAboutProcess2.visibility = View.GONE
        processingImage2.visibility = View.GONE
    }

    private fun getScoreWithUnit(score: VFScore, code: String): String {
        var unit = ""
        if (code == "jitter") {
            unit = "${score.getValue()}%"
        } else if (code == "pitch_range") {
            unit = "${score.getRawValue()} octaves"
        } else if (code == "shimmer") {
            unit = "${score.getValue()}%"
        } else if (code == "energy_range") {
            unit = "${score.getValue()} dB"
        } else if (code == "vowel_space_area") {
            unit = "${score.getRawValue()} kHz^2"
        } else if (code == "phonation_duration") {
            unit = score.getValue().toString()
        }
        return unit
    }

    override fun initViewModel(): ForegroundRecordingViewModel {
        return ViewModelProvider(this).get(ForegroundRecordingViewModel::class.java)
    }

    override fun initDependencies() {
        segmentScoreAdapter = SegmentScoreAdapter(viewModel)
        segmentScoreBlockAdapter = SegmentScoringBlockAdapter()
//        Log.d(TAG,"getSegmentScoreList Size==>${sharedPreferenceHelper.getSegmentScoreList().size}")
//        if (sharedPreferenceHelper.getSegmentScoreList().isNotEmpty()) {
//            segmentScoreAdapter.mSegmentScoreList?.addAll(sharedPreferenceHelper.getSegmentScoreList())
//        } else {
//            segmentScoreAdapter.mSegmentScoreList = ArrayList()
//        }

    }

    override fun initDataBinding() {
        viewBinding.viewModel = viewModel
        viewBinding.recyclerViewSegmentScoring.adapter = segmentScoreAdapter
        val manager = LinearLayoutManager(activity)
        manager.reverseLayout = true
        viewBinding.recyclerViewSegmentScoring.layoutManager = manager


        viewBinding.recyclerViewSegmentScoringBlock.adapter = segmentScoreBlockAdapter
        val managerHorizontal = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        managerHorizontal.reverseLayout = true
        managerHorizontal.stackFromEnd = true
        viewBinding.recyclerViewSegmentScoringBlock.layoutManager = managerHorizontal
        segmentScoreBlockAdapter.addWhiteBlockAtStart()

    }

    fun getTotalTimeElapsed(): Long {
        val sharedPreferenceHelper =
            SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())
        val savedTime = sharedPreferenceHelper.getTotalTimeElapsed()
        val difference = Date().time - savedTime
        return difference / 1000
    }

    private fun navigateToMenuScreen() {
        sharedPreferenceHelper.setRecordingServiceRunning(false)
        activity?.finish()
//        val checkInHostActivityIntent = Intent(activity, MainActivity::class.java)
//        checkInHostActivityIntent.flags =
//            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(checkInHostActivityIntent)
    }

}