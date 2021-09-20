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
package com.screenrecord.app

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import com.screenrecord.app.utils.NetworkUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TestScreenRecordApp : Application() {

    override fun onCreate() {
        super.onCreate()

        observeNetwork()
    }

    /**
     * Observe network state.
     */
    private fun observeNetwork() {
        val cm: ConnectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder().build()

        cm.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    NetworkUtil.isNetworkConnected = true
                }

                override fun onLost(network: Network) {
                    NetworkUtil.isNetworkConnected = false
                }
            })
    }
}
