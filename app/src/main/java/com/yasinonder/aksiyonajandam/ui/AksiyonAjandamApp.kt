package com.yasinonder.aksiyonajandam.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yasinonder.aksiyonajandam.alarm.AlarmScheduler
import com.yasinonder.aksiyonajandam.data.ActionDatabase
import com.yasinonder.aksiyonajandam.data.ActionItem
import com.yasinonder.aksiyonajandam.util.VoiceParser
import java.time.LocalDate
import java.time.LocalTime

private enum class AppScreen { HOME, FORM }

@Composable
fun AksiyonAjandamApp() {
    val context = LocalContext.current
    val database = remember { ActionDatabase.get(context) }
    var screen by remember { mutableStateOf(AppScreen.HOME) }
    var reloadKey by remember { mutableIntStateOf(0) }
    val actions = remember(reloadKey) { database.all() }

    when (screen) {
        AppScreen.HOME -> HomeScreen(
            actions = actions,
            onAdd = { screen = AppScreen.FORM },
            onCompletedChange = { item, completed ->
                database.setCompleted(item.id, completed)
                if (completed) AlarmScheduler.cancel(context, item.id)
                reloadKey++
            }
        )

        AppScreen.FORM -> ActionForm(
            onBack = { screen = AppScreen.HOME },
            onSave = { draft ->
                val id = database.insert(draft)
                val saved = draft.copy(id = id)
                AlarmScheduler.schedule(context, saved)
                reloadKey++
                screen = AppScreen.HOME
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    actions: List<ActionItem>,
    onAdd: () -> Unit,
    onCompletedChange: (ActionItem, Boolean) -> Unit
) {
    var filter by remember { mutableStateOf("Bugün") }
    val today = LocalDate.now().toString()

    val visible = remember(actions, filter, today) {
        when (filter) {
            "Bugün" -> actions.filter { !it.completed && it.date == today }
            "Yaklaşan" -> actions.filter { !it.completed && it.date >= today }
            "Tamamlanan" -> actions.filter { it.completed }
            else -> actions
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Aksiyon Ajandam", fontWeight = FontWeight.Bold)
                        Text(
                            "Unutmadan yakala, zamanında aksiyon al",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Bugün", "Yaklaşan", "Tümü", "Tamamlanan").forEach { value ->
                    FilterChip(
                        selected = filter == value,
                        onClick = { filter = value },
                        label = { Text(value) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (visible.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (filter == "Bugün") "Bugün için kayıtlı aksiyon yok." else "Kayıt bulunamadı.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visible, key = { it.id }) { item ->
                        ActionCard(item, onCompletedChange)
                    }
                    item { Spacer(Modifier.height(90.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    item: ActionItem,
    onCompletedChange: (ActionItem, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.completed,
                    onCheckedChange = { onCompletedChange(item, it) }
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.subject.isNotBlank()) {
                        Text(
                            item.subject,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(item.date + "  •  " + item.time + "  •  " + item.type)
            Text(
                "Öncelik: " + item.priority +
                    if (item.alarmEnabled) "  •  Alarm açık" else "  •  Alarm kapalı",
                style = MaterialTheme.typography.labelMedium
            )

            if (item.imageUris.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(item.imageUris.take(4)) { uri ->
                        UriThumbnail(uri)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionForm(
    onBack: () -> Unit,
    onSave: (ActionItem) -> Unit
) {
    val context = LocalContext.current
    val now = LocalTime.now().plusMinutes(5).withSecond(0).withNano(0)

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Genel") }
    var priority by remember { mutableStateOf("Normal") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var time by remember { mutableStateOf(now.toString()) }
    var alarmEnabled by remember { mutableStateOf(true) }
    var extra1 by remember { mutableStateOf("") }
    var extra2 by remember { mutableStateOf("") }
    val images = remember { mutableStateListOf<String>() }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            if (uri.toString() !in images) images.add(uri.toString())
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            .orEmpty()

        if (spoken.isNotBlank()) {
            val parsed = VoiceParser.parse(spoken)
            title = parsed.title
            parsed.date?.let { date = it }
            parsed.time?.let { time = it }
            parsed.type?.let { type = it }
            parsed.priority?.let { priority = it }
        }
    }

    fun openDatePicker() {
        val selected = LocalDate.parse(date)
        DatePickerDialog(
            context,
            { _, year, month, day ->
                date = LocalDate.of(year, month + 1, day).toString()
            },
            selected.year,
            selected.monthValue - 1,
            selected.dayOfMonth
        ).show()
    }

    fun openTimePicker() {
        val selected = LocalTime.parse(time)
        TimePickerDialog(
            context,
            { _, hour, minute ->
                time = LocalTime.of(hour, minute).toString()
            },
            selected.hour,
            selected.minute,
            true
        ).show()
    }

    val dynamicLabels = when (type) {
        "Görev" -> "Sorumlu / kişi" to "Etiket / proje"
        "Randevu" -> "Kişi / kurum" to "Konum"
        "Alışveriş" -> "Mağaza / yer" to "Liste notu"
        "Araç" -> "Araç / plaka" to "KM / servis bilgisi"
        "Evrak" -> "Kurum / belge" to "Referans / dosya no"
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Aksiyon") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Geri") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Aksiyonunu söyle")
                        }
                        voiceLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎙  Sesle doldur")
                }
            }

            item {
                Text("Aksiyon türü", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Genel", "Görev", "Randevu", "Alışveriş", "Araç", "Evrak").forEach { value ->
                        FilterChip(
                            selected = type == value,
                            onClick = { type = value },
                            label = { Text(value) }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ana Başlık") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Konu") },
                    singleLine = true
                )
            }

            dynamicLabels?.let { labels ->
                item {
                    OutlinedTextField(
                        value = extra1,
                        onValueChange = { extra1 = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(labels.first) },
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = extra2,
                        onValueChange = { extra2 = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(labels.second) },
                        singleLine = true
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Açıklama / Detay") },
                    minLines = 3
                )
            }

            item {
                Text("Öncelik", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Normal", "Önemli", "Kritik").forEach { value ->
                        FilterChip(
                            selected = priority == value,
                            onClick = { priority = value },
                            label = { Text(value) }
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { openDatePicker() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📅 " + date)
                    }
                    OutlinedButton(
                        onClick = { openTimePicker() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⏰ " + time)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Alarm", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Zamanı geldiğinde uyar",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Switch(
                        checked = alarmEnabled,
                        onCheckedChange = { alarmEnabled = it }
                    )
                }
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Görseller", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { galleryLauncher.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🖼  Galeriden resim ekle")
                }
            }

            if (images.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(images) { uri ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                UriThumbnail(uri)
                                TextButton(onClick = { images.remove(uri) }) {
                                    Text("Kaldır")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        if (title.isBlank()) return@Button
                        onSave(
                            ActionItem(
                                title = title.trim(),
                                subject = subject.trim(),
                                notes = notes.trim(),
                                type = type,
                                priority = priority,
                                date = date,
                                time = time,
                                alarmEnabled = alarmEnabled,
                                imageUris = images.toList(),
                                extra1 = extra1.trim(),
                                extra2 = extra2.trim()
                            )
                        )
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aksiyonu Kaydet")
                }
                Spacer(Modifier.height(90.dp))
            }
        }
    }
}

@Composable
private fun UriThumbnail(uri: String) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        runCatching {
            context.contentResolver.openInputStream(Uri.parse(uri))
                ?.use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }

    Card(
        modifier = Modifier.size(88.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Not görseli",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Resim")
            }
        }
    }
}
