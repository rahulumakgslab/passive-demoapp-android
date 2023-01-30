package com.sonde.mentalfitness.presentation.ui.checkIn.feelingreason.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.LayoutFeelingReasonsItemBinding
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.presentation.ui.checkIn.feelingreason.FeelingReasonsViewModel


class FeelingReasonAdapter(private val feelingReasonsViewModel: FeelingReasonsViewModel) :
    RecyclerView.Adapter<FeelingReasonAdapter.ViewHolder>() {

    private var mOptionsList: List<OptionModel>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding: LayoutFeelingReasonsItemBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_feeling_reasons_item,
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.options = mOptionsList!![position]
        holder.binding.viewModel = feelingReasonsViewModel

        holder.binding.rootLayout.setOnClickListener {
            mOptionsList?.let {
                mOptionsList!![position].isSelected = !mOptionsList!![position].isSelected
                feelingReasonsViewModel.updateReasonOptionList(mOptionsList!!)
            }
        }

        val selected = mOptionsList!![position].isSelected
        if (selected) {
            holder.binding.optionValueTextView.background = ContextCompat.getDrawable(
                holder.binding.optionValueTextView.context,
                R.drawable.bg_capsule_outline_accent
            )
        } else {
            holder.binding.optionValueTextView.background = ContextCompat.getDrawable(
                holder.binding.optionValueTextView.context,
                R.drawable.bg_capsule_outline_gray
            )
        }

        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int = mOptionsList?.size ?: 0

    fun setOptionsList(optionListData: List<OptionModel>) {
        this.mOptionsList = optionListData
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: LayoutFeelingReasonsItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}