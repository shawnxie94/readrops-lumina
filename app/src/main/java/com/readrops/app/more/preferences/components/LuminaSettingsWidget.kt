package com.readrops.app.more.preferences.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.readrops.app.R
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ShortSpacer

@Composable
fun LuminaSettingsWidget(
    apiUrl: String,
    internalToken: String,
    skipAiProcessing: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var localApiUrl by remember { mutableStateOf(apiUrl) }
    var localInternalToken by remember { mutableStateOf(internalToken) }
    var localSkipAiProcessing by remember { mutableStateOf(skipAiProcessing) }

    PreferenceBaseDialog(
        title = stringResource(R.string.lumina),
        onDismiss = onDismiss
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.lumina_settings_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = AlertDialogDefaults.textContentColor
            )

            MediumSpacer()

            TextField(
                value = localApiUrl,
                onValueChange = { localApiUrl = it },
                label = { Text(stringResource(R.string.lumina_api_url)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ShortSpacer()

            TextField(
                value = localInternalToken,
                onValueChange = { localInternalToken = it },
                label = { Text(stringResource(R.string.lumina_internal_token)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            MediumSpacer()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.lumina_skip_ai_processing),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = localSkipAiProcessing,
                    onCheckedChange = { localSkipAiProcessing = it }
                )
            }

            MediumSpacer()

            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.back))
                }

                TextButton(
                    onClick = {
                        onSave(localApiUrl, localInternalToken, localSkipAiProcessing)
                    }
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
        }
    }
}
