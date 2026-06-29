package com.personal.englishlearning.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.personal.englishlearning.data.WordEntity
import com.personal.englishlearning.ui.theme.Faint
import com.personal.englishlearning.ui.theme.Green
import com.personal.englishlearning.ui.theme.Ink
import com.personal.englishlearning.ui.theme.Line
import com.personal.englishlearning.ui.theme.Muted
import com.personal.englishlearning.ui.theme.Paper
import com.personal.englishlearning.ui.theme.SurfaceSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Today : AppDestination("today", "今日", Icons.Outlined.Home)
    data object Words : AppDestination("words", "词汇", Icons.Outlined.MenuBook)
    data object Speaking : AppDestination("speaking", "口语", Icons.Outlined.MicNone)
    data object Stats : AppDestination("stats", "统计", Icons.Outlined.BarChart)
    data object Profile : AppDestination("profile", "我的", Icons.Outlined.PersonOutline)
}

private val destinations = listOf(
    AppDestination.Today,
    AppDestination.Words,
    AppDestination.Speaking,
    AppDestination.Stats,
    AppDestination.Profile,
)

@Composable
fun EnglishLearningApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppDestination.Today.route
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddWord by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.transientMessage) {
        uiState.transientMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        containerColor = Paper,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                HorizontalDivider(color = Line)
                NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                    destinations.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label, letterSpacing = 0.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Ink,
                                selectedTextColor = Ink,
                                indicatorColor = SurfaceSoft,
                                unselectedIconColor = Faint,
                                unselectedTextColor = Faint,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Today.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppDestination.Today.route) {
                TodayScreen(
                    state = uiState,
                    onAddWord = { showAddWord = true },
                    onOpenWords = { navController.navigate(AppDestination.Words.route) },
                    onOpenSpeaking = { navController.navigate(AppDestination.Speaking.route) },
                )
            }
            composable(AppDestination.Words.route) {
                WordsScreen(
                    words = uiState.words,
                    onAddWord = { showAddWord = true },
                    onDelete = viewModel::deleteWord,
                )
            }
            composable(AppDestination.Speaking.route) { SpeakingScreen() }
            composable(AppDestination.Stats.route) { StatsScreen(uiState) }
            composable(AppDestination.Profile.route) {
                ProfileScreen(
                    state = uiState,
                    onSaveGoals = viewModel::updateGoals,
                    onReminderChanged = viewModel::setRemindersEnabled,
                    onCheckUpdate = viewModel::checkForUpdate,
                )
            }
        }
    }

    if (showAddWord) {
        AddWordDialog(
            onDismiss = { showAddWord = false },
            onSave = { term, meaning, note ->
                viewModel.addWord(term, meaning, note)
                showAddWord = false
            },
        )
    }
}

@Composable
private fun TodayScreen(
    state: MainUiState,
    onAddWord: () -> Unit,
    onOpenWords: () -> Unit,
    onOpenSpeaking: () -> Unit,
) {
    val dateText = remember {
        SimpleDateFormat("M 月 d 日 EEEE", Locale.SIMPLIFIED_CHINESE).format(Date())
    }
    PageColumn {
        PageHeader("今日", "$dateText · 原生 Alpha")
        StatsPanel(
            listOf(
                StatValue("${state.words.size} / ${state.settings.dailyNewWords}", "今日生词"),
                StatValue("0 / ${state.settings.dailyReviewWords}", "待复习"),
                StatValue("0 / ${state.settings.dailyMinutes}", "学习分钟"),
            ),
        )

        SectionTitle("今日计划")
        if (state.words.isEmpty()) {
            EmptyPanel(
                icon = Icons.Outlined.AutoStories,
                title = "从第一个生词开始",
                copy = "添加内容后，学习任务会出现在这里。",
                action = "添加生词",
                onAction = onAddWord,
            )
        } else {
            SurfacePanel {
                PlanRow("学习新词", "${state.words.size} / ${state.settings.dailyNewWords}", false)
                HorizontalDivider(color = Line)
                PlanRow("复习到期内容", "0 / ${state.settings.dailyReviewWords}", false)
                HorizontalDivider(color = Line)
                PlanRow("完成学习时长", "0 / ${state.settings.dailyMinutes} 分钟", false)
            }
        }

        SectionTitle("快速开始")
        SurfacePanel {
            ActionRow(
                Icons.Outlined.MenuBook,
                "我的生词库",
                "${state.words.size} 个单词",
                onClick = onOpenWords,
            )
            HorizontalDivider(color = Line)
            ActionRow(Icons.Outlined.MicNone, "口语练习", "暂无练习记录", onClick = onOpenSpeaking)
        }
    }
}

