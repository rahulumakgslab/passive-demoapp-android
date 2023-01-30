package com.sonde.mentalfitness.domain.model

import com.sondeservices.edge.ml.model.Score

data class SegmentScore(
    var score: Score,
    var segmentNumber: Int,
    var averageScore:Int,
    var totalTimeElapsed:Long
)
