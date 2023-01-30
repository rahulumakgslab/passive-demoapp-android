package com.sonde.mentalfitness.presentation.utils.util

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.*
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

const val FOLDER_NAME_AUDIO_SAMPLE = "audio_sample"

fun getAudioSampleFolderPath(context: Context): String {
    val measureDir: File =
        File(getPath(context), FOLDER_NAME_AUDIO_SAMPLE)
    if (!measureDir.exists()) {
        measureDir.mkdir()
    }
    return measureDir.absolutePath
}

fun getPath(context: Context): String? {
    return context.filesDir.absolutePath
}

fun readAndWriteBinFile(context: Context, fileName: String) {
    Log.d("FileUtils==>", "File name==>$fileName")
    val outputFileName = "tiny_sound_model.bin"
    try {
        val inputFile: File = File(getPath(context), fileName)//tiny_sound_model.bin
        val outputFile: File = File(getPath(context), outputFileName)//
        Log.d("FileUtils==>", "File name as a file==>${inputFile.absoluteFile}")
        Log.d("FileUtils==>", "File name as a file==>${inputFile.absoluteFile.absolutePath}")
        if (!inputFile.exists()) {
            Log.d("FileUtils==>", "$fileName file not present")
        } else {

            var fileInputStream: FileInputStream? = null
            fileInputStream = context.openFileInput(fileName)
            var inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var text: String? = null
            while ({ text = bufferedReader.readLine(); text }() != null) {
                Log.d("FileUtils==>", "text=>$text")
                stringBuilder.append(text)
            }
            bufferedReader.close()
            inputStreamReader.close()
            var fileOutputStream =
                context.openFileOutput(outputFileName, Context.MODE_PRIVATE)
            fileOutputStream.write(stringBuilder.toString().toByteArray())
            fileOutputStream.close()
        }
    } catch (ex: java.lang.Exception) {
        Log.e("FileUtils", "Exception==>${ex.message!!}")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun copyBinFile(context: Context, fileName: String) {
    Log.d("FileUtils==>", "File name==>$fileName")
    val outputFileName = "tiny_sound_model.bin"
    try {
        val inputFile: File = File(getPath(context), fileName)//tiny_sound_model.bin
        val outputFile: File = File(getPath(context), outputFileName)//
        copyFile(inputFile,outputFile)
    } catch (ex: java.lang.Exception) {
        Log.e("FileUtils", "Exception==>${ex.message!!}")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun copyFile(src: File, dest: File) {
    Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
}
