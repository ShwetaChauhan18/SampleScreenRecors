/*
 * Copyright (C) 2019 Indrit Bashkimi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.screenrecord.app.viewmodel

import androidx.lifecycle.LiveData
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
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn

@HiltViewModel
class RecordingsViewModel @Inject constructor(
) : BaseViewModel() {

    private var dataManager: DataManager? = null

    val recorderState = RecorderState.state

    val recordings: LiveData<List<Recording>>

    init {
        val preferences = PreferenceProvider()
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