package com.minimax.mobile.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minimax.mobile.data.ApiKeyStore
import com.minimax.mobile.data.ChatMessageUi
import com.minimax.mobile.data.CreateTool
import com.minimax.mobile.data.GeneratedMedia
import com.minimax.mobile.data.MiniMaxApi
import com.minimax.mobile.data.MiniMaxRegion
import com.minimax.mobile.data.SearchItem
import com.minimax.mobile.data.StudioTab
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class MiniMaxViewModel(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val apiKeyStore = ApiKeyStore(context)
    private val api = MiniMaxApi(context)

    var currentTab by mutableStateOf(StudioTab.CHAT)
        private set
    var selectedTool by mutableStateOf(CreateTool.IMAGE)
        private set
    var apiKey by mutableStateOf(apiKeyStore.readKey())
        private set
    var region by mutableStateOf(apiKeyStore.readRegion())
        private set
    var selectedModel by mutableStateOf("MiniMax-M2.7")
    var isBusy by mutableStateOf(false)
        private set
    var progressText by mutableStateOf("")
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var quotaText by mutableStateOf<String?>(null)
        private set
    var visionText by mutableStateOf<String?>(null)
        private set

    val messages = mutableStateListOf(
        ChatMessageUi(
            id = 1L,
            role = "assistant",
            content = "你好，我是 MiniMax Studio。\n\n输入 API Key 后，你可以在这里聊天，也可以生成图片、视频、语音和音乐。",
        ),
    )
    val imageUrls = mutableStateListOf<String>()
    val mediaResults = mutableStateListOf<GeneratedMedia>()
    val searchResults = mutableStateListOf<SearchItem>()

    fun selectTab(tab: StudioTab) {
        currentTab = tab
        notice = null
    }

    fun selectTool(tool: CreateTool) {
        selectedTool = tool
        notice = null
        visionText = null
    }

    fun saveSettings(newKey: String, newRegion: MiniMaxRegion) {
        try {
        apiKeyStore.save(newKey.trim(), newRegion)
        apiKey = newKey.trim()
        region = newRegion
        notice = "设置已保存，API Key 只会保存在本机。"
        } catch (error: Exception) {
            notice = "API Key 保存失败：${error.message ?: "当前设备不支持安全存储"}"
        }
    }

    fun clearSettings() {
        apiKeyStore.clear()
        apiKey = ""
        quotaText = null
        notice = "已清除本机保存的 API Key。"
    }

    fun sendMessage(text: String) {
        val clean = text.trim()
        if (clean.isBlank() || isBusy) return
        if (!requireApiKey()) return
        messages += ChatMessageUi(System.currentTimeMillis(), "user", clean)
        isBusy = true
        notice = null
        scope.launch {
            try {
                val answer = api.chat(apiKey, region, selectedModel, messages.toList())
                messages += ChatMessageUi(System.currentTimeMillis(), "assistant", answer.ifBlank { "MiniMax 没有返回文本。" })
            } catch (error: Exception) {
                notice = error.message ?: "请求失败，请检查网络和 API Key。"
            } finally {
                isBusy = false
            }
        }
    }

    fun generateImage(prompt: String, aspectRatio: String, count: Int) {
        if (prompt.isBlank() || isBusy || !requireApiKey()) return
        runBusy("正在生成图片…") {
            imageUrls.clear()
            imageUrls += api.generateImage(apiKey, region, prompt.trim(), aspectRatio, count)
            if (imageUrls.isEmpty()) notice = "图片生成完成，但没有返回图片地址。"
        }
    }

    fun generateVideo(prompt: String, model: String) {
        if (prompt.isBlank() || isBusy || !requireApiKey()) return
        runBusy("正在创建视频任务…") {
            mediaResults.removeAll { it.type == "video" }
            mediaResults += api.generateVideo(apiKey, region, model, prompt.trim()) { progressText = it }
        }
    }

    fun synthesizeSpeech(text: String, voice: String) {
        if (text.isBlank() || isBusy || !requireApiKey()) return
        runBusy("正在合成语音…") {
            mediaResults.removeAll { it.type == "audio" && it.title == "语音合成" }
            mediaResults += api.synthesizeSpeech(apiKey, region, text.trim(), voice)
        }
    }

    fun generateMusic(prompt: String, lyrics: String) {
        if (prompt.isBlank() || isBusy || !requireApiKey()) return
        runBusy("正在创作音乐…") {
            mediaResults.removeAll { it.type == "audio" && it.title == "音乐生成" }
            mediaResults += api.generateMusic(apiKey, region, prompt.trim(), lyrics.trim())
        }
    }

    fun describeImage(input: String, prompt: String) {
        if (input.isBlank() || isBusy || !requireApiKey()) return
        runBusy("正在理解图片…") {
            visionText = api.describeImage(apiKey, region, input.trim(), prompt.ifBlank { "请详细描述这张图片。" })
        }
    }

    fun search(query: String) {
        if (query.isBlank() || isBusy || !requireApiKey()) return
        runBusy("正在搜索网络…") {
            searchResults.clear()
            searchResults += api.search(apiKey, region, query.trim())
            if (searchResults.isEmpty()) notice = "没有找到相关结果。"
        }
    }

    fun loadQuota() {
        if (isBusy || !requireApiKey()) return
        runBusy("正在读取用量…") {
            quotaText = api.quota(apiKey, region)
        }
    }

    private fun runBusy(message: String, block: suspend () -> Unit) {
        progressText = message
        isBusy = true
        notice = null
        scope.launch {
            try {
                block()
            } catch (error: Exception) {
                notice = error.message ?: "请求失败，请检查网络和 API Key。"
            } finally {
                isBusy = false
                progressText = ""
            }
        }
    }

    private fun requireApiKey(): Boolean {
        if (apiKey.isBlank()) {
            currentTab = StudioTab.SETTINGS
            notice = "请先在设置中填写 MiniMax API Key。"
            return false
        }
        return true
    }

    fun close() {
        scope.cancel()
    }
}
