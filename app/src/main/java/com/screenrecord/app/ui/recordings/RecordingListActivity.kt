package com.screenrecord.app.ui.recordings

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.screenrecord.app.R
import com.screenrecord.app.base.BaseAppCompatActivity
import com.screenrecord.app.data.Recording
import com.screenrecord.app.databinding.ActivityRecordingBinding
import com.screenrecord.app.ui.adapter.RecordingAdapter
import com.screenrecord.app.viewmodel.RecordingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class RecordingListActivity :
    BaseAppCompatActivity<ActivityRecordingBinding, RecordingsViewModel>() {

    private lateinit var recordingsAdapter: RecordingAdapter

    protected lateinit var selectionTracker: SelectionTracker<Recording>

    override val viewModel: RecordingsViewModel by viewModels()

    override fun getLayoutResId(): Int = R.layout.activity_recording

    override fun initialize() {
        binding.messageNoVideo.visibility = View.GONE
        binding.videosList.apply {
            recordingsAdapter = RecordingAdapter()
            adapter = recordingsAdapter
            selectionTracker = SelectionTracker.Builder(
                "recording-selection-id",
                this,
                RecordingKeyProvider(recordingsAdapter),
                RecordingDetailsLookup(this),
                StorageStrategy.createParcelableStorage(Recording::class.java)
            )
                .withOnItemActivatedListener { item, _ ->
                    onRecordingClick(item.selectionKey!!)
                    return@withOnItemActivatedListener true
                }
                .build()
            recordingsAdapter.selectionTracker = selectionTracker
        }
    }

    override fun initializeObservers(viewModel: RecordingsViewModel) {
        super.initializeObservers(viewModel)
        viewModel.recordings.observe(this, Observer {
            onDataLoaded(it)
        })
    }

    protected open fun onRecordingClick(recording: Recording) {
        val intent = Intent()
        intent.setAction(Intent.ACTION_VIEW)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setDataAndType(
                recording.uri,
                contentResolver.getType(recording.uri)
            )
        startActivity(intent)
    }

    private fun onDataLoaded(data: List<Recording>) {
        binding.messageNoVideo.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
        recordingsAdapter.updateData(data)
    }

}
