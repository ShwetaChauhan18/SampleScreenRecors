package com.screenrecord.app.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.screenrecord.app.base.BaseViewModel
import com.screenrecord.app.data.DataManager
import com.screenrecord.app.data.MediaStoreDataSource
import com.screenrecord.app.data.Recording
import com.screenrecord.app.data.SAFDataSource
import com.screenrecord.app.service.RecorderState
import com.screenrecord.app.service.SaveUri
import com.screenrecord.app.service.UriType
import com.screenrecord.app.utils.PreferenceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn

@HiltViewModel
class RecordingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private var dataManager: DataManager? = null

    val recorderState = RecorderState.state

    val recordings: LiveData<List<Recording>>

    init {
        val preferences = PreferenceProvider(context)
        recordings = preferences.saveLocationFlow.flatMapLatest {
            dataManager = createDataManager(it)
            dataManager?.recordings() ?: emptyFlow()
        }.flowOn(Dispatchers.IO).combine(preferences.sortOrderOptionsFlow) { recordings, options ->
            processData(recordings, options)
        }.flowOn(Dispatchers.Default).asLiveData()
    }

    private fun createDataManager(saveUri: SaveUri?) = when (saveUri?.type) {
        UriType.MEDIA_STORE -> DataManager(MediaStoreDataSource(context, saveUri.uri))
        UriType.SAF -> DataManager(SAFDataSource(context, saveUri.uri))
        else -> null
    }

    fun rename(recording: Recording, newName: String) {
        dataManager?.rename(recording, newName)
    }

    fun deleteRecording(recording: Recording) {
        dataManager?.delete(recording)
    }

    fun deleteRecordings(recordings: List<Recording>) {
        dataManager?.delete(recordings.map { it.uri })
    }

    private fun processData(
        recordings: List<Recording>,
        options: PreferenceProvider.SortOrderOptions
    ): List<Recording> {
        return recordings.filter { !it.isPending }.run {
            when (options.sortBy) {
                PreferenceProvider.SortBy.NAME -> sortedBy { it.title }
                PreferenceProvider.SortBy.DATE -> sortedBy { it.modified }
                PreferenceProvider.SortBy.DURATION -> sortedBy { it.duration }
                PreferenceProvider.SortBy.SIZE -> sortedBy { it.size }
            }.run {
                when (options.orderBy) {
                    PreferenceProvider.OrderBy.ASCENDING -> this
                    PreferenceProvider.OrderBy.DESCENDING -> reversed()
                }
            }
        }
    }
}