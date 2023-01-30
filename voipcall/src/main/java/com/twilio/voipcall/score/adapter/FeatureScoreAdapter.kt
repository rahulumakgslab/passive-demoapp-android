package com.twilio.voipcall.score.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sonde.base.domain.VoiceFeaturesModel
import com.twilio.voipcall.R


class FeatureScoreAdapter(private var mFeatureLabels: List<VoiceFeaturesModel>) :
    RecyclerView.Adapter<FeatureScoreAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mfscore_component, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feature = mFeatureLabels.get(position)
        holder.featureName.text = feature.name
        holder.value.text = feature.score
        holder.value.setTextColor(feature.color)
    }

    override fun getItemCount(): Int = mFeatureLabels.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val featureName = itemView.findViewById<TextView>(R.id.textView_feature_name)
        val value = itemView.findViewById<TextView>(R.id.textView_feature_score_value)
    }
}