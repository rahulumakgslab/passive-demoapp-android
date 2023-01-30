package com.sonde.mentalfitness.presentation.ui.record.foregroundrecording

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.domain.model.SegmentScore

class ForegroundRecordingViewModel : ViewModel() {
    private val _segmentScore = MutableLiveData<SegmentScore>()
    val segmentScore: LiveData<SegmentScore> get() = _segmentScore

    var segmentScoreList: List<SegmentScore>

    val sharedPreferenceHelper =
        SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())

    fun addSegmentScore(segmentScore: SegmentScore) {
        _segmentScore.value = segmentScore!!
    }

    init {
        segmentScoreList = sharedPreferenceHelper.getSegmentScoreList()
        Log.d("ForegroundRecordingViewModel","segmentScoreList Size==>${segmentScoreList.size}")
    }
}