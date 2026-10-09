package com.trazavoz.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {

    suspend fun downloadAndCacheImage(pictogramId: Int): String? = withContext(Dispatchers.IO) {
        val url = "https://api.arasaac.org/api/pictograms/$pictogramId"
        val request = Request.Builder().url(url).build()
        
        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            
            val body = response.body ?: return@withContext null
            
            val directory = File(context.filesDir, "pictogramas")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val file = File(directory, "pic_$pictogramId.png")
            
            body.byteStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            file.absolutePath
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun deleteCachedImage(localPath: String) {
        try {
            val file = File(localPath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
