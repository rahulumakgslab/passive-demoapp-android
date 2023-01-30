package com.sonde.mentalfitness.presentation.ui.score.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.LayoutFactorItemBinding
import com.sonde.mentalfitness.domain.model.checkin.score.ScoreFactorAdapterModel

class ScoreFactorAdapter : RecyclerView.Adapter<ScoreFactorAdapter.ViewHolder>() {

    private var mScoreFactorList: List<ScoreFactorAdapterModel>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding: LayoutFactorItemBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_factor_item,
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.scoreFactorAdapterModel = mScoreFactorList!![position]
    }

    override fun getItemCount(): Int = mScoreFactorList?.size ?: 0

    fun setScoreFactorsList(scoreFactorList: List<ScoreFactorAdapterModel>) {
        this.mScoreFactorList = scoreFactorList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: LayoutFactorItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}