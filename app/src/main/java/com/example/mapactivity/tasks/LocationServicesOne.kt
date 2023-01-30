package com.example.mapactivity.tasks

import android.Manifest
import android.app.Service
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices


open class LocationServicesOne : Service(), LocationListener {
    protected var locationManager: LocationManager? = null
    var checkGPS = false
    var checkNetwork = false

    // boolean canGetLocation = false;
    var loc: Location? = null

    //    double latitude;
//    double longitude;
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {
         Toast.makeText(applicationContext, ""+location.latitude + location.longitude, Toast.LENGTH_LONG).show();
    }


    override fun onCreate() {
        super.onCreate()
        location
    }

    private val location: Location?
        private get() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
            }
            locationManager = applicationContext
                .getSystemService(LOCATION_SERVICE) as LocationManager
            checkGPS = locationManager!!
                .isProviderEnabled(LocationManager.GPS_PROVIDER)
            checkNetwork = locationManager!!
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
           /* locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
            )*/
            if (locationManager != null) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        Toast.makeText(
                            applicationContext,
                            location.latitude.toString() + "" +location.longitude + "from method",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            Toast.makeText(
                applicationContext, ""+0.0 + 0.0 + "from method",
                Toast.LENGTH_LONG
            ).show();
            return loc
        }

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 100
        private const val MIN_TIME_BW_UPDATES: Long = 30
    }
}