@Composable
private fun WordsScreen(
    words: List<WordEntity>,
    onAddWord: () -> Unit,
    onDelete: (WordEntity) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(words, query) {
        words.filter {
            query.isBlank() || it.term.contains(query.trim(), ignoreCase = true) ||
                it.meaning.contains(query.trim(), ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) { PageHeader("生词库", "${words.size} 个收藏") }
                Button(
                    onClick = onAddWord,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("新增")
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索单词或释义") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
            )
            Spacer(Modifier.height(18.dp))
        }

        if (filtered.isEmpty()) {
            item {
                EmptyPanel(
                    icon = Icons.Outlined.MenuBook,
                    title = if (words.isEmpty()) "还没有生词" else "没有匹配内容",
                    copy = if (words.isEmpty()) "添加后会保存在本机。" else "调整关键词后再试一次。",
                    action = if (words.isEmpty()) "添加生词" else null,
                    onAction = onAddWord,
                )
            }
        } else {
            item { SurfacePanel(contentPadding = PaddingValues(0.dp)) {
                filtered.forEachIndexed { index, word ->
                    WordRow(word, onDelete)
                    if (index != filtered.lastIndex) HorizontalDivider(color = Line)
                }
            } }
        }
    }
}

@Composable
private fun SpeakingScreen() {
    val modes = listOf("场景", "跟读", "自由表达")
    var selectedMode by rememberSaveable { mutableStateOf(modes.first()) }
    PageColumn {
        PageHeader("口语练习", "暂无录音")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            modes.forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { selectedMode = mode },
                    label = { Text(mode) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        EmptyPanel(
            icon = Icons.Outlined.MicNone,
            title = "还没有练习记录",
            copy = when (selectedMode) {
                "跟读" -> "练习记录会按句子保存。"
                "自由表达" -> "练习记录会按主题保存。"
                else -> "练习记录会按场景保存。"
            },
        )
    }
}

@Composable
private fun StatsScreen(state: MainUiState) {
    val summary = state.summary
    PageColumn {
        PageHeader("学习统计", "从首次学习开始")
        StatsPanel(
            listOf(
                StatValue(state.words.size.toString(), "总生词"),
                StatValue(summary.addedWords.toString(), "新增记录"),
                StatValue("${summary.studySeconds / 60}", "学习分钟"),
            ),
        )
        SectionTitle("学习记录")
        SurfacePanel {
            MetricRow(Icons.Outlined.MenuBook, "词汇", "${state.words.size} 个")
            HorizontalDivider(color = Line)
            MetricRow(Icons.Outlined.MicNone, "口语", "${summary.speakingSessions} 次")
            HorizontalDivider(color = Line)
            MetricRow(Icons.Outlined.Schedule, "学习事件", "${summary.totalEvents} 条")
        }
    }
}

@Composable
private fun ProfileScreen(
    state: MainUiState,
    onSaveGoals: (Int, Int, Int) -> Unit,
    onReminderChanged: (Boolean) -> Unit,
    onCheckUpdate: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    var newWords by remember(state.settings.dailyNewWords) { mutableIntStateOf(state.settings.dailyNewWords) }
    var reviewWords by remember(state.settings.dailyReviewWords) { mutableIntStateOf(state.settings.dailyReviewWords) }
    var minutes by remember(state.settings.dailyMinutes) { mutableIntStateOf(state.settings.dailyMinutes) }

    PageColumn {
        PageHeader("我的", "个人学习设置")
        SectionTitle("每日计划")
        SurfacePanel {
            GoalRow("新学单词", newWords, 0, 100) { newWords = it }
            HorizontalDivider(color = Line)
            GoalRow("复习单词", reviewWords, 0, 200) { reviewWords = it }
            HorizontalDivider(color = Line)
            GoalRow("学习时长", minutes, 5, 240, "分钟") { minutes = it }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onSaveGoals(newWords, reviewWords, minutes) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) { Text("保存计划") }
        }

        SectionTitle("提醒")
        SurfacePanel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("复习提醒", fontWeight = FontWeight.SemiBold)
                    Text("每天 21:30", style = MaterialTheme.typography.labelMedium, color = Muted)
                }
                Switch(
                    checked = state.settings.remindersEnabled,
                    onCheckedChange = onReminderChanged,
                    colors = SwitchDefaults.colors(checkedTrackColor = Green),
                )
            }
        }

        SectionTitle("版本与更新")
        SurfacePanel {
            ActionRow(
                icon = Icons.Outlined.Refresh,
                title = "检查更新",
                subtitle = "当前 ${MainViewModel.currentVersion} · ${state.update.message}",
                enabled = !state.update.checking,
                onClick = onCheckUpdate,
            )
            val update = state.update.info
            if (update?.hasUpdate == true && update.apkUrl.isNotBlank()) {
                HorizontalDivider(color = Line)
                ActionRow(
                    icon = Icons.Outlined.OpenInNew,
                    title = "下载 ${update.versionName}",
                    subtitle = update.changelog.ifBlank { "打开新版 APK" },
                    onClick = { uriHandler.openUri(update.apkUrl) },
                )
            }
        }

        SectionTitle("数据")
        SurfacePanel {
            MetricRow(Icons.Outlined.Language, "存储方式", "仅保存在本机")
            HorizontalDivider(color = Line)
            MetricRow(Icons.Outlined.Settings, "数据库", "Room v1")
        }
    }
}

@Composable
private fun AddWordDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    var term by rememberSaveable { mutableStateOf("") }
    var meaning by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加生词") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = term,
                    onValueChange = { term = it },
                    label = { Text("英文单词") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                )
                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = { Text("中文释义") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("笔记（可选）") },
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(term, meaning, note) },
                enabled = term.isNotBlank() && meaning.isNotBlank(),
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        shape = RoundedCornerShape(8.dp),
    )
}

@Composable
private fun PageColumn(content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item { Column(content = content) }
    }
}

@Composable
private fun PageHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 22.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.labelMedium, color = Muted, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = Muted,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
    )
}

private data class StatValue(val value: String, val label: String)

@Composable
private fun StatsPanel(stats: List<StatValue>) {
    SurfacePanel(contentPadding = PaddingValues(0.dp)) {
        Row {
            stats.forEachIndexed { index, stat ->
                Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 13.dp)) {
                    Text(stat.value, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(stat.label, style = MaterialTheme.typography.labelMedium, color = Muted, modifier = Modifier.padding(top = 4.dp))
                }
                if (index != stats.lastIndex) Box(Modifier.size(width = 1.dp, height = 54.dp).background(Line))
            }
        }
    }
}

@Composable
private fun SurfacePanel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Line),
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
private fun EmptyPanel(
    icon: ImageVector,
    title: String,
    copy: String,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    SurfacePanel {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = Muted, modifier = Modifier.size(28.dp))
            Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
            Text(copy, style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.padding(top = 4.dp))
            if (action != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.padding(top = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                ) { Text(action) }
            }
        }
    }
}

@Composable
private fun PlanRow(title: String, progress: String, complete: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Outlined.CheckCircleOutline,
            contentDescription = null,
            tint = if (complete) Green else Faint,
            modifier = Modifier.size(22.dp),
        )
        Text(title, modifier = Modifier.padding(start = 10.dp).weight(1f), fontWeight = FontWeight.Medium)
        Text(progress, style = MaterialTheme.typography.labelMedium, color = Muted)
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (enabled) Ink else Faint, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = if (enabled) Ink else Faint)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun WordRow(word: WordEntity, onDelete: (WordEntity) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(word.term, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(word.meaning, style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (word.note.isNotBlank()) Text(word.note, style = MaterialTheme.typography.labelMedium, color = Faint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = { onDelete(word) }) {
            Icon(Icons.Outlined.DeleteOutline, contentDescription = "删除 ${word.term}", tint = Muted)
        }
    }
}

@Composable
private fun MetricRow(icon: ImageVector, title: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Muted, modifier = Modifier.size(21.dp))
        Text(title, modifier = Modifier.padding(start = 12.dp).weight(1f), fontWeight = FontWeight.Medium)
        Text(value, style = MaterialTheme.typography.labelMedium, color = Muted)
    }
}

@Composable
private fun GoalRow(
    title: String,
    value: Int,
    min: Int,
    max: Int,
    suffix: String = "个",
    onValueChange: (Int) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        IconButton(onClick = { onValueChange((value - 1).coerceAtLeast(min)) }, enabled = value > min) {
            Icon(Icons.Outlined.Remove, contentDescription = "减少$title")
        }
        Text("$value $suffix", modifier = Modifier.width(66.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        IconButton(onClick = { onValueChange((value + 1).coerceAtMost(max)) }, enabled = value < max) {
            Icon(Icons.Outlined.Add, contentDescription = "增加$title")
        }
    }
}
