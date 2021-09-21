package com.screenrecord.app.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.screenrecord.app.R
import com.screenrecord.app.base.BaseAppCompatActivity
import com.screenrecord.app.databinding.ActivitySampleBinding
import com.screenrecord.app.service.RecorderService
import com.screenrecord.app.service.RecorderState
import com.screenrecord.app.service.UriType
import com.screenrecord.app.ui.recordings.RecordingListActivity
import com.screenrecord.app.utils.ACTION_TOGGLE_RECORDING
import com.screenrecord.app.utils.PreferenceProvider
import com.screenrecord.app.utils.REQUEST_DOCUMENT_TREE
import com.screenrecord.app.utils.SCREEN_RECORD_REQUEST_CODE
import com.screenrecord.app.utils.extension.launchActivity
import com.screenrecord.app.viewmodel.RecordingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SampleActivity : BaseAppCompatActivity<ActivitySampleBinding, RecordingsViewModel>() {

    lateinit var preferenceProvider: PreferenceProvider

    private lateinit var recorderState: LiveData<RecorderState.State>

    private lateinit var getContent: ActivityResultLauncher<Intent>

    private lateinit var getContentScreenRecord: ActivityResultLauncher<Intent>

    private val isRecording: Boolean
        get() = recorderState.value?.run {
            this != RecorderState.State.STOPPED
        } ?: false

    override val viewModel: RecordingsViewModel by viewModels()

    override fun getLayoutResId(): Int = R.layout.activity_sample

    override fun initialize() {
        super.initialize()
        preferenceProvider = PreferenceProvider(this).apply {
            if (ContextCompat.checkSelfPermission(
                    this@SampleActivity,
                    Manifest.permission.RECORD_AUDIO
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (recordAudio) {
                    recordAudio = false
                }
            }
            saveLocation?.let { uri ->
                if (uri.type == UriType.SAF) {
                    contentResolver.takePersistableUriPermission(
                        uri.uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    contentResolver.persistedUriPermissions.filter { it.uri == uri.uri }
                        .apply {
                            if (isEmpty()) {
                                resetSaveLocation()
                            }
                        }
                }
            }
        }

        recorderState = viewModel.recorderState

        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                onActivityResult(REQUEST_DOCUMENT_TREE, result)
            }

        getContentScreenRecord =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                onActivityResult(SCREEN_RECORD_REQUEST_CODE, result)
            }

        // Respond to app shortcut
        intent.action?.let {
            when (it) {
                ACTION_TOGGLE_RECORDING -> if (RecorderState.State.STOPPED == recorderState.value)
                    startRecording()
                else {
                    stopRecording()
                    finish()
                }
            }
        }

        binding.btnRecord.setOnClickListener {
            when {
                isRecording -> stopRecording()
                else -> {
                    if (preferenceProvider.saveLocation == null) {
                        showChooseTreeUri()
                    } else {
                        startRecording()
                    }
                }
            }
        }

        binding.btnList.setOnClickListener {
            launchActivity<RecordingListActivity>()
        }
    }

    private fun stopRecording() {
        binding.btnRecord.background = ContextCompat.getDrawable(this, R.drawable.ic_play)
        RecorderService.stop(this)
    }

    private fun startRecording() {
        // Request Screen recording permission
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        getContentScreenRecord.launch(projectionManager.createScreenCaptureIntent())
        binding.btnRecord.background = ContextCompat.getDrawable(this, R.drawable.ic_pause)
    }

    private fun showChooseTreeUri() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.choose_location_dialog_title)
            .setPositiveButton(R.string.choose_location_action) { _, _ ->
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.putExtra(
                    "android.provider.extra.INITIAL_URI",
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                getContent.launch(intent)
            }
            .create().show()
    }

    private fun onTreeUriResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data!!
            contentResolver.apply {
                takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                persistedUriPermissions.filter { it.uri == uri }.apply {
                    if (isNotEmpty()) {
                        PreferenceProvider(this@SampleActivity).setSaveLocation(uri, UriType.SAF)
                    }
                }
            }
        }
    }

    private fun onActivityResult(requestCode: Int, result: ActivityResult) {
        when (requestCode) {
            SCREEN_RECORD_REQUEST_CODE -> {
                if (result.resultCode == Activity.RESULT_OK) {
                    RecorderService.start(this, result.resultCode, result.data)
                }

                intent.action?.takeIf { it == ACTION_TOGGLE_RECORDING }?.let {
                    finish()
                }
            }
            REQUEST_DOCUMENT_TREE -> {
                if (result.resultCode == Activity.RESULT_OK) {
                    onTreeUriResult(result.resultCode, result.data)
                }
                if (preferenceProvider.saveLocation != null) {
                    startRecording()
                }
            }
        }
    }
}
