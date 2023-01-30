package com.sonde.mentalfitness.presentation.ui.checkIn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sonde.base.presentation.utils.extensions.observe
import com.sonde.mentalfitness.R
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.domain.model.checkin.queanswers.QuestionnaireAnswersModel
import com.sonde.mentalfitness.presentation.ui.checkIn.feeling.FeelingFragment
import com.sonde.mentalfitness.presentation.ui.record.readytorecord.ReadyToRecordFragment
import com.sonde.mentalfitness.presentation.ui.score.MFScoreFragment


class CheckInHostActivity : AppCompatActivity() {
    private lateinit var viewModel: CheckInHostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_host)
        viewModel = ViewModelProvider(this).get(CheckInHostViewModel::class.java)
        observe(viewModel.event, ::onViewEvent)
    }

    private fun onViewEvent(viewEvent: CheckInHostViewEvent) {
        when (viewEvent) {
            is CheckInHostViewEvent.FeelingScreenEvent -> {
                navigateToFeeling()
            }
            is CheckInHostViewEvent.ReadyToRecordEvent -> {
                navigateToReadyToRecord(viewEvent.questionnaireAnswer, viewEvent.passageModel)
            }
        }
    }


    private fun navigateToFeeling() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, FeelingFragment.newInstance()).commit()
    }

    private fun navigateToReadyToRecord(
        questionnaireAnswer: QuestionnaireAnswersModel,
        passageModel: PassageModel
    ) {
        supportFragmentManager.beginTransaction().replace(
            R.id.container, ReadyToRecordFragment.newInstance(questionnaireAnswer, passageModel)
        ).commit()

//        supportFragmentManager.beginTransaction().replace(
//            R.id.container, MFScoreFragment.newInstance()
//        ).commit()

    }
}