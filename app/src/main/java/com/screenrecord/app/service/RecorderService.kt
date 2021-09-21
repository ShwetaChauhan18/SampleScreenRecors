/*
* Copyright 2021 ScreenRecord
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.screenrecord.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.screenrecord.app.R
import com.screenrecord.app.data.DataManager
import com.screenrecord.app.data.MediaStoreDataSource
import com.screenrecord.app.data.SAFDataSource
import com.screenrecord.app.utils.PreferenceProvider

class RecorderService : Service() {

    private var session: RecordingSession? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return when (intent.action) {
            ACTION_RECORDING_START -> onActionRecordingStart(intent)
            ACTION_RECORDING_PAUSE -> onActionRecordingPause()
            ACTION_RECORDING_RESUME -> onActionRecordingResume()
            ACTION_RECORDING_STOP -> onActionRecordingStop()
            ACTION_RECORDING_DELETE -> onActionRecordingDelete(intent)
            else -> START_NOT_STICKY
        }
    }

    private fun onActionRecordingStart(intent: Intent): Int {
        return when (session?.state) {
            RecorderState.State.RECORDING -> START_STICKY
            RecorderState.State.PAUSED -> {
                resume(this)
                START_STICKY
            }
            RecorderState.State.STOPPED, null -> {
                return (createNewRecordingSession()?.let {
                    if (it.start(intent)) {
                        session = it
                        START_STICKY
                    } else {
                        START_NOT_STICKY
                    }
                } ?: START_NOT_STICKY).also {
                    if (it == START_NOT_STICKY) stopForeground(true)
                }
            }
        }
    }

    private fun onActionRecordingPause(): Int = session?.let {
        when (it.state) {
            RecorderState.State.RECORDING -> {
                it.pause()
                Toast.makeText(
                    this,
                    R.string.recording_paused_message,
                    Toast.LENGTH_SHORT
                ).show()
                START_STICKY
            }
            RecorderState.State.PAUSED -> return START_STICKY
            RecorderState.State.STOPPED -> START_NOT_STICKY
        }
        START_STICKY
    } ?: START_NOT_STICKY

    private fun onActionRecordingResume(): Int = session?.let {
        when (it.state) {
            RecorderState.State.PAUSED -> {
                it.resume()
                START_STICKY
            }
            RecorderState.State.RECORDING -> START_STICKY
            RecorderState.State.STOPPED -> START_NOT_STICKY
        }
    } ?: START_NOT_STICKY

    private fun onActionRecordingStop(): Int {
        session?.let {
            when (it.state) {
                RecorderState.State.RECORDING, RecorderState.State.PAUSED -> {
                    if (it.stop()) {
                        Log.d(TAG, "Recording finished.")
                        onRecordingCompleted()
                        session = null
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.recording_error_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                RecorderState.State.STOPPED -> {
                }
            }
        }

        stopForeground(true)
        stopSelf()
        return START_NOT_STICKY
    }

    private fun onActionRecordingDelete(intent: Intent): Int {
        intent.getParcelableExtra<SaveUri>(EXTRA_RECORDING_DELETE_URI)?.also {
            createNewDataManager(it).delete(it.uri)
            onRecordingDeleted()
        }
        return if (session?.state == RecorderState.State.STOPPED) START_NOT_STICKY else START_STICKY
    }


    private fun createNewDataManager(saveUri: SaveUri): DataManager {
        return DataManager(
            (when (saveUri.type) {
                UriType.SAF -> SAFDataSource(this, saveUri.uri)
                else -> MediaStoreDataSource(this, saveUri.uri)
            })
        )
    }

    private fun createNewRecordingSession(): RecordingSession? {
        val preferences = PreferenceProvider(this)
        val saveLocation = preferences.saveLocation ?: return null
        val dataManager = createNewDataManager(saveLocation)
        val options = preferences.generateOptions(dataManager)
        if (options == null) {
            Toast.makeText(
                this,
                R.string.recording_error_message,
                Toast.LENGTH_SHORT
            ).show()
            return null
        }
        return RecordingSession(this, options, dataManager)
    }

    private fun onRecordingCompleted() = broadcast(ACTION_RECORDING_COMPLETED)

    private fun onRecordingDeleted() = broadcast(ACTION_RECORDING_DELETE)

    private fun broadcast(action: String) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(action))
    }

    companion object {
        private val TAG = RecorderService::class.java.simpleName

        const val ACTION_RECORDING_START = "com.screenrecord.app.action.RECORDING_START"
        const val ACTION_RECORDING_STOP = "com.screenrecord.app.action.RECORDING_STOP"
        const val ACTION_RECORDING_PAUSE = "com.screenrecord.app.action.RECORDING_PAUSE"
        const val ACTION_RECORDING_RESUME = "com.screenrecord.app.action.RECORDING_RESUME"

        const val ACTION_RECORDING_DELETE = "con.screenrecord.app.action.RECORDING_DELETE"
        const val EXTRA_RECORDING_DELETE_URI = "arg_delete_uri"

        const val ACTION_RECORDING_COMPLETED =
            "com.screenrecord.app.action.RECORDING_COMPLETED"
        const val ACTION_RECORDING_DELETED = "com.screenrecord.app.action.RECORDING_DELETED"

        const val RECORDER_INTENT_DATA = "recorder_intent_data"
        const val RECORDER_INTENT_RESULT = "recorder_intent_result"

        fun start(context: Context, resultCode: Int, data: Intent?) {
            Intent(context, RecorderService::class.java).apply {
                action = ACTION_RECORDING_START
                putExtra(RECORDER_INTENT_DATA, data)
                putExtra(RECORDER_INTENT_RESULT, resultCode)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                context.startService(this)
            }
        }

        @Suppress("unused")
        fun pause(context: Context) {
            Intent(context, RecorderService::class.java).apply {
                action = ACTION_RECORDING_PAUSE
                context.startService(this)
            }
        }

        @Suppress("unused")
        fun resume(context: Context) {
            Intent(context, RecorderService::class.java).apply {
                action = ACTION_RECORDING_RESUME
                context.startService(this)
            }
        }

        fun stop(context: Context) {
            Intent(context, RecorderService::class.java).apply {
                action = ACTION_RECORDING_STOP
                context.startService(this)
            }
        }
    }
}
