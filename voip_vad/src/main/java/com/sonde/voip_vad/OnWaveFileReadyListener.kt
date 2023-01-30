package com.sonde.voip_vad

interface OnWaveFileReadyListener {
    fun onWavFileReady(waveFilePath : String)
    fun onWavFileReadyForSegmentScore(waveFilePath : String, segmentNumber:Int)
    fun onWavFileReadyForSegmentScore(waveFilePath: String)
}