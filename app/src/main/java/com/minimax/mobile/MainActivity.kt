package com.minimax.mobile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimax.mobile.data.ChatMessageUi
import com.minimax.mobile.data.CreateTool
import com.minimax.mobile.data.GeneratedMedia
import com.minimax.mobile.data.MiniMaxRegion
import com.minimax.mobile.data.StudioTab
import com.minimax.mobile.ui.MiniMaxViewModel
import com.minimax.mobile.ui.theme.MiniMaxTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.asImageBitmap

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MiniMaxTheme { MiniMaxStudioApp() } }
    }
}

@Composable
private fun MiniMaxStudioApp() {
    val context = LocalContext.current
    val vm = remember { MiniMaxViewModel(context.applicationContext) }
    DisposableEffect(vm) { onDispose { vm.close() } }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = vm.currentTab == StudioTab.CHAT,
                    onClick = { vm.selectTab(StudioTab.CHAT) },
                    icon = { Icon(Icons.Default.ChatBubbleOutline, null) },
                    label = { Text("对话") },
                )
                NavigationBarItem(
                    selected = vm.currentTab == StudioTab.CREATE,
                    onClick = { vm.selectTab(StudioTab.CREATE) },
                    icon = { Icon(Icons.Default.AutoAwesome, null) },
                    label = { Text("创作") },
                )
                NavigationBarItem(
                    selected = vm.currentTab == StudioTab.SETTINGS,
                    onClick = { vm.selectTab(StudioTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("设置") },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .safeDrawingPadding(),
        ) {
            AppHeader(vm)
            vm.notice?.let { NoticeBar(it) }
            if (vm.isBusy) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            when (vm.currentTab) {
                StudioTab.CHAT -> ChatScreen(vm)
                StudioTab.CREATE -> CreateScreen(vm)
                StudioTab.SETTINGS -> SettingsScreen(vm)
            }
        }
    }
}

@Composable
private fun AppHeader(vm: MiniMaxViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text("M", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("MiniMax Studio", fontWeight = FontWeight.Bold, fontSize = 19.sp)
            Text(
                text = if (vm.apiKey.isBlank()) "输入 API Key 开始使用" else "已连接 · ${vm.region.label}",
                style = MaterialTheme.typography.labelMedium,
                color = if (vm.apiKey.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.secondary,
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(if (vm.apiKey.isBlank()) "未连接" else "READY", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun NoticeBar(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(10.dp))
            Text(text, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ChatScreen(vm: MiniMaxViewModel) {
    var input by rememberSaveable { mutableStateOf("") }
    var modelMenu by rememberSaveable { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("智能对话", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Box {
                OutlinedButton(onClick = { modelMenu = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                    Text(vm.selectedModel, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
                }
                DropdownMenu(expanded = modelMenu, onDismissRequest = { modelMenu = false }) {
                    listOf("MiniMax-M2.7", "MiniMax-M2.5", "MiniMax-M2.1", "MiniMax-M3").forEach { model ->
                        DropdownMenuItem(text = { Text(model) }, onClick = { vm.selectedModel = model; modelMenu = false })
                    }
                }
            }
        }
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(vm.messages.size, key = { vm.messages[it].id }) { index ->
                MessageBubble(vm.messages[index], onCopy = { clipboard.setText(AnnotatedString(vm.messages[index].content)) })
            }
            if (vm.isBusy) {
                item { TypingIndicator(vm.progressText.ifBlank { "MiniMax 正在思考…" }) }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("问 MiniMax 任何问题…") },
                maxLines = 4,
                shape = RoundedCornerShape(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { vm.sendMessage(input); input = "" },
                enabled = input.isNotBlank() && !vm.isBusy,
                modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessageUi, onCopy: () -> Unit) {
    val user = message.role == "user"
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
        Card(
            modifier = Modifier.fillMaxWidth(if (user) 0.88f else 0.96f),
            shape = RoundedCornerShape(22.dp, 22.dp, if (user) 6.dp else 22.dp, if (user) 22.dp else 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (user) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(message.content, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                if (!user) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onCopy, contentPadding = PaddingValues(0.dp)) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("复制", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(10.dp)) {}
        Spacer(Modifier.width(10.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CreateScreen(vm: MiniMaxViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        Text("全模态创作", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("一把钥匙，解锁 MiniMax 的全部能力", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(18.dp))
        ToolRow(vm, CreateTool.IMAGE, CreateTool.VIDEO)
        Spacer(Modifier.height(10.dp))
        ToolRow(vm, CreateTool.SPEECH, CreateTool.MUSIC)
        Spacer(Modifier.height(10.dp))
        ToolRow(vm, CreateTool.VISION, CreateTool.SEARCH)
        Spacer(Modifier.height(22.dp))
        Text(vm.selectedTool.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(vm.selectedTool.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        when (vm.selectedTool) {
            CreateTool.IMAGE -> ImageEditor(vm)
            CreateTool.VIDEO -> VideoEditor(vm)
            CreateTool.SPEECH -> SpeechEditor(vm)
            CreateTool.MUSIC -> MusicEditor(vm)
            CreateTool.VISION -> VisionEditor(vm)
            CreateTool.SEARCH -> SearchEditor(vm)
        }
        Spacer(Modifier.height(28.dp))
        if (vm.progressText.isNotBlank()) Text(vm.progressText, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium)
        ImageResults(vm)
        MediaResults(vm)
        vm.visionText?.let { ResultCard("视觉理解结果", it) }
        SearchResults(vm)
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
private fun ToolRow(vm: MiniMaxViewModel, first: CreateTool, second: CreateTool) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ToolCard(first, vm.selectedTool == first, { vm.selectTool(first) }, Modifier.weight(1f))
        ToolCard(second, vm.selectedTool == second, { vm.selectTool(second) }, Modifier.weight(1f))
    }
}

@Composable
private fun ToolCard(tool: CreateTool, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(tool.icon, fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Text(tool.title, fontWeight = FontWeight.Bold)
            Text(tool.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ImageEditor(vm: MiniMaxViewModel) {
    var prompt by rememberSaveable { mutableStateOf("") }
    var aspect by rememberSaveable { mutableStateOf("16:9") }
    var count by rememberSaveable { mutableStateOf(1) }
    EditorCard {
        OutlinedTextField(prompt, { prompt = it }, modifier = Modifier.fillMaxWidth(), label = { Text("描述你想生成的画面") }, minLines = 3)
        Spacer(Modifier.height(12.dp))
        Text("画幅", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1:1", "16:9", "9:16", "4:3").forEach { ratio -> FilterChip(selected = aspect == ratio, onClick = { aspect = ratio }, label = { Text(ratio) }) }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("数量", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            listOf(1, 2, 3, 4).forEach { number -> FilterChip(selected = count == number, onClick = { count = number }, label = { Text(number.toString()) }, modifier = Modifier.padding(start = 4.dp)) }
        }
        ActionButton("生成图片", Icons.Default.Image, enabled = prompt.isNotBlank() && !vm.isBusy) { vm.generateImage(prompt, aspect, count) }
    }
}

@Composable
private fun VideoEditor(vm: MiniMaxViewModel) {
    var prompt by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("MiniMax-Hailuo-2.3") }
    var menu by rememberSaveable { mutableStateOf(false) }
    EditorCard {
        OutlinedTextField(prompt, { prompt = it }, modifier = Modifier.fillMaxWidth(), label = { Text("描述视频内容和镜头运动") }, minLines = 3)
        Spacer(Modifier.height(12.dp))
        Box {
            OutlinedButton(onClick = { menu = true }) { Text(model) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                listOf("MiniMax-Hailuo-2.3", "MiniMax-H3").forEach { value -> DropdownMenuItem(text = { Text(value) }, onClick = { model = value; menu = false }) }
            }
        }
        Text("视频生成需要等待几分钟，应用会自动轮询任务状态。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        ActionButton("生成视频", Icons.Default.Videocam, enabled = prompt.isNotBlank() && !vm.isBusy) { vm.generateVideo(prompt, model) }
    }
}

@Composable
private fun SpeechEditor(vm: MiniMaxViewModel) {
    var text by rememberSaveable { mutableStateOf("") }
    var voice by rememberSaveable { mutableStateOf("male-qn-qingse") }
    EditorCard {
        OutlinedTextField(text, { text = it }, modifier = Modifier.fillMaxWidth(), label = { Text("输入要朗读的文字") }, minLines = 4)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(voice, { voice = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Voice ID") }, singleLine = true)
        ActionButton("合成语音", Icons.Default.GraphicEq, enabled = text.isNotBlank() && !vm.isBusy) { vm.synthesizeSpeech(text, voice) }
    }
}

@Composable
private fun MusicEditor(vm: MiniMaxViewModel) {
    var prompt by rememberSaveable { mutableStateOf("") }
    var lyrics by rememberSaveable { mutableStateOf("") }
    EditorCard {
        OutlinedTextField(prompt, { prompt = it }, modifier = Modifier.fillMaxWidth(), label = { Text("音乐风格、情绪和场景") }, minLines = 2)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(lyrics, { lyrics = it }, modifier = Modifier.fillMaxWidth(), label = { Text("歌词（可选）") }, minLines = 4)
        ActionButton("生成音乐", Icons.Default.GraphicEq, enabled = prompt.isNotBlank() && !vm.isBusy) { vm.generateMusic(prompt, lyrics) }
    }
}

@Composable
private fun VisionEditor(vm: MiniMaxViewModel) {
    var image by rememberSaveable { mutableStateOf("") }
    var prompt by rememberSaveable { mutableStateOf("请详细描述这张图片。") }
    EditorCard {
        OutlinedTextField(image, { image = it }, modifier = Modifier.fillMaxWidth(), label = { Text("图片 URL 或 file_id") }, singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(prompt, { prompt = it }, modifier = Modifier.fillMaxWidth(), label = { Text("你想了解什么？") }, minLines = 2)
        Text("当前版本先支持公网图片 URL；可直接粘贴 filecdn 等图片链接。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        ActionButton("开始理解", Icons.Default.Image, enabled = image.isNotBlank() && !vm.isBusy) { vm.describeImage(image, prompt) }
    }
}

@Composable
private fun SearchEditor(vm: MiniMaxViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    EditorCard {
        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("搜索你想了解的内容") }, singleLine = true)
        ActionButton("搜索网络", Icons.Default.Search, enabled = query.isNotBlank() && !vm.isBusy) { vm.search(query) }
    }
}

@Composable
private fun EditorCard(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun ActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().padding(top = 14.dp), shape = RoundedCornerShape(16.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun ImageResults(vm: MiniMaxViewModel) {
    if (vm.imageUrls.isEmpty()) return
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("生成结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            vm.imageUrls.forEach { url ->
                RemoteImage(url)
            }
        }
    }
}

@Composable
private fun RemoteImage(url: String) {
    var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(url) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching { java.net.URL(url).openStream().use(BitmapFactory::decodeStream) }.getOrNull()
        }
    }
    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "生成图片",
            modifier = Modifier.size(190.dp).clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Surface(modifier = Modifier.size(190.dp).clip(RoundedCornerShape(18.dp)), color = MaterialTheme.colorScheme.surfaceVariant) {
            Box(contentAlignment = Alignment.Center) { Text("加载中…", style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun MediaResults(vm: MiniMaxViewModel) {
    vm.mediaResults.forEach { media -> MediaCard(media) }
}

@Composable
private fun MediaCard(media: GeneratedMedia) {
    val context = LocalContext.current
    var playing by rememberSaveable(media.localPath, media.remoteUrl) { mutableStateOf(false) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(media.localPath) {
        onDispose { player?.release(); player = null }
    }
    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(media.title, fontWeight = FontWeight.Bold)
            Text(if (media.localPath != null) "已保存到应用缓存" else "结果链接有效期以 MiniMax 返回为准", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (media.type == "audio") {
                    Button(onClick = {
                        if (playing) {
                            player?.pause()
                            playing = false
                        } else {
                            player?.release()
                            player = if (media.localPath != null) MediaPlayer.create(context, Uri.fromFile(java.io.File(media.localPath))) else null
                            if (player != null) { player?.start(); playing = true }
                        }
                    }, enabled = media.localPath != null, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)) {
                        Icon(if (playing) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(5.dp))
                        Text(if (playing) "暂停" else "试听")
                    }
                }
                OutlinedButton(onClick = {
                    val value = media.remoteUrl ?: return@OutlinedButton
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(value)))
                }, enabled = media.remoteUrl != null, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)) {
                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("打开")
                }
            }
        }
    }
}

@Composable
private fun ResultCard(title: String, content: String) {
    val clipboard = LocalClipboardManager.current
    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { clipboard.setText(AnnotatedString(content)) }) { Icon(Icons.Default.ContentCopy, null) }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(content, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun SearchResults(vm: MiniMaxViewModel) {
    if (vm.searchResults.isEmpty()) return
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("搜索结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        vm.searchResults.forEach { result ->
            val context = LocalContext.current
            Card(modifier = Modifier.fillMaxWidth().padding(top = 10.dp).clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.link))) }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(result.title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(result.snippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                    Text(result.link, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(vm: MiniMaxViewModel) {
    var keyText by rememberSaveable { mutableStateOf(vm.apiKey) }
    var region by rememberSaveable { mutableStateOf(vm.region.id) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Text("连接设置", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("API Key 仅保存在这台设备上，不会上传到第三方服务器。", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(18.dp))
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(keyText, { keyText = it }, modifier = Modifier.fillMaxWidth(), label = { Text("MiniMax API Key") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                Spacer(Modifier.height(14.dp))
                Text("服务区域", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniMaxRegion.entries.forEach { item -> FilterChip(selected = region == item.id, onClick = { region = item.id }, label = { Text(item.label) }) }
                }
                Button(onClick = { vm.saveSettings(keyText, MiniMaxRegion.fromId(region)) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("保存并连接")
                }
                OutlinedButton(onClick = { keyText = ""; vm.clearSettings() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("清除本机 Key")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Token Plan 用量", fontWeight = FontWeight.Bold)
                        Text("从当前区域读取额度信息", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { vm.loadQuota() }, enabled = !vm.isBusy) { Icon(Icons.Default.Refresh, null) }
                }
                vm.quotaText?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 10.dp)) }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("MiniMax Studio 1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("基于 MiniMax 开放平台 API 构建", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
