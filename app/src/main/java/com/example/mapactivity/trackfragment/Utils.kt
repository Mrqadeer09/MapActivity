/*
 * Copyright (C) 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.mapactivity.trackfragment

import android.app.Activity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.example.mapactivity.DirectionsJSONParser
import com.example.mapactivity.DownloadTask
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar

/**
 * Helper functions to simplify permission checks/requests.
 */
object Utils {
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

    /**
     * Requests permission and if the user denied a previous request, but didn't check
     * "Don't ask again", we provide additional rationale.
     *
     * Note: The Snackbar should have an action to request the permission.
     */
    fun requestPermissionWithRationale(
        permission: String, requestCode: Int, snackbar: Snackbar?, activity: Activity
    ) {
        val provideRationale = shouldShowRequestPermissionRationale(activity, permission)

        if (provideRationale) {
            snackbar?.show()
        } else {
            requestPermissions(activity, arrayOf(permission), requestCode)
        }
    }
    fun drawRoute(
        originLatitude: Double?,
        originLongitude: Double?,
        destinationLatitude: Double?,
        destinationLongitude: Double?


    ) {

        // Getting URL to the Google Directions API
        val originLocation = LatLng(originLatitude!!, originLongitude!!)
        val desLocation = LatLng(destinationLatitude!!, destinationLongitude!!)

        val url: String = DirectionsJSONParser.getDirectionsUrl(originLocation, desLocation)
        val downloadTask = DownloadTask()

        // Start downloading json data from Google Directions API
        downloadTask.execute(url)
    }


    private const val ARG_PERMISSION_REQUEST_TYPE =
        "com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.PERMISSION_REQUEST_TYPE"

    public const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
    public const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param permissionRequestType Type of permission you would like to request.
     * @return A new instance of fragment PermissionRequestFragment.
     */
//    @JvmStatic
//    fun newInstance(permissionRequestType: PermissionRequestType) =
//        PermissionRequestFragment().apply {
//            arguments = Bundle().apply {
//                putSerializable(ARG_PERMISSION_REQUEST_TYPE, permissionRequestType)
//            }
//        }
}