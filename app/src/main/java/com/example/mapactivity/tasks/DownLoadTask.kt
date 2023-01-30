package com.example.mapactivity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject

/**
 * A class to download data from Google Directions URL
 */
@SuppressLint("StaticFieldLeak")
class DownloadTask : AsyncTask<String?, Void?, String>() {
    // Downloading data in non-ui thread
    override fun doInBackground(vararg url: String?): String {

        // For storing data from web service
        var data = ""
        try {
            // Fetching the data from web service
            data = DirectionsJSONParser.downloadUrl(url[0])
            Log.d("DownloadTask", "DownloadTask : $data")
        } catch (e: java.lang.Exception) {
            Log.d("Background Task", e.toString())
        }
        return data
    }

    // Executes in UI thread, after the execution of
    // doInBackground()
    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        val parserTask = ParserTask()

        // Invokes the thread for parsing the JSON data
        parserTask.execute(result)
    }


}

private class ParserTask : AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>() {
    // Parsing the data in non-ui thread
    override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>>? {
        val jObject: JSONObject
        var routes: List<List<HashMap<String, String>>>? = null
        try {
            jObject = JSONObject(jsonData[0])
            val parser = DirectionsJSONParser()

            // Starts parsing data
            routes = parser.parse(jObject)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return routes
    }

    // Executes in UI thread, after the parsing process
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
        var points: java.util.ArrayList<LatLng?>? = null
        var lineOptions: PolylineOptions? = null

        // Traversing through all the routes
        for (i in result!!.indices) {
            points = java.util.ArrayList()
            lineOptions = PolylineOptions()

            // Fetching i-th route
            val path = result[i]

            // Fetching all the points in i-th route
            for (j in path.indices) {
                val point = path[j]
                val lat = point["lat"]!!.toDouble()
                val lng = point["lng"]!!.toDouble()
                val position = LatLng(lat, lng)
                points.add(position)
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points)
            lineOptions.width(8f)
            lineOptions.color(Color.RED)
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            if (MapsActivity.mPolyline != null) {
                MapsActivity.mPolyline!!.remove()
            }
            MapsActivity.mPolyline = MapsActivity.mMap!!.addPolyline(lineOptions)
        } else {
            Log.d("TAG", "onPostExecute:No route is found ")
            //                Toast.makeText(getApplicationContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }


}