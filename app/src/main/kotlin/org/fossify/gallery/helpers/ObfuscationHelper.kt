package org.fossify.gallery.helpers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.fossify.gallery.extensions.config
import java.io.File
import kotlin.random.Random

class ObfuscationHelper(val context: Context) {

    fun isObfuscatedMode() = context.config.isObfuscatedMode

    fun getObfuscationMap(): Map<String, String> {
        val json = context.config.obfuscationMap
        if (json.isEmpty()) return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            Gson().fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveObfuscationMap(map: Map<String, String>) {
        val json = Gson().toJson(map)
        context.config.obfuscationMap = json
    }

    fun generateRandomMap(): Map<String, String> {
        val supportedExtensions = listOf(
            "jpg", "jpeg", "png", "webp", "gif", "heic", "bmp",
            "mp4", "mkv", "webm", "avi", "mov", "3gp",
            "mp3", "wav", "flac", "ogg", "m4a", "aac"
        )
        val charPool = "abcdefghijklmnopqrstuvwxyz0123456789"
        val map = mutableMapOf<String, String>()
        val usedValues = mutableSetOf<String>()

        for (ext in supportedExtensions) {
            var randomString = ""
            var attempts = 0
            do {
                val length = Random.nextInt(5, 9) // 5 to 8
                randomString = (1..length)
                    .map { charPool.random() }
                    .joinToString("")
                attempts++
            } while (usedValues.contains(randomString) && attempts < 100)
            
            map[ext] = randomString
            usedValues.add(randomString)
        }
        return map
    }

    fun getObfuscatedExtension(realExtension: String): String? {
        val map = getObfuscationMap()
        return map[realExtension.lowercase()]
    }

    fun getRealExtension(obfuscatedExtension: String): String? {
        val map = getObfuscationMap()
        return map.entries.find { it.value == obfuscatedExtension }?.key
    }

    fun isObfuscatedFile(path: String): Boolean {
        val extension = path.substringAfterLast('.', "")
        if (extension.isEmpty()) return false
        val realExt = getRealExtension(extension)
        return realExt != null
    }

    fun getObfuscatedFileType(path: String): Int {
        val extension = path.substringAfterLast('.', "")
        val realExt = getRealExtension(extension) ?: return -1
        
        val videoExtensions = listOf("mp4", "mkv", "webm", "avi", "mov", "3gp", "m4v")
        
        return when {
             videoExtensions.contains(realExt) -> TYPE_VIDEOS
             realExt == "gif" -> TYPE_GIFS
             realExt == "svg" -> TYPE_SVGS
             else -> TYPE_IMAGES // Default to image
        }
    }
}
