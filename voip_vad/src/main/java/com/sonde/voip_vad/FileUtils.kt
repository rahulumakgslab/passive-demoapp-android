package com.sonde.voip_vad

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object FileUtils {
    fun getPath(context: Context): String {
        return context.filesDir.absolutePath
    }

    @Throws(IOException::class)
    fun copyAssetsFolderToStorage(context: Context, assetDir: String, destDirPath: String) {
        val destDir = File(destDirPath)
        createDir(destDir)
        val assetManager = context.assets
        val files = assetManager.list(assetDir)
        for (file in files!!) {
            val absAssetFilePath = addTrailingSlash(assetDir) + file
            val subFiles = assetManager.list(absAssetFilePath)
            if (subFiles!!.isEmpty()) {
                // It is a file
                val destFilePath = addTrailingSlash(destDirPath) + file
                copyAssetFile(context, absAssetFilePath, destFilePath)
            } else {
                // It is a sub directory
                copyAssetsFolderToStorage(
                    context,
                    absAssetFilePath,
                    addTrailingSlash(destDirPath) + file
                )
            }
        }
    }

    @Throws(IOException::class)
    private fun copyAssetFile(
        context: Context,
        assetFilePath: String,
        destinationFilePath: String
    ) {
        if (!File(destinationFilePath).exists()) {
            val `in` = context.assets.open(assetFilePath)
            val out: OutputStream = FileOutputStream(destinationFilePath)
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) out.write(buf, 0, len)
            `in`.close()
            out.close()
        }
    }

    fun addTrailingSlash(path: String): String {
        var path = path
        if (path[path.length - 1] != '/') {
            path += "/"
        }
        return path
    }

    @Throws(IOException::class)
    private fun createDir(dir: File) {
        if (dir.exists()) {
            if (!dir.isDirectory) {
                throw IOException("Can't create directory, a file is in the way")
            }
        } else {
            dir.mkdirs()
            if (!dir.isDirectory) {
                throw IOException("Unable to create directory")
            }
        }
    }

}