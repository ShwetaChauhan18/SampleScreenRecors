package com.screenrecord.app.data

import android.content.ContentValues
import android.net.Uri
import kotlinx.coroutines.flow.Flow

class DataManager(private val source: DataSource) {

    fun create(folderUri: Uri, name: String, mimeType: String, extra: ContentValues?): Uri? {
        return source.create(folderUri, name, mimeType, extra)
    }

    fun delete(recording: Recording) {
        delete(recording.uri)
    }

    fun delete(uri: Uri) {
        source.delete(uri)
    }

    fun delete(uris: List<Uri>) {
        source.delete(uris)
    }

    fun rename(recording: Recording, newName: String) {
        rename(recording.uri, newName)
    }

    fun rename(uri: Uri, newName: String) {
        source.rename(uri, newName)
    }

    fun recordings(): Flow<List<Recording>> {
        return source.recordings()
    }

    fun update(uri: Uri, values: ContentValues) {
        source.update(uri, values)
    }
}
