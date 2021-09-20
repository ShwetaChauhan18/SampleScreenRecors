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

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resource helper is specially for [androidx.lifecycle.ViewModel] when we need resource but
 * we don't want to inject [Context] directly.
 */
interface ResourceHelper {
    /**
     * Returns [String] of string [id] from resource.
     */
    fun getString(@StringRes id: Int): String
}

/**
 * Implementation of [ResourceHelper].
 */
@Singleton
class ResourceHelperImpl @Inject constructor(@ApplicationContext private val context: Context) : ResourceHelper {
    override fun getString(@StringRes id: Int): String {
        return context.getString(id)
    }
}
