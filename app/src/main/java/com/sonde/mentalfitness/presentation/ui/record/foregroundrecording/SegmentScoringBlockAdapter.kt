package com.sonde.mentalfitness.presentation.ui.record.foregroundrecording

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.databinding.ItemSegmentBlockBinding
import com.sonde.voip_vad.RecordingData
import com.sonde.voip_vad.RecordingDataType

class SegmentScoringBlockAdapter() : RecyclerView.Adapter<SegmentScoringBlockAdapter.ViewHolder>() {

    var mSegmentRecordingDataList: ArrayList<RecordingData>? = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemSegmentBlockBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_segment_block,
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recordingData = mSegmentRecordingDataList?.get(position)
        if (recordingData?.isUserVerified == true) {
            holder.binding.cardBlock.setBackgroundColor(holder.itemView.context.getColor(R.color.colorAccent))
        } else {
            when (recordingData?.recordingDataType) {
                RecordingDataType.NO_VOICE -> {
                    holder.binding.cardBlock.setBackgroundColor(Color.GRAY)
                }
                RecordingDataType.INSUFFICIENT_VOICE -> {
                    holder.binding.cardBlock.setBackgroundColor(Color.parseColor("#a67c00"))
                }
                RecordingDataType.RECORDING -> {
                    holder.binding.cardBlock.setBackgroundColor(Color.WHITE)
                    startBlinking(holder)
                }
                else -> {
                    holder.binding.cardBlock.setBackgroundColor(Color.RED)
                }
            }
        }
    }

    override fun getItemCount(): Int = mSegmentRecordingDataList?.size ?: 0

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setSegmentRecordingList(optionListData: RecordingData) {
//        if (this.mSegmentRecordingDataList?.size == 0) {
//            addWhiteBlockAtStart()
//        } else {
//            this.mSegmentRecordingDataList?.removeAt(this.mSegmentRecordingDataList?.size!!.minus(1))
//        }
        deleteWhiteBlockAtStart()
        this.mSegmentRecordingDataList?.add(optionListData)
        notifyItemInserted(itemCount - 1)
        addWhiteBlockAtStart()
//        notifyDataSetChanged()
    }

    fun addWhiteBlockAtStart() {
        val recordingData = RecordingData()
        recordingData.noOfSecond = 0
        recordingData.isUserVerified = false
        recordingData.recordingDataType = RecordingDataType.RECORDING
        this.mSegmentRecordingDataList?.add(recordingData)
//        notifyItemInserted(this.itemCount - 1)
    }

    fun deleteWhiteBlockAtStart() {
        this.mSegmentRecordingDataList?.removeAt(this.mSegmentRecordingDataList?.size!!.minus(1))
//        this.mSegmentRecordingDataList?.removeAt(this.itemCount - 1)
//        notifyDataSetChanged()
//        notifyItemRemoved(this.itemCount - 1)
    }

    fun startBlinking(viewHolder: ViewHolder) {
        val animationBlink=AnimationUtils.loadAnimation(viewHolder.itemView.context,R.anim.blink)
        viewHolder.binding.cardBlock.startAnimation(animationBlink)
    }

    inner class ViewHolder(val binding: ItemSegmentBlockBinding) :
        RecyclerView.ViewHolder(binding.root)
}