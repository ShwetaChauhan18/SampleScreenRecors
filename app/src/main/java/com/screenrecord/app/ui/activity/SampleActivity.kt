package com.screenrecord.app.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.screenrecord.app.R
import com.screenrecord.app.base.BaseAppCompatActivity
import com.screenrecord.app.databinding.ActivitySampleBinding
import com.screenrecord.app.service.RecorderState
import com.screenrecord.app.service.UriType
import com.screenrecord.app.utils.PreferenceProvider
import com.screenrecord.app.utils.SCREEN_RECORD_REQUEST_CODE
import com.screenrecord.app.viewmodel.SampleViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SampleActivity : BaseAppCompatActivity<ActivitySampleBinding, SampleViewModel>() {

    @Inject
    lateinit var preferenceProvider: PreferenceProvider

    private lateinit var recorderState: LiveData<RecorderState.State>

    private val isRecording: Boolean
        get() = recorderState.value?.run {
            this != RecorderState.State.STOPPED
        } ?: false

    override val viewModel: SampleViewModel by viewModels()

    override fun getLayoutResId(): Int = R.layout.activity_sample

    /*private val userAdapter = UserAdapter()
    private val paginationListener = RecyclerPaginationListener {
        viewModel.loadMoreUsers()
    }*/

    override fun initialize() {
        super.initialize()
        preferenceProvider.apply {
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
    }

    override fun initializeObservers(viewModel: SampleViewModel) {
        super.initializeObservers(viewModel)

        /*viewModel.onNewUserList.observeEvent(this) {
            userAdapter.addAllItem(ArrayList(it))
        }*/
    }

    private fun startRecording() {
        // Request Screen recording permission
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            projectionManager.createScreenCaptureIntent(),
            SCREEN_RECORD_REQUEST_CODE
        )
    }
}
