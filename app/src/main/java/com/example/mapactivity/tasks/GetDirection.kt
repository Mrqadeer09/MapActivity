package com.example.mapactivity.tasks

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import com.example.mapactivity.trackfragment.GeoFragment
import com.example.mapactivity.trackfragment.Utils.decodePolyline
import com.example.mapactivity.models.MapData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

@SuppressLint("StaticFieldLeak")
class GetDirection(val url: String) :
    AsyncTask<Void, Void, List<List<LatLng>>>() {
    override fun doInBackground(vararg params: Void?): List<List<LatLng>> {

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val data = response.body!!.string()

        val result = ArrayList<List<LatLng>>()
        try {
            val respObj = Gson().fromJson(data, MapData::class.java)
            val path = ArrayList<LatLng>()
            for (i in 0 until respObj.routes[0].legs[0].steps.size) {
                path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
            }
            result.add(path)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    override fun onPostExecute(result: List<List<LatLng>>) {
        val lineoption = PolylineOptions()
        for (i in result.indices) {
            lineoption.addAll(result[i])
            lineoption.width(20f)
            lineoption.color(Color.BLUE)
            lineoption.geodesic(true)
        }
        GeoFragment.mMap!!.addPolyline(lineoption).apply {
            isClickable = true
            color = Color.BLUE
        }
    }
}




