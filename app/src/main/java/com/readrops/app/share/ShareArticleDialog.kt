package com.readrops.app.share

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import com.readrops.app.R

@Composable
fun ShareArticleDialog(
    onDismiss: () -> Unit,
    onShareToOtherApps: () -> Unit,
    onSyncToLumina: () -> Unit
) {
    var showSyncConfirmation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.share_article)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onShareToOtherApps
                ) {
                    Text(text = stringResource(R.string.share_to_other_apps))
                }

                TextButton(
                    onClick = { showSyncConfirmation = true }
                ) {
                    Text(text = stringResource(R.string.sync_to_lumina))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.back))
            }
        }
    )

    if (showSyncConfirmation) {
        AlertDialog(
            onDismissRequest = { showSyncConfirmation = false },
            title = { Text(text = stringResource(R.string.sync_to_lumina)) },
            text = { Text(text = stringResource(R.string.lumina_confirm_sync)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSyncConfirmation = false
                        onSyncToLumina()
                    }
                ) {
                    Text(text = stringResource(R.string.sync_to_lumina))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncConfirmation = false }) {
                    Text(text = stringResource(R.string.back))
                }
            }
        )
    }
}
