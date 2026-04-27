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
    onSyncLinkToLumina: () -> Unit,
    onSyncFullTextToLumina: () -> Unit
) {
    var pendingLuminaSync by remember { mutableStateOf<LuminaSyncMode?>(null) }

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
                    onClick = { pendingLuminaSync = LuminaSyncMode.LINK }
                ) {
                    Text(text = stringResource(R.string.sync_to_lumina_link))
                }

                TextButton(
                    onClick = { pendingLuminaSync = LuminaSyncMode.FULL_TEXT }
                ) {
                    Text(text = stringResource(R.string.sync_to_lumina_full_text))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.back))
            }
        }
    )

    pendingLuminaSync?.let { syncMode ->
        AlertDialog(
            onDismissRequest = { pendingLuminaSync = null },
            title = { Text(text = stringResource(syncMode.titleRes)) },
            text = { Text(text = stringResource(syncMode.confirmationRes)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingLuminaSync = null
                        when (syncMode) {
                            LuminaSyncMode.LINK -> onSyncLinkToLumina()
                            LuminaSyncMode.FULL_TEXT -> onSyncFullTextToLumina()
                        }
                    }
                ) {
                    Text(text = stringResource(syncMode.titleRes))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingLuminaSync = null }) {
                    Text(text = stringResource(R.string.back))
                }
            }
        )
    }
}

private enum class LuminaSyncMode(
    val titleRes: Int,
    val confirmationRes: Int
) {
    LINK(
        R.string.sync_to_lumina_link,
        R.string.lumina_confirm_sync_link
    ),
    FULL_TEXT(
        R.string.sync_to_lumina_full_text,
        R.string.lumina_confirm_sync_full_text
    )
}
