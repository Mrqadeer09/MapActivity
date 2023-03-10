package com.example.mapactivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
/*import com.example.mapactivity.DirectionsJSONParser.downloadUrl
import com.example.mapactivity.DirectionsJSONParser.getDirectionsUrl*/
import com.example.mapactivity.trackfragment.Utils.REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
import com.example.mapactivity.trackfragment.Utils.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
import com.example.mapactivity.trackfragment.Utils.drawRoute
import com.example.mapactivity.trackfragment.Utils.requestPermissionWithRationale
import com.example.mapactivity.databinding.ActivityMapsBinding
import com.example.mapactivity.models.*
import com.example.mapactivity.tasks.GetDirection
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.material.snackbar.Snackbar
import java.text.MessageFormat




@RequiresApi(Build.VERSION_CODES.M)
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    var lastKnownLocation = null
    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private val TAG = "LocationActivity"
    private val INTERVAL = (1000 * 10).toLong()
    private val FASTEST_INTERVAL = (1000 * 5).toLong()
    var btnFusedLocation: Button? = null
    var tvLocation: TextView? = null
    var mLocationRequest: LocationRequest? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mCurrentLocation: android.location.Location? = null
    var mLastUpdateTime: String? = null

    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = INTERVAL
        mLocationRequest?.fastestInterval = FASTEST_INTERVAL
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    fun hasPermission(permission: String): Boolean {

        // Background permissions didn't exit prior to Q, so it's approved by default.
        if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true
        }

        return ActivityCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    val markerPoints: ArrayList<String> = ArrayList();

    companion object {
        var mMap: GoogleMap? = null

        var mPolyline: Polyline? = null
    }

    private var permissionRequestType: PermissionRequestType? = null
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var locationRequest: LocationRequest? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestFineLocationPermission()
            requestBackgroundLocationPermission()


            return
        } else {


            locationRequest = LocationRequest.create()
            locationRequest?.interval = 100
            locationRequest?.fastestInterval = 50
            locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val locationCallback: LocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult != null) {
                        //Showing the latitude, longitude and accuracy on the home screen.
                        for (location in locationResult.locations) {
                            originLatitude2 = location.latitude
                            originLongitude2 = location.longitude

                            val desLocation = LatLng(originLatitude2, originLongitude2)

                            mMap?.addMarker(
                                MarkerOptions().position(desLocation)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.download))
                            )

                            findViewById<TextView>(R.id.tvLoc).setText(
                                MessageFormat.format(
                                    "Lat: {0} Long: {1} Accuracy: {2}",
                                    location.latitude,
                                    location.longitude,
                                    location.accuracy
                                )
                            )
                        }
                    }
                }
            }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener {
                Toast.makeText(
                    this, ":latitude${it.latitude} longitude${it.longitude}", Toast.LENGTH_SHORT
                ).show()
                Log.d("TAG", "onCreate:latitude${it.latitude}longitude${it.longitude} ")
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest!!, locationCallback, Looper.getMainLooper()
            );


        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo = applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName,
            PackageManager.GET_META_DATA
        )
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }


        val gd = findViewById<Button>(R.id.directions)
        gd.setOnClickListener {
            mapFragment.getMapAsync {
                mMap = it
                val originLocation = LatLng(originLatitude2, originLongitude2)
                mMap!!.addMarker(MarkerOptions().position(originLocation))
                val destinationLocation = LatLng(destinationLatitude2, destinationLongitude2)
                mMap!!.addMarker(MarkerOptions().position(destinationLocation))
                val urll = getDirectionURL(originLocation, destinationLocation, apiKey)
                GetDirection(urll).execute()
                mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 12F))
            }
        }


        //

        when (permissionRequestType) {
            PermissionRequestType.FINE_LOCATION -> {

                binding.apply {
//                    iconImageView.setImageResource(R.drawable.ic_location_on_24px)
//                    titleTextView.text = getString(R.string.fine_location_access_rationale_title_text)
//                    detailsTextView.text = getString(R.string.fine_location_access_rationale_details_text)
//                    permissionRequestButton.text = getString(R.string.enable_fine_location_button_text)
                }
            }

            PermissionRequestType.BACKGROUND_LOCATION -> {

                binding.apply {
//                    iconImageView.setImageResource(R.drawable.ic_my_location_24px)
//                    titleTextView.text = getString(R.string.background_location_access_rationale_title_text)
//                    detailsTextView.text = getString(R.string.background_location_access_rationale_details_text)
//                    permissionRequestButton.text = getString(R.string.enable_background_location_button_text)
                }
            }
            else -> {}
        }

        /* binding.permissionRequestButton.setOnClickListener {
             when (permissionRequestType) {
                 PermissionRequestType.FINE_LOCATION ->
                     requestFineLocationPermission()

                 PermissionRequestType.BACKGROUND_LOCATION ->
                     requestBackgroundLocationPermission()
                 else -> {}
             }
         }*/

        //


    }

    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
            binding.directions, R.string.fine_location_permission_rationale, Snackbar.LENGTH_LONG
        ).setAction(R.string.ok) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
            binding.directions,
            R.string.background_location_permission_rationale,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.ok) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestFineLocationPermission() {
        val permissionApproved = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ?: return

        if (permissionApproved) {
            activityListener?.displayLocationUI()
        } else {
            requestPermissionWithRationale(
                Manifest.permission.ACCESS_FINE_LOCATION,
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                fineLocationRationalSnackbar,
                this
            )
        }
    }

    private var activityListener: Callbacks? = null

    interface Callbacks {
        fun displayLocationUI()
    }

    private fun requestBackgroundLocationPermission() {
        val permissionApproved = hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        if (permissionApproved) {
            activityListener?.displayLocationUI()
        } else {
            requestPermissionWithRationale(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                backgroundRationalSnackbar,
                this
            )
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


// GeeksforGeeks coordinates
    private var originLatitude: Double = 28.5021359
    private var originLongitude: Double = 77.4054901
    private var originLatitude2: Double = 24.9012
    private var originLongitude2: Double = 67.1155

    // Coordinates of a park nearby
    private var destinationLatitude: Double = 28.5151087
    private var destinationLongitude: Double = 77.3932163
    private var destinationLatitude2: Double = 24.896111
    private var destinationLongitude2: Double = 67.081389
    private var mDestination: LatLng? = null
    private var mOrigin: LatLng? = null
    var mMarkerPoints: ArrayList<LatLng> = ArrayList()

    override fun onMapReady(p0: GoogleMap) {
        /*     mMap = p0!!
             val originLocation = LatLng(originLatitude, originLongitude)
             mMap!!.clear()
             mMap!!.addMarker(MarkerOptions().position(originLocation))
             mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 18F))
     */

        mMap = p0!!
        mMap!!.setOnMapClickListener { point -> // Already two locations
            if (mMarkerPoints.size > 1) {
                mMarkerPoints.clear()
                mMap!!.clear()
            }

            // Adding new item to the ArrayList
            mMarkerPoints.add(point)

            // Creating MarkerOptions
            val options = MarkerOptions()

            // Setting the position of the marker
            options.position(point)

            if (mMarkerPoints.size == 1) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            } else if (mMarkerPoints.size == 2) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }

            // Add new marker to the Google Map Android API V2
            mMap!!.addMarker(options)

            // Checks, whether start and end locations are captured
            if (mMarkerPoints.size >= 2) {
                mOrigin = mMarkerPoints.get(0)
                mDestination = mMarkerPoints.get(1)
                drawRoute(
                    originLatitude,
                    originLongitude,
                    destinationLatitude,
                    destinationLongitude
                )
            }
        }
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 111
            )
        }
    }

    var locationPermissionGranted = false

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        val lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude, lastKnownLocation!!.longitude
                                    ), 1.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    originLatitude,
                                    originLongitude
                                ), 1.toFloat()
                            )
                        )
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getDirectionURL(origin: LatLng, dest: LatLng, secret: String): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" + "&destination=${dest.latitude},${dest.longitude}" + "&sensor=false" + "&mode=driving" + "&key=$secret"
    }




/*    private fun drawRoute() {

        // Getting URL to the Google Directions API
        val originLocation = LatLng(originLatitude, originLongitude)
        val desLocation = LatLng(destinationLatitude, destinationLongitude)

        val url: String = getDirectionsUrl(mOrigin!!, mDestination!!)
        val downloadTask = DownloadTask()

        // Start downloading json data from Google Directions API
        downloadTask.execute(url)
    }*/

}




