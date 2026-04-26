package com.readrops.app.more.preferences

import android.content.Context
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.R
import com.readrops.app.lumina.LuminaConfig
import com.readrops.app.util.Preference
import com.readrops.app.util.Preferences
import com.readrops.db.entities.Item
import com.readrops.db.pojo.ItemWithFeed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

typealias PreferenceState<T> = Pair<T, Preference<T>>

class PreferencesScreenModel(
    context: Context,
    preferences: Preferences,
    private val luminaConfig: LuminaConfig,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<PreferencesScreenState>(PreferencesScreenState.Loading) {
    init {
        screenModelScope.launch(dispatcher) {
            with(preferences) {
                val flows = listOf(
                    theme.flow,
                    backgroundSynchronization.flow,
                    scrollRead.flow,
                    hideReadFeeds.flow,
                    openLinksWith.flow,
                    timelineItemSize.flow,
                    mainFilter.flow,
                    synchAtLaunch.flow,
                    useCustomShareIntentTpl.flow,
                    customShareIntentTpl.flow,
                    luminaApiUrl.flow,
                    luminaSkipAiProcessing.flow,
                    swipeToLeft.flow,
                    swipeToRight.flow,
                )

                combine(
                    flows
                ) { list ->
                    PreferencesScreenState.Loaded(
                        themePref = (list[0] as String) to theme,
                        backgroundSyncPref = (list[1] as String) to backgroundSynchronization,
                        scrollReadPref = (list[2] as Boolean) to scrollRead,
                        hideReadFeeds = (list[3] as Boolean) to hideReadFeeds,
                        openLinksWith = (list[4] as String) to openLinksWith,
                        timelineItemSize = (list[5] as String) to timelineItemSize,
                        mainFilterPref = (list[6] as String) to mainFilter,
                        syncAtLaunchPref = (list[7] as Boolean) to synchAtLaunch,
                        useCustomShareIntentTpl = (list[8] as Boolean) to useCustomShareIntentTpl,
                        customShareIntentTpl = (list[9] as String) to customShareIntentTpl,
                        luminaApiUrl = list[10] as String,
                        luminaInternalToken = luminaConfig.getInternalToken(),
                        luminaSkipAiProcessing = list[11] as Boolean,
                        swipeToLeft = (list[12] as String) to swipeToLeft,
                        swipeToRight = (list[13] as String) to swipeToRight,
                        exampleItem = ItemWithFeed(
                            item = Item(
                                title = context.getString(R.string.example_item_title),
                                author = context.getString(R.string.example_item_author),
                                content = context.getString(R.string.example_item_content),
                                link = "https://example.org/feed1"
                            ),
                            feedName = "Example feed",
                            feedId = -1,
                            color = 0,
                            feedIconUrl = "https://example.org/icon.webp",
                            websiteUrl = "https://example.org",
                            folder = null,
                            openIn = null,
                        )
                    )
                }.collect { theme ->
                    mutableState.update { previous ->
                        (previous as? PreferencesScreenState.Loaded)?.let {
                            theme.copy(
                                showDialog = previous.showDialog,
                                showLuminaDialog = previous.showLuminaDialog
                            )
                        } ?: theme
                    }
                }
            }
        }
    }

    fun updateDialog(isVisible: Boolean) {
        if (mutableState.value is PreferencesScreenState.Loaded) {
            mutableState.update {
                (mutableState.value as PreferencesScreenState.Loaded).copy(
                    showDialog = isVisible
                )
            }
        }
    }

    fun updateLuminaDialog(isVisible: Boolean) {
        if (mutableState.value is PreferencesScreenState.Loaded) {
            mutableState.update {
                (mutableState.value as PreferencesScreenState.Loaded).copy(
                    showLuminaDialog = isVisible
                )
            }
        }
    }

    fun saveLuminaSettings(
        apiUrl: String,
        internalToken: String,
        skipAiProcessing: Boolean
    ) {
        screenModelScope.launch {
            luminaConfig.saveSettings(apiUrl, internalToken, skipAiProcessing)
            updateLuminaDialog(false)
        }
    }
}

sealed class PreferencesScreenState {
    data object Loading : PreferencesScreenState()
    data object Error : PreferencesScreenState()

    data class Loaded(
        val themePref: PreferenceState<String>,
        val backgroundSyncPref: PreferenceState<String>,
        val scrollReadPref: PreferenceState<Boolean>,
        val hideReadFeeds: PreferenceState<Boolean>,
        val openLinksWith: PreferenceState<String>,
        val timelineItemSize: PreferenceState<String>,
        val mainFilterPref: PreferenceState<String>,
        val syncAtLaunchPref: PreferenceState<Boolean>,
        val useCustomShareIntentTpl: PreferenceState<Boolean>,
        val customShareIntentTpl: PreferenceState<String>,
        val luminaApiUrl: String,
        val luminaInternalToken: String,
        val luminaSkipAiProcessing: Boolean,
        val swipeToLeft: PreferenceState<String>,
        val swipeToRight: PreferenceState<String>,
        val exampleItem: ItemWithFeed,
        val showDialog: Boolean = false,
        val showLuminaDialog: Boolean = false
    ) : PreferencesScreenState()

}
