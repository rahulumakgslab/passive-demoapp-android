package com.sonde.mentalfitness.presentation.ui.record.foregroundrecording

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.data.local.sharedpref.SharedPreferenceServiceImpl
import com.sonde.mentalfitness.databinding.ItemSegmentScoringBinding
import com.sonde.mentalfitness.domain.model.SegmentScore
import com.sonde.mentalfitness.presentation.utils.ArithmeticUtils
import com.sondeservices.edge.inference.TAG
import com.sondeservices.edge.ml.model.VFFinalScore
import com.sondeservices.edge.ml.model.VFScore

class SegmentScoreAdapter(private val foregroundRecordingViewModel: ForegroundRecordingViewModel) :
    RecyclerView.Adapter<SegmentScoreAdapter.ViewHolder>() {
    val sharedPreferenceHelper =
        SharedPreferenceServiceImpl(MentalFitnessApplication.applicationContext())

    //     var mSegmentScoreList: ArrayList<SegmentScore>? = sharedPreferenceHelper.getSegmentScoreList()
    var mSegmentScoreList: ArrayList<SegmentScore>? = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemSegmentScoringBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_segment_scoring,
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val segmentScore = mSegmentScoreList?.get(position)
        holder.binding.textViewSegmentScoring.text =
            "Segment ${segmentScore?.segmentNumber}, Score:${segmentScore?.score?.getValue()}"
        val totalTime = (segmentScore?.segmentNumber)?.times(3)
//        val totalTimePercentage = (totalTime?.div(segmentScore.totalTimeElapsed))?.times(100)
        var totalTimePercentage = 0
        if (totalTime != null) {
//            Log.d(
//                "SegmentScoreAdapter",
//                "Percentage==>${(totalTime / (segmentScore.totalTimeElapsed.toInt())) * 100}"
//            )
//            totalTimePercentage = ((totalTime / (segmentScore.totalTimeElapsed)) * 100).toInt()
            totalTimePercentage = ArithmeticUtils.getPercentage(totalTime, segmentScore.totalTimeElapsed.toInt()
            )
        }

        holder.binding.textTotalTimeElapsed.text =
            "\n Total : ${totalTime}/${segmentScore?.totalTimeElapsed}sec (${totalTimePercentage}%)"
        setSegmentScoreDetails(holder.binding, segmentScore)
        holder.binding.executePendingBindings()

    }

    fun setSegmentScoreDetails(binding: ItemSegmentScoringBinding, segmentScore: SegmentScore?) {
        binding.segmentScoreDetails.text = ""
        val vfFinalScore = segmentScore?.score as VFFinalScore
        for (item in vfFinalScore.getVFScores()) {
            binding.segmentScoreDetails.text =
                "${binding.segmentScoreDetails.text} \n ${item.getName()}: ${
                    getScoreWithUnit(
                        item,
                        item.getCode()
                    )
                }"
        }
    }

    fun setSegmentScoreList(optionListData: SegmentScore) {
        this.mSegmentScoreList?.add(optionListData)
        this.mSegmentScoreList?.let { sharedPreferenceHelper.setSegmentScoreList(it) }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mSegmentScoreList?.size ?: 0

    override fun getItemId(position: Int): Long {
        return position.toLong()
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

    inner class ViewHolder(val binding: ItemSegmentScoringBinding) :
        RecyclerView.ViewHolder(binding.root)


}