package com.readrops.app.lumina

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.readrops.app.R
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LuminaExternalShareConfirmActivity : ComponentActivity(), KoinComponent {

    private val externalShareHandler: LuminaExternalShareHandler by inject()
    private var confirmed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        if (url.isBlank()) {
            Toast.makeText(this, R.string.lumina_no_link_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.sync_to_lumina)
            .setMessage(getString(R.string.lumina_confirm_sync_url, url))
            .setNegativeButton(R.string.back) { _, _ ->
                finish()
            }
            .setPositiveButton(R.string.sync_to_lumina) { _, _ ->
                confirmed = true
                lifecycleScope.launch {
                    externalShareHandler.syncConfirmedUrl(
                        this@LuminaExternalShareConfirmActivity,
                        url
                    )
                    finish()
                }
            }
            .create()
            .apply {
                setOnDismissListener {
                    if (!confirmed) {
                        finish()
                    }
                }
                show()
            }
    }

    companion object {
        const val EXTRA_URL = "lumina_url"
    }
}
