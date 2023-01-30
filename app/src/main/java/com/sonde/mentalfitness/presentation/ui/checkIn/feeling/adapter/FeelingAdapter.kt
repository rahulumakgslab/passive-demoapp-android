package com.sonde.mentalfitness.presentation.ui.checkIn.feeling.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.LayoutCheckinOptionsItemBinding
import com.sonde.mentalfitness.domain.model.checkin.OptionModel
import com.sonde.mentalfitness.presentation.ui.checkIn.feeling.FeelingViewModel

class FeelingAdapter(private val feelingViewModel: FeelingViewModel) :
    RecyclerView.Adapter<FeelingAdapter.ViewHolder>() {

    private var mOptionsList: List<OptionModel>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding: LayoutCheckinOptionsItemBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_checkin_options_item,
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.options = mOptionsList!![position]
        holder.binding.viewModel = feelingViewModel
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int = mOptionsList?.size ?: 0

    fun setOptionsList(optionListData: List<OptionModel>) {
        this.mOptionsList = optionListData
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: LayoutCheckinOptionsItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
