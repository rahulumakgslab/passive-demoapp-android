package com.twilio.voipcall.score

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.sonde.base.domain.VoiceFeaturesModel
import com.sonde.base.presentation.utils.util.getAggregatedScoreColorByRange
import com.sonde.base.presentation.utils.util.getColorByRange
import com.sondeservices.edge.ml.model.VFFinalScore
import com.sondeservices.edge.ml.model.VFScore
import com.tenclouds.gaugeseekbar.GaugeSeekBar
import com.tenclouds.gaugeseekbar.ThumbDrawable
import com.twilio.voipcall.R
import com.twilio.voipcall.score.adapter.FeatureScoreAdapter
import com.twilio.voipcall.utils.MMMM_D_YYYY_DATE_PATTERN
import com.twilio.voipcall.utils.getCurrentDateStr

//private const val ARG_SESSION_DATA = "session_data"
private const val ARG_AGGREGATE_SCORE = "aggregate_score"

class VoipMFScoreFragment : Fragment() {
    companion object {
        fun newInstance(vfFinalScore: VFFinalScore) =
            VoipMFScoreFragment().apply {
                arguments = Bundle().apply {
//                    putParcelable(ARG_SESSION_DATA, sessionData)
                    putParcelable(ARG_AGGREGATE_SCORE, vfFinalScore)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_voip_mf_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val date = getCurrentDateStr(MMMM_D_YYYY_DATE_PATTERN)
        view.findViewById<TextView>(R.id.date_textView).setText(date)
        view.findViewById<TextView>(R.id.textView24).setOnClickListener {
            navigateToCheckIn()
        }
        val vfFinalScore = arguments?.getParcelable<VFFinalScore>(ARG_AGGREGATE_SCORE)
        val list = ArrayList<VoiceFeaturesModel>()
        if (vfFinalScore != null) {
            val color = getAggregatedScoreColorByRange(vfFinalScore.getValue())
            view.findViewById<TextView>(R.id.feature_score_textView)
                .setText("${vfFinalScore.getValue()}")
            view.findViewById<TextView>(R.id.feature_score_textView)
                .setTextColor(color)
            val tumbDrawable = ThumbDrawable(color)
            val gaugeSeekBar = view.findViewById<GaugeSeekBar>(R.id.custom_progress)
            gaugeSeekBar.setProgress(vfFinalScore.getRawValue() / 100)
            gaugeSeekBar.setCustomThumbDrawable(tumbDrawable)

            view.findViewById<TextView>(R.id.feature_score_scale)
                .setText(getScoreLabel(vfFinalScore.getRawValue()))

            for (item in vfFinalScore.getVFScores()) {
                val colorByRange = getColorValue(item.getRawValue(), item.getCode())
                list.add(
                    VoiceFeaturesModel(
                        item.getName(),
                        colorByRange,
                        getScoreWithUnit(item, item.getCode())
                    )
                )
            }
        }
        view.findViewById<RecyclerView>(R.id.feature_score_RecyclerView).adapter =
            FeatureScoreAdapter(list)
    }

    private fun getScoreLabel(score: Float): String {
        if (score <= 64) {
            return "Pay attention"
        } else if (score in 65.0..79.0) {
            return "Good"
        } else if (score >= 80) {
            return "Excellent"
        }
        return ""
    }

    private fun getColorValue(score: Float, code: String): Int {
        var minRange = 0f
        var maxRange = 0f
        if (code == "jitter") {
            minRange = 40f
            maxRange = 100f
        } else if (code == "pitch_range") {
            minRange = 0f
            maxRange = 0.45f
        } else if (code == "shimmer") {
            minRange = 55f
            maxRange = 100f
        } else if (code == "energy_range") {
            minRange = 0f
            maxRange = 36f
        } else if (code == "vowel_space_area") {
            minRange = 0f
            maxRange = 0.36f
        } else if (code == "phonation_duration") {
            minRange = 0f
            maxRange = 600f
        }
        val colorByRange = getColorByRange(score, minRange, maxRange)
        return colorByRange
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

    private fun navigateToCheckIn() {
        activity?.finish()
//        val checkInHostActivityIntent = Intent(activity, MainActivity::class.java)
//        checkInHostActivityIntent.flags =
//            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(checkInHostActivityIntent)
    }
}