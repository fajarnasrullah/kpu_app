package com.jer.kpuapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.jer.kpuapp.R
import com.jer.kpuapp.databinding.ActivityMapsBinding
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    private var mMap: GoogleMap? = null
    private lateinit var mapView: MapView

    //    private lateinit var googleMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    private val DEFAULT_ZOOM = 15f
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var addressText: String
    private lateinit var sharedPreferences: SharedPreferences



    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Izin lokasi diperlukan untuk menggunakan fitur ini", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)

        mapView = findViewById(R.id.mapView)

        addressText = ""


        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        binding.btnPick.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putString("alamat", addressText)
            if (addressText != "" ) {
                editor.apply()
                finish()
            } else {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)


        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)

    }

    override fun onMapReady(map: GoogleMap) {


        mapView.onResume()
        mMap = map

        checkLocationPermissionsAndProceed()


//        mMap!!.setMyLocationEnabled(true)
        mMap!!.setOnCameraMoveListener(this)
        mMap!!.setOnCameraMoveStartedListener(this)
        mMap!!.setOnCameraIdleListener(this)

    }






    private fun checkLocationPermissionsAndProceed() {
        val locationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        when {
            locationPermissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED } -> {
                getCurrentLocation()
            }

            locationPermissions.any { shouldShowRequestPermissionRationale(it) } -> {
                AlertDialog.Builder(this)
                    .setTitle("Izin Lokasi Diperlukan")
                    .setMessage("Aplikasi ini memerlukan akses lokasi untuk menampilkan posisi Anda pada peta.")
                    .setPositiveButton("OK") { _, _ ->
                        requestLocationPermissionsLauncher.launch(locationPermissions)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }

            else -> {
                // Minta kedua izin secara langsung
                requestLocationPermissionsLauncher.launch(locationPermissions)
            }
        }
    }



    private fun getCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MapsActivity)

        try {
            @SuppressLint("MissingPermission")
            val location = fusedLocationProviderClient!!.lastLocation

            location.addOnCompleteListener(object: OnCompleteListener<Location>{
                override fun onComplete(p0: Task<Location>) {
                    if (p0.isSuccessful) {
                        val currentLocation = p0.result as Location?
                        if (currentLocation != null) {
                            moveCamera(currentLocation.latitude, currentLocation.longitude,
                            DEFAULT_ZOOM)
                        }
                    } else {

                        checkLocationPermissionsAndProceed()
                    }
                }

            })
        } catch (se: SecurityException) {
            Log.e("TAG","SecurityException")
        }
    }

    private fun moveCamera(lat: Double, lng: Double, zoom: Float) {
//        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        val latLng = com.google.android.gms.maps.model.LatLng(lat, lng)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun setAddress(address: Address) {
        if (address != null) {
            if(address.getAddressLine(0) != null) {
                binding.tvAddress.setText(address.getAddressLine(0))
                addressText = address.getAddressLine(0)
            }
            if (address.getAddressLine(1) != null) {
                binding.tvAddress.getText().toString() + address.getAddressLine(1)
                addressText = address.getAddressLine(1)
            }
        }
    }




    override fun onLocationChanged(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        setAddress(addresses!![0])

    }




    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onCameraMove() {

    }

    override fun onCameraMoveStarted(p0: Int) {

    }

    override fun onCameraIdle() {
        var addresses: List<Address>? = null
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(mMap!!.cameraPosition.target.latitude,
                mMap!!.cameraPosition.target.longitude, 1)
            setAddress(addresses!![0])
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}