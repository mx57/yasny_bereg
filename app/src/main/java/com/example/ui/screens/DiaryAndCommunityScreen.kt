package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.CommunityPostEntity
import com.example.data.local.EmotionalState
import com.example.data.local.WellnessDiaryEntity
import com.example.security.CryptoVault
import com.example.ui.UserProfileState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private enum class DiarySubTab(val titleRu: String) {
    DIARY("Дневник самочувствия (AES-256)"),
    COMMUNITY("Сообщество поддержки")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiaryAndCommunityScreen(
    profile: UserProfileState,
    diaryEntries: List<WellnessDiaryEntity>,
    communityPosts: List<CommunityPostEntity>,
    onAddDiaryEntry: (EmotionalState, Int, Float, String, String, String?, Boolean) -> Unit,
    onDeleteDiaryEntry: (Long) -> Unit,
    onAddCommunityPost: (String, Int, String, String) -> Unit,
    onToggleSupportPost: (CommunityPostEntity) -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)

    var activeSubTab by remember { mutableStateOf(DiarySubTab.DIARY) }

    // Diary Form State
    var selectedEmotion by remember(profile.currentEmotion) { mutableStateOf(profile.currentEmotion) }
    var cravingSlider by remember { mutableFloatStateOf(2f) }
    var sleepSlider by remember { mutableFloatStateOf(7.5f) }
    var symptomsText by remember { mutableStateOf("") }
    var commentText by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Zero-permission Android Photo Picker (compliant with Google Play policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    // Community Post Form State
    var authorNameInput by remember { mutableStateOf(profile.userName) }
    var authorAgeInput by remember { mutableStateOf("62") }
    var postMessageInput by remember { mutableStateOf("") }

    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiarySubTab.entries.forEach { tab ->
                    val selected = activeSubTab == tab
                    Button(
                        onClick = { activeSubTab = tab },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 54.dp)
                            .testTag("subtab_${tab.name}"),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = if (tab == DiarySubTab.DIARY) Icons.Default.MenuBook else Icons.Default.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (tab == DiarySubTab.DIARY) "Дневник" else "Сообщество")
                    }
                }
            }
        }

        if (activeSubTab == DiarySubTab.DIARY) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_diary_entry_card"),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Новая запись в дневник самочувствия",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Text(
                            text = "Все записи и комментарии шифруются ключом AES-256-GCM (Android Keystore) и хранятся строго на вашем телефоне.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "1. Эмоциональное состояние:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EmotionalState.entries.forEach { emo ->
                                FilterChip(
                                    selected = selectedEmotion == emo,
                                    onClick = { selectedEmotion = emo },
                                    label = { Text(emo.titleRu) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "2. Уровень тяги к алкоголю: ${cravingSlider.roundToInt()} из 10",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (cravingSlider >= 7f) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = cravingSlider,
                            onValueChange = { cravingSlider = it },
                            valueRange = 0f..10f,
                            steps = 9,
                            modifier = Modifier.testTag("diary_craving_slider")
                        )

                        Text(
                            text = "3. Продолжительность сна: ${String.format(Locale.US, "%.1f", sleepSlider)} ч",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Slider(
                            value = sleepSlider,
                            onValueChange = { sleepSlider = it },
                            valueRange = 2f..12f,
                            steps = 19
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = symptomsText,
                            onValueChange = { symptomsText = it },
                            label = { Text("Физическое самочувствие (сон, давление, аппетит, бодрость)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("diary_symptoms_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            label = { Text("Личный комментарий, мысли или благодарность за день") },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("diary_comment_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 52.dp)
                                    .testTag("attach_diary_photo_button")
                            ) {
                                Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (selectedPhotoUri != null) "Фото выбрано ✓"
                                    else "Прикрепить фото"
                                )
                            }
                        }

                        if (selectedPhotoUri != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AsyncImage(
                                model = selectedPhotoUri,
                                contentDescription = "Прикрепленное фото дневника",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                onAddDiaryEntry(
                                    selectedEmotion,
                                    cravingSlider.roundToInt(),
                                    sleepSlider,
                                    symptomsText.ifBlank { "Без выраженных симптомов" },
                                    commentText.ifBlank { "Трезвый день зафиксирован в дневнике." },
                                    selectedPhotoUri?.toString(),
                                    profile.checkedSupplementIds.isNotEmpty()
                                )
                                symptomsText = ""
                                commentText = ""
                                selectedPhotoUri = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .testTag("save_diary_entry_button")
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Зашифровать и сохранить запись",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            items(diaryEntries, key = { it.id }) { entry ->
                val emotion = runCatching { EmotionalState.valueOf(entry.emotionalStateName) }
                    .getOrDefault(EmotionalState.CALM)
                val decryptedSymptoms = remember(entry.encryptedSymptoms) {
                    CryptoVault.decrypt(entry.encryptedSymptoms)
                }
                val decryptedComment = remember(entry.encryptedComment) {
                    CryptoVault.decrypt(entry.encryptedComment)
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diary_entry_card_${entry.id}"),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dateFormatter.format(Date(entry.timestampMillis)),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${emotion.titleRu} • Тяга: ${entry.cravingLevel}/10 • Сон: ${entry.sleepHours} ч",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            IconButton(onClick = { onDeleteDiaryEntry(entry.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить запись",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!entry.photoUriString.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AsyncImage(
                                model = entry.photoUriString,
                                contentDescription = "Фотография из дневника",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(175.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Симптомы: $decryptedSymptoms",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = decryptedComment,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🔒 Расшифровано локально из AES-256-GCM",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // SUPPORT COMMUNITY SUBTAB
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("community_compose_card"),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Круг взаимной поддержки трезвости",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Поделитесь своим опытом или словами ободрения. Ваши отклики работают даже в оффлайн-режиме.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = authorNameInput,
                                onValueChange = { authorNameInput = it },
                                label = { Text("Ваше имя") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = authorAgeInput,
                                onValueChange = { authorAgeInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Возраст") },
                                modifier = Modifier.width(105.dp),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = postMessageInput,
                            onValueChange = { postMessageInput = it },
                            label = { Text("Напишите слова поддержки или расскажите о своем дне...") },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("community_post_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (postMessageInput.isNotBlank()) {
                                    onAddCommunityPost(
                                        authorNameInput,
                                        authorAgeInput.toIntOrNull() ?: 60,
                                        "Личный опыт (${profile.soberDays} дн. трезвости)",
                                        postMessageInput
                                    )
                                    postMessageInput = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp)
                                .testTag("publish_community_post_button")
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Опубликовать в сообществе")
                        }
                    }
                }
            }

            items(communityPosts, key = { it.id }) { post ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("community_post_${post.id}"),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${post.authorName}, ${post.authorAge} лет",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Трезвость: ${post.soberDays} дн. • ${post.categoryTag}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = post.messageText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormatter.format(Date(post.timestampMillis)),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { onToggleSupportPost(post) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("support_post_btn_${post.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = if (post.isSupportedByMe) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Держись! (${post.supportCount})")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
