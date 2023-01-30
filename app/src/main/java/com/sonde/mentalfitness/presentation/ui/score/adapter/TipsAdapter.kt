package com.sonde.mentalfitness.presentation.ui.score.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.domain.model.checkin.score.TipModel

class TipsAdapter() : PagerAdapter() {
    var tipsList: List<TipModel>? = null

    lateinit var layoutInflater: LayoutInflater

    override fun getCount(): Int {
        return tipsList?.size ?: 0
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = LayoutInflater.from(container.context)
        val view: View = layoutInflater.inflate(R.layout.layout_tip_item, container, false)

        val imageViewTipImage = view.findViewById<ImageView>(R.id.imageView_tip_image)
        val textViewTitle = view.findViewById<TextView>(R.id.textView_title)
        val textViewDesc = view.findViewById<TextView>(R.id.textView_desc)

        val tipModel = tipsList!![position]
        imageViewTipImage.setImageDrawable(ContextCompat.getDrawable(container.context, tipModel.imageResId))
        textViewTitle.text = tipModel.Title
        textViewDesc.text = tipModel.desc

        container.addView(view)

        return view

    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        super.destroyItem(container, position, `object`)
        container.removeView(`object` as View?)
    }


    fun updateTipsList(tipsList: List<TipModel>) {
        this.tipsList = tipsList
        notifyDataSetChanged()
    }
}