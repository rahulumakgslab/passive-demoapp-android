package com.sonde.mentalfitness.presentation.ui.record.recording

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.*
import com.sonde.mentalfitness.MentalFitnessApplication
import com.sonde.mentalfitness.domain.model.checkin.PassageModel
import com.sonde.mentalfitness.presentation.utils.util.ONE_SECOND_IN_MILLIS
import com.sondeservices.edge.recorder.SondeAudioRecorder
import com.sondeservices.edge.recorder.listeners.TimerRecordingListener

class RecordingViewModel : ViewModel(), LifecycleObserver {

    private var timer: CountDownTimer? = null

    private val sondeAudioRecorder: SondeAudioRecorder by lazy {
        SondeAudioRecorder(MentalFitnessApplication.applicationContext())
    }
    private val _event = MutableLiveData<RecordingViewEvent>()
    val event: LiveData<RecordingViewEvent> get() = _event

    private val _passageModel = MutableLiveData<PassageModel>()
    val passageModel: LiveData<PassageModel> get() = _passageModel

    private val _timerCount = MutableLiveData<Int>()
    val timerCount: LiveData<Int> get() = _timerCount

    fun setPassage(passageModel: PassageModel) {
        _passageModel.value = passageModel
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopCalled() {
        Log.d("!!", "onStop: =============------ViewModel-----------========================")
        if (sondeAudioRecorder.isRecording()) {
            sondeAudioRecorder.interruptAndDiscardRecording()
        }

        timer?.cancel()
        _event.postValue(RecordingViewEvent.OnRecordingInterrupted)

    }

    fun initRecording() {
        _timerCount.postValue(passageModel.value!!.timerLength)
        sondeAudioRecorder.beginRecording(
            getTimerLengthInMillis(passageModel.value?.timerLength),
            object : TimerRecordingListener {
                override fun onError(throwable: Throwable) {
                    Log.e("!!", "onError: $throwable")
                    _event.postValue(RecordingViewEvent.OnRecordingInterrupted)
                }

                override fun onRecorderInitialized() {
                    Log.e("!!", "onRecorderInitialized")
                }

                override fun onRecordingFinish(filePath: String) {
                    _event.postValue(
                        RecordingViewEvent.OnRecordingFinished(
                            filePath,
                            passageModel.value!!
                        )
                    )
                }

                override fun onRecordingStarted() {
                    Log.e("!!", "onRecordingStarted")
                }

                override fun onTick(millisUntilFinished: Long) {
                    _timerCount.value = _timerCount.value?.minus(1)
                }

            })

    }
//
//    private fun finishRecording() {
//        sondeAudioRecorder.finishRecording(object : OnRecordingFinishListener {
//            override fun onSuccess(filepath: String) {
//                Log.d("!!", "onSuccess: $filepath")
//                _event.postValue(
//                    RecordingViewEvent.OnRecordingFinished(
//                        filepath,
//                        passageModel.value!!
//                    )
//                )
//            }
//
//            override fun onError(throwable: Throwable) {
//                Log.e("!!", "onError: $throwable")
//                _event.postValue(RecordingViewEvent.OnRecordingInterrupted)
//            }
//
//        })
//    }
//
//    private fun startRecording(
//        timer: CountDownTimer
//    ) {
//        sondeAudioRecorder.beginRecording(object :
//            OnBeginRecordingListener {
//            override fun onRecorderInitialized() {
//                Log.d("!!", "onRecorderInitialized: ")
//            }
//
//            override fun onRecordingStarted() {
//                timer.start()
//            }
//
//            override fun onError(throwable: Throwable) {
//                Log.e("!!", "onError: " + throwable)
//            }
//
//        })
//    }

    private fun getTimerLengthInMillis(timerLengthSec: Int?): Long {
        timerLengthSec?.let {
            return (timerLengthSec * ONE_SECOND_IN_MILLIS).toLong()
        }
        return 0
    }

    fun onCancelClicked() {
        _event.postValue(RecordingViewEvent.OnCancelClicked)
    }
}