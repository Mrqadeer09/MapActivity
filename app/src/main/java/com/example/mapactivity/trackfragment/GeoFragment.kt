package com.example.mapactivity.trackfragment

import android.Manifest

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mapactivity.R
import com.example.mapactivity.databinding.FragmentGeoBinding
import com.example.mapactivity.models.PermissionRequestType
import com.example.mapactivity.tasks.GetDirection
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.libraries.places.api.Places
import com.google.android.material.snackbar.Snackbar
import java.text.MessageFormat


class GeoFragment : Fragment(), OnMapReadyCallback {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentGeoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    //main activity
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


    private fun hasPermission(permission: String): Boolean {

        // Background permissions didn't exit prior to Q, so it's approved by default.
        if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true
        }

        return ActivityCompat.checkSelfPermission(
            requireContext(), permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    val markerPoints: ArrayList<String> = ArrayList();

    companion object {
        var mMap: GoogleMap? = null

        var mPolyline: Polyline? = null
    }

    private var permissionRequestType: PermissionRequestType? = null
    private lateinit var binding: FragmentGeoBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var locationRequest: LocationRequest? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
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

                            binding.tvLoc.setText(
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
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener {
                Toast.makeText(
                    requireActivity(),
                    ":latitude${it.latitude} longitude${it.longitude}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("TAG", "onCreate:latitude${it.latitude}longitude${it.longitude} ")
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest!!, locationCallback, Looper.getMainLooper()
            )


        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
//        val mapFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)


        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo =
            requireActivity().applicationContext.packageManager.getApplicationInfo(
                requireActivity().applicationContext.packageName, PackageManager.GET_META_DATA
            )
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().applicationContext, apiKey)
        }



        binding.directions.setOnClickListener {
            Toast.makeText(requireActivity(), "message$mapFragment", Toast.LENGTH_SHORT).show()
            mapFragment?.getMapAsync {
                mMap = it
                val originLocation = LatLng(originLatitude, originLongitude)
                mMap!!.addMarker(MarkerOptions().position(originLocation))
                val destinationLocation = LatLng(destinationLatitude, destinationLongitude)
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


    }

    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
            binding.root, R.string.fine_location_permission_rationale, Snackbar.LENGTH_LONG
        ).setAction(R.string.ok) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Utils.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
            binding.root, R.string.background_location_permission_rationale, Snackbar.LENGTH_LONG
        ).setAction(R.string.ok) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                Utils.REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestFineLocationPermission() {
        val permissionApproved = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ?: return

        if (permissionApproved) {
            activityListener?.displayLocationUI()
        } else {
            Utils.requestPermissionWithRationale(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Utils.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                fineLocationRationalSnackbar,
                requireActivity()
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
            Utils.requestPermissionWithRationale(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Utils.REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                backgroundRationalSnackbar,
                requireActivity()
            )
        }
    }


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
                Utils.drawRoute(
                    originLatitude2, originLongitude2, destinationLatitude2, destinationLongitude2
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
                requireActivity().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 111
            )
        }
    }

    var locationPermissionGranted = false

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
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
                                    originLatitude, originLongitude
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

    fun showRoute(view: View) {}
    private fun getDirectionURL(origin: LatLng, dest: LatLng, secret: String): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" + "&destination=${dest.latitude},${dest.longitude}" + "&sensor=false" + "&mode=driving" + "&key=$secret"
    }


}