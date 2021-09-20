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
package com.screenrecord.app.utils

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PreferenceChangedLiveData(
    private val sharedPreferences: SharedPreferences,
    private val keys: List<String>
) : MutableLiveData<String>(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onActive() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onInactive() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key in keys) {
            value = key
        }
    }
}

class PreferenceLiveData<T>(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val loadFirst: Boolean = false,
    private val onKeyChanged: (String) -> T
) : MutableLiveData<T>(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onActive() {
        if (loadFirst) {
            value = onKeyChanged(key)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onInactive() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, changed: String) {
        if (key == changed) {
            value = onKeyChanged(key)
        }
    }
}

@ExperimentalCoroutinesApi
fun preferencesChangedFlow(
    sharedPreferences: SharedPreferences,
    keys: List<String>,
    emitOnCreate: Boolean = false
): Flow<String> = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key in keys) {
            offer(key)
        }
    }
    if (emitOnCreate) {
        offer(keys.first())
    }
    sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    awaitClose {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

}

@ExperimentalCoroutinesApi
fun <T> preferenceFlow(
    sharedPreferences: SharedPreferences,
    key: String,
    loadFirst: Boolean = false,
    onKeyChanged: (String) -> T
): Flow<T> = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed ->
        if (key == changed) {
            offer(onKeyChanged(key))
        }
    }
    if (loadFirst) {
        offer(onKeyChanged(key))
    }
    sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    awaitClose {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}

fun <T> PreferenceProvider.preferenceFlow(
    key: String,
    loadFirst: Boolean = false,
    onKeyChanged: (String) -> T
): Flow<T> =
    preferenceFlow(preference, key, loadFirst, onKeyChanged)


fun createPreferenceChangedLiveData(
    sharedPreferences: SharedPreferences,
    keys: List<String>
): LiveData<String> {
    return PreferenceChangedLiveData(sharedPreferences, keys)
}

fun <T> createPreferenceLiveData(
    sharedPreferences: SharedPreferences,
    key: String,
    onChanged: (SharedPreferences, String) -> T
): LiveData<T> {
    return PreferenceLiveData(sharedPreferences, key) {
        onChanged(sharedPreferences, it)
    }
}
