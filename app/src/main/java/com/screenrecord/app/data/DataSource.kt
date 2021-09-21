package com.screenrecord.app.data

import android.content.ContentValues
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface DataSource {

    fun create(folderUri: Uri, name: String, mimeType: String, extra: ContentValues?): Uri?

    fun delete(uri: Uri)

    fun delete(uris: List<Uri>)

    fun recordings(): Flow<List<Recording>>

    fun rename(uri: Uri, newName: String)

    fun update(uri: Uri, values: ContentValues)
}
