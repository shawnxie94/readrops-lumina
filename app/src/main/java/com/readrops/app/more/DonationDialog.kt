package com.readrops.app.more

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.readrops.app.R
import com.readrops.app.util.components.dialog.BaseDialog
import com.readrops.app.util.theme.MediumSpacer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DonationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLargeQrCode by remember { mutableStateOf(false) }

    BaseDialog(
        title = stringResource(id = R.string.make_donation),
        icon = painterResource(id = R.drawable.ic_donation),
        onDismiss = onDismiss
    ) {
        Column {
            Text(
                text = stringResource(R.string.donation_text)
            )

            Text(
                text = stringResource(R.string.donation_qr_hint)
            )

            MediumSpacer()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .combinedClickable(
                        onClick = { showLargeQrCode = true },
                        onLongClick = {
                            coroutineScope.launch {
                                val saved = saveDonationQrCode(context)
                                Toast.makeText(
                                    context,
                                    if (saved) R.string.donation_qr_saved else R.string.error_image_download,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.donation_qr),
                    contentDescription = stringResource(R.string.make_donation),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showLargeQrCode) {
        DonationQrCodeDialog(
            onDismiss = { showLargeQrCode = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DonationQrCodeDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shape = AlertDialogDefaults.shape,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.donation_qr),
                contentDescription = stringResource(R.string.make_donation),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .combinedClickable(
                        onClick = onDismiss,
                        onLongClick = {
                            coroutineScope.launch {
                                val saved = saveDonationQrCode(context)
                                Toast.makeText(
                                    context,
                                    if (saved) R.string.donation_qr_saved else R.string.error_image_download,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
            )
        }
    }
}

private suspend fun saveDonationQrCode(context: Context): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        val fileName = "readropsforlumina_donation_qr.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@runCatching false

            resolver.openOutputStream(uri)?.use { output ->
                context.resources.openRawResource(R.drawable.donation_qr).use { input ->
                    input.copyTo(output)
                }
            } ?: return@runCatching false

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } else {
            val target = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                fileName
            )

            context.resources.openRawResource(R.drawable.donation_qr).use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), null, null)
        }

        true
    }.getOrDefault(false)
}
