package com.sonde.voip_vad

import android.content.Context
import android.util.Log
import com.sonde.voip_vad.FileUtils.addTrailingSlash
import com.sonde.voip_vad.FileUtils.copyAssetsFolderToStorage
import com.sonde.voip_vad.FileUtils.getPath
import java.io.File

const val VOIP_VAD_DATA_FOLDER_NAME = "voip_vad"

class VadLib(val context: Context) {

    /**
     * A native method that is implemented by the 'voip_vad' native library,
     * which is packaged with this application.
     */
    external fun performVAD(
        inputAudioFilePath: String,
        configFilePath: String,
        outPutDirPath: String
    ): Int


    // Used to load the 'voip_vad' library on application startup.
    init {
        copyAssetsFolderToStorage(
            context,
            VOIP_VAD_DATA_FOLDER_NAME,
            addTrailingSlash(getPath(context)) + VOIP_VAD_DATA_FOLDER_NAME
        )
        System.loadLibrary("voip_vad")
    }

    fun performVAD(
        inputAudioFilePath: String,
    ): String? {
        val outPut = performVAD(
            inputAudioFilePath,
            getPath(context) + "/$VOIP_VAD_DATA_FOLDER_NAME/manifest.yaml",
            getPath(context) + "/$VOIP_VAD_DATA_FOLDER_NAME/"
        )
        Log.d("!!", "outPut value==="+outPut);
        if (outPut == 1) {
            val fileName = File(inputAudioFilePath).name.split(".")[0] + "_vad.csv"
            return getPath(context) + "/$VOIP_VAD_DATA_FOLDER_NAME/$fileName"
        } else {
            return null
        }
    }


}