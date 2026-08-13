package com.minimax.mobile.data

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class MiniMaxApi(context: Context) {
    private val appContext = context.applicationContext
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun chat(
        apiKey: String,
        region: MiniMaxRegion,
        model: String,
        messages: List<ChatMessageUi>,
    ): String {
        val jsonMessages = JSONArray()
        messages.forEach { message ->
            jsonMessages.put(JSONObject().put("role", message.role).put("content", message.content))
        }
        val body = JSONObject()
            .put("model", model)
            .put("messages", jsonMessages)
            .put("max_completion_tokens", 4096)
            .put("temperature", 0.7)
        val response = post(apiKey, region, "/v1/chat/completions", body)
        val content = response.optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content", "")
            .orEmpty()
        return content.replace(Regex("<think>[\\s\\S]*?</think>"), "").trim()
    }

    suspend fun generateImage(
        apiKey: String,
        region: MiniMaxRegion,
        prompt: String,
        aspectRatio: String,
        count: Int,
    ): List<String> {
        val body = JSONObject()
            .put("model", "image-01")
            .put("prompt", prompt)
            .put("aspect_ratio", aspectRatio)
            .put("response_format", "url")
            .put("n", count.coerceIn(1, 4))
            .put("prompt_optimizer", true)
        val response = post(apiKey, region, "/v1/image_generation", body)
        val urls = response.optJSONObject("data")?.optJSONArray("image_urls") ?: JSONArray()
        return buildList {
            for (index in 0 until urls.length()) add(urls.optString(index))
        }.filter { it.isNotBlank() }
    }

    suspend fun generateVideo(
        apiKey: String,
        region: MiniMaxRegion,
        model: String,
        prompt: String,
        onProgress: (String) -> Unit = {},
    ): GeneratedMedia = withContext(Dispatchers.IO) {
        val isH3 = model == "MiniMax-H3"
        val requestBody = if (isH3) {
            val content = JSONArray().put(JSONObject().put("type", "text").put("text", prompt))
            JSONObject()
                .put("model", "MiniMax-H3")
                .put("content", content)
                .put("resolution", "2K")
                .put("duration", 5)
                .put("ratio", "16:9")
        } else {
            JSONObject()
                .put("model", model)
                .put("prompt", prompt)
                .put("duration", 6)
                .put("resolution", "1080P")
        }
        val createPath = if (isH3) "/v2/video_generation" else "/v1/video_generation"
        val created = post(apiKey, region, createPath, requestBody)
        val taskId = created.optString("task_id")
        if (taskId.isBlank()) throw MiniMaxApiException("视频任务创建失败：没有返回 task_id")

        repeat(36) { attempt ->
            delay(4000)
            onProgress("视频生成中 · ${attempt + 1}/36")
            val statusJson = if (isH3) {
                get(apiKey, region, "/v2/query/video_generation/${encode(taskId)}")
            } else {
                get(apiKey, region, "/v1/query/video_generation?task_id=${encode(taskId)}")
            }
            if (isH3) {
                val task = statusJson.optJSONObject("task") ?: JSONObject()
                when (task.optString("status")) {
                    "succeeded" -> {
                        val url = task.optJSONObject("content")?.optString("url").orEmpty()
                        if (url.isBlank()) throw MiniMaxApiException("视频已完成，但没有返回下载地址")
                        return@withContext GeneratedMedia("video", "MiniMax-H3 视频", remoteUrl = url)
                    }
                    "failed", "cancelled", "expired" -> {
                        throw MiniMaxApiException("视频任务失败：${task.optJSONObject("error")?.optString("message") ?: "未知错误"}")
                    }
                }
            } else {
                when (statusJson.optString("status")) {
                    "Success" -> {
                        val fileId = statusJson.optString("file_id")
                        if (fileId.isBlank()) throw MiniMaxApiException("视频完成，但没有返回 file_id")
                        val fileInfo = get(apiKey, region, "/v1/files/retrieve?file_id=${encode(fileId)}")
                        val url = fileInfo.optJSONObject("file")?.optString("download_url").orEmpty()
                        if (url.isBlank()) throw MiniMaxApiException("视频完成，但没有返回下载地址")
                        return@withContext GeneratedMedia("video", "MiniMax 视频", remoteUrl = url)
                    }
                    "Failed", "Fail" -> throw MiniMaxApiException("视频任务失败")
                }
            }
        }
        throw MiniMaxApiException("视频生成超时，请稍后在 MiniMax 控制台查看任务")
    }

    suspend fun synthesizeSpeech(
        apiKey: String,
        region: MiniMaxRegion,
        text: String,
        voice: String,
    ): GeneratedMedia = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("model", "speech-2.8-hd")
            .put("text", text)
            .put("stream", false)
            .put("output_format", "url")
            .put("voice_setting", JSONObject().put("voice_id", voice).put("speed", 1.0).put("vol", 1.0).put("pitch", 0))
            .put("audio_setting", JSONObject().put("sample_rate", 32000).put("bitrate", 128000).put("format", "mp3").put("channel", 1))
        val response = post(apiKey, region, "/v1/t2a_v2", body)
        val data = response.optJSONObject("data") ?: JSONObject()
        val remote = data.optString("audio_url").takeIf { it.isNotBlank() }
            ?: data.optString("audio").takeIf { it.startsWith("http") }
        val local = if (remote == null) saveHexAudio(data.optString("audio"), "speech") else null
        if (remote == null && local == null) throw MiniMaxApiException("语音生成成功，但没有返回音频地址")
        GeneratedMedia("audio", "语音合成", remoteUrl = remote, localPath = local)
    }

    suspend fun generateMusic(
        apiKey: String,
        region: MiniMaxRegion,
        prompt: String,
        lyrics: String,
    ): GeneratedMedia = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("model", "music-3.0")
            .put("prompt", prompt)
            .put("lyrics", lyrics)
            .put("output_format", "url")
            .put("audio_setting", JSONObject().put("sample_rate", 44100).put("bitrate", 256000).put("format", "mp3"))
        val response = post(apiKey, region, "/v1/music_generation", body)
        val data = response.optJSONObject("data") ?: JSONObject()
        val remote = data.optString("audio_url").takeIf { it.isNotBlank() }
            ?: data.optString("audio").takeIf { it.startsWith("http") }
        val local = if (remote == null) saveHexAudio(data.optString("audio"), "music") else null
        if (remote == null && local == null) throw MiniMaxApiException("音乐生成成功，但没有返回音频地址")
        GeneratedMedia("audio", "音乐生成", remoteUrl = remote, localPath = local)
    }

    suspend fun describeImage(
        apiKey: String,
        region: MiniMaxRegion,
        imageInput: String,
        prompt: String,
    ): String {
        val body = JSONObject().put("prompt", prompt)
        if (imageInput.startsWith("http", ignoreCase = true) || imageInput.startsWith("data:")) {
            body.put("image_url", imageInput)
        } else {
            body.put("file_id", imageInput)
        }
        return post(apiKey, region, "/v1/coding_plan/vlm", body).optString("content", "")
    }

    suspend fun search(
        apiKey: String,
        region: MiniMaxRegion,
        query: String,
    ): List<SearchItem> {
        val response = post(apiKey, region, "/v1/coding_plan/search", JSONObject().put("q", query))
        val organic = response.optJSONArray("organic") ?: JSONArray()
        return buildList {
            for (index in 0 until organic.length()) {
                val item = organic.optJSONObject(index) ?: continue
                add(SearchItem(item.optString("title"), item.optString("link"), item.optString("snippet"), item.optString("date")))
            }
        }
    }

    suspend fun quota(apiKey: String, region: MiniMaxRegion): String {
        return get(apiKey, region, "/v1/token_plan/remains").toString(2)
    }

    private suspend fun post(apiKey: String, region: MiniMaxRegion, path: String, body: JSONObject): JSONObject {
        return request(apiKey, region, path, "POST", body.toString())
    }

    private suspend fun get(apiKey: String, region: MiniMaxRegion, path: String): JSONObject {
        return request(apiKey, region, path, "GET", null)
    }

    private suspend fun request(
        apiKey: String,
        region: MiniMaxRegion,
        path: String,
        method: String,
        body: String?,
    ): JSONObject = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) throw MiniMaxApiException("请先在设置中填写 API Key")
        val requestBuilder = Request.Builder()
            .url(region.baseUrl + path)
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "application/json")
        val requestBody = body?.toRequestBody(JSON_MEDIA_TYPE)
        requestBuilder.method(method, requestBody)
        client.newCall(requestBuilder.build()).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw MiniMaxApiException("HTTP ${response.code}：${extractError(responseText)}")
            }
            runCatching { JSONObject(responseText) }
                .getOrElse { throw MiniMaxApiException("MiniMax 返回了无法解析的数据") }
        }
    }

    private fun extractError(text: String): String {
        return runCatching {
            val json = JSONObject(text)
            json.optJSONObject("base_resp")?.optString("status_msg")
                ?.takeIf { it.isNotBlank() }
                ?: json.optString("message").takeIf { it.isNotBlank() }
                ?: text.take(240)
        }.getOrDefault(text.take(240))
    }

    private fun saveHexAudio(hex: String, prefix: String): String? {
        if (hex.isBlank() || hex.length % 2 != 0) return null
        return runCatching {
            val bytes = ByteArray(hex.length / 2)
            for (index in bytes.indices) {
                bytes[index] = hex.substring(index * 2, index * 2 + 2).toInt(16).toByte()
            }
            val file = File(appContext.cacheDir, "${prefix}_${System.currentTimeMillis()}.mp3")
            file.writeBytes(bytes)
            file.absolutePath
        }.getOrNull()
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

class MiniMaxApiException(message: String) : Exception(message)
