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
import com.screenrecord.app.data.DataManager
import com.screenrecord.app.utils.PreferenceProvider

fun PreferenceProvider.generateOptions(dataManager: DataManager): Options? {
    val saveUri = saveLocation ?: return null
    val folderUri = saveUri.uri

    val uri = dataManager
        .create(folderUri, filename, "video/mp4", null) ?: return null

    return Options(
        video = VideoOptions(
            resolution = resolution.run {
                Resolution(first, second)
            },
            bitrate = videoBitrate,
            encoder = videoEncoder,
            fps = fps,
            virtualDisplayDpi = displayMetrics.densityDpi
        ),
        audio = if (recordAudio) {
            AudioOptions.RecordAudio(
                source = MediaRecorder.AudioSource.MIC,
                samplingRate = audioSamplingRate,
                encoder = audioEncoder,
                bitRate = audioBitrate
            )
        } else AudioOptions.NoAudio,
        output = OutputOptions(
            uri = SaveUri(uri, saveUri.type)
        )
    )
}
