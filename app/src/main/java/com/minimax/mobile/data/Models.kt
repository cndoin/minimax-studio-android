package com.minimax.mobile.data

enum class MiniMaxRegion(
    val id: String,
    val label: String,
    val baseUrl: String,
) {
    CN("cn", "国内平台", "https://api.minimaxi.com"),
    GLOBAL("global", "国际平台", "https://api.minimax.io");

    companion object {
        fun fromId(id: String?): MiniMaxRegion = entries.firstOrNull { it.id == id } ?: CN
    }
}

enum class StudioTab(val label: String) {
    CHAT("对话"),
    CREATE("创作"),
    SETTINGS("设置"),
}

enum class CreateTool(
    val title: String,
    val subtitle: String,
    val icon: String,
) {
    IMAGE("图片", "文字生成视觉作品", "✦"),
    VIDEO("视频", "让画面动起来", "▶"),
    SPEECH("语音", "自然流畅的声音", "◉"),
    MUSIC("音乐", "创作专属配乐", "♫"),
    VISION("视觉理解", "让 AI 看懂图片", "◌"),
    SEARCH("网络搜索", "获取最新信息", "⌕"),
}

data class ChatMessageUi(
    val id: Long,
    val role: String,
    val content: String,
)

data class GeneratedMedia(
    val type: String,
    val title: String,
    val remoteUrl: String? = null,
    val localPath: String? = null,
)

data class SearchItem(
    val title: String,
    val link: String,
    val snippet: String,
    val date: String,
)
