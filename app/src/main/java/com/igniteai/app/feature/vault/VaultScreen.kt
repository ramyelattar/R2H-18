package com.igniteai.app.feature.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.igniteai.app.data.model.VaultItem
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.R2H18Card
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.DeepCharcoal
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

@Composable
fun VaultScreen(
    items: List<VaultItem>,
    onAddItem: (title: String, body: String, category: String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onBack: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(AbyssBlack),
    ) {
        EmberParticles(particleCount = 6, intensity = 2)

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }
                Text(
                    text = "Vault",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MoltenGold,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Your vault is empty.\nTap + to add private content.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        VaultItemCard(
                            item = item,
                            onDelete = { onDeleteItem(item.id) },
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            shape = CircleShape,
            containerColor = EmberOrange,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
        }

        if (showAddDialog) {
            AddVaultItemDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, body, category ->
                    onAddItem(title, body, category)
                    showAddDialog = false
                },
            )
        }
    }
}

@Composable
private fun VaultItemCard(
    item: VaultItem,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    R2H18Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* decrypt and show - future detail screen */ },
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = MoltenGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.category,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap to decrypt",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.clickable { showDeleteConfirm = true },
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete?", color = TextSecondary) },
            text = { Text("This cannot be undone.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) { Text("Delete", color = EmberOrange) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DeepCharcoal,
        )
    }
}

@Composable
private fun AddVaultItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, body: String, category: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fantasy") }

    val categories = listOf("Fantasy", "Memory", "Wish", "Secret")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Vault", color = MoltenGold) },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextSecondary,
                        unfocusedTextColor = TextSecondary,
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Content") },
                    minLines = 3,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextSecondary,
                        unfocusedTextColor = TextSecondary,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Category", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                // Simple text-based category picker
                categories.forEach { cat ->
                    Text(
                        text = if (cat == category) "● $cat" else "○ $cat",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (cat == category) EmberOrange else TextMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { category = cat }
                            .padding(vertical = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank() && body.isNotBlank()) onConfirm(title, body, category) },
            ) { Text("Save", color = EmberOrange) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        },
        containerColor = DeepCharcoal,
    )
}
