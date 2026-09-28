package com.example.stashly.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stashly.data.StashItem
import com.example.stashly.data.StashType
import com.example.stashly.ui.StashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: StashViewModel,
    itemId: Long = 0L,
    initialType: StashType = StashType.NOTE,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var type by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var isLoading by remember { mutableStateOf(itemId != 0L) }

    // Load item if editing
    LaunchedEffect(itemId) {
        if (itemId != 0L) {
            val existingItem = viewModel.getItemById(itemId)
            if (existingItem != null) {
                type = existingItem.type
                title = existingItem.title
                content = existingItem.content
                url = existingItem.url ?: ""
                tagsInput = existingItem.tags.joinToString(", ")
                if (existingItem.type == StashType.IMAGE && !existingItem.url.isNullOrBlank()) {
                    try {
                        imageUri = Uri.parse(existingItem.url)
                    } catch (e: Exception) {
                        // ignore invalid uri
                    }
                }
            }
            isLoading = false
        }
    }

    // Photo picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            url = uri.toString()
            // Persist read permissions for local URI if possible
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not a persistable document URI
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (itemId == 0L) "New Stash" else "Edit Stash",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isBlank() && content.isBlank() && url.isBlank() && imageUri == null) {
                                Toast.makeText(
                                    context,
                                    "Please enter a title or content",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@IconButton
                            }

                            val finalUrl = when (type) {
                                StashType.LINK -> url.trim()
                                StashType.IMAGE -> imageUri?.toString() ?: url.trim()
                                else -> null
                            }

                            val tagsList = tagsInput
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }

                            viewModel.saveItem(
                                id = itemId,
                                type = type,
                                title = title.trim(),
                                content = content.trim(),
                                url = finalUrl,
                                tags = tagsList
                            )

                            Toast.makeText(context, "Saved to Stashly", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading item...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type Selector Row
                Text(
                    text = "TYPE",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    StashType.entries.forEachIndexed { index, stashType ->
                        SegmentedButton(
                            selected = type == stashType,
                            onClick = { type = stashType },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = StashType.entries.size
                            )
                        ) {
                            Text(stashType.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Title Input Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Short descriptive title") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Title,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dynamic Input based on Type
                when (type) {
                    StashType.NOTE -> {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Note Content") },
                            placeholder = { Text("Write your thoughts, ideas, or reminders...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 5,
                            maxLines = 10,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    StashType.LINK -> {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("URL / Web Link") },
                            placeholder = { Text("https://example.com/article") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Link Notes / Context (Optional)") },
                            placeholder = { Text("Why is this link useful?") },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    StashType.SNIPPET -> {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Code / Text Snippet") },
                            placeholder = { Text("Paste code snippet, terminal command, or JSON...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            ),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    StashType.IMAGE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (imageUri != null || url.isNotBlank()) {
                                AsyncImage(
                                    model = imageUri ?: url,
                                    contentDescription = "Selected Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (imageUri != null || url.isNotBlank()) "Change Image" else "Select Image from Gallery")
                            }
                        }

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Image Caption / Description (Optional)") },
                            placeholder = { Text("Add context about this image...") },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Tags Input Field
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags") },
                    placeholder = { Text("work, ideas, android (comma separated)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Action Button
                Button(
                    onClick = {
                        if (title.isBlank() && content.isBlank() && url.isBlank() && imageUri == null) {
                            Toast.makeText(
                                context,
                                "Please enter a title or content",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val finalUrl = when (type) {
                            StashType.LINK -> url.trim()
                            StashType.IMAGE -> imageUri?.toString() ?: url.trim()
                            else -> null
                        }

                        val tagsList = tagsInput
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        viewModel.saveItem(
                            id = itemId,
                            type = type,
                            title = title.trim(),
                            content = content.trim(),
                            url = finalUrl,
                            tags = tagsList
                        )

                        Toast.makeText(context, "Saved to Stashly", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (itemId == 0L) "Save to Stashly" else "Update Item",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
