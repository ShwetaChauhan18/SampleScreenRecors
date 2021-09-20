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

import android.media.MediaRecorder
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Options(
    val video: VideoOptions,
    val audio: AudioOptions,
    val output: OutputOptions
)

data class VideoOptions(
    val resolution: Resolution,
    val encoder: Int = MediaRecorder.VideoEncoder.H264,
    val fps: Int = 30,
    val bitrate: Int = 7130317,
    val virtualDisplayDpi: Int
)

data class Resolution(val width: Int, val height: Int)

sealed class AudioOptions {
    object NoAudio : AudioOptions()
    data class RecordAudio(
        val source: Int = MediaRecorder.AudioSource.DEFAULT,
        val samplingRate: Int = 44100,
        val encoder: Int = MediaRecorder.AudioEncoder.AAC,
        val bitRate: Int = 128000
    ) : AudioOptions()
}

class OutputOptions(val uri: SaveUri, val format: Int = MediaRecorder.OutputFormat.DEFAULT)

@Parcelize
data class SaveUri(val uri: Uri, val type: UriType) : Parcelable

@Parcelize
enum class UriType : Parcelable {
    MEDIA_STORE, SAF
}
