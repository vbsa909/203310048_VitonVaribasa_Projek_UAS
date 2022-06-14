package com.example.a203310048_vitonvaribasa_projek_uas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.a203310048_vitonvaribasa_projek_uas.databinding.ActivityMainBinding
import com.example.a203310048_vitonvaribasa_projek_uas.model.InternalFileRepository
import com.example.a203310048_vitonvaribasa_projek_uas.model.NoteRepository
import com.example.a203310048_vitonvaribasa_projek_uas.model.Note
import java.util.*

class MainActivity : AppCompatActivity() {
    private val repo: NoteRepository by lazy { InternalFileRepository(this) }

    private lateinit var mainBinding: ActivityMainBinding

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mainBinding.cari.setOnClickListener {
            getLocation()
        }
    mainBinding.Share.setOnClickListener {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            val logData1 = mainBinding.Catatan.text.toString()
            val logData2 = mainBinding.FileName.text.toString()


            putExtra(Intent.EXTRA_TEXT, "Nama :  $logData1 :\n $logData2 :\n ")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)

    }
    mainBinding.scan.setOnClickListener {
        var logDataSensor =
            mainBinding.Catatan.text.toString()
            mainBinding.FileName.text.toString()

        val logData1 = mainBinding.tvLatitude.text.toString()
        val logData2 = mainBinding.tvLongitude.text.toString()
        val logData3 = mainBinding.tvCountryName.text.toString()
        val logData4 = mainBinding.tvLocality.text.toString()
        val logData5 = mainBinding.tvAddress.text.toString()



        logDataSensor = ">$logData1\n >$logData2\n >$logData3\n >$logData4\n >$logData5\n $logDataSensor"
        mainBinding.Catatan.setText(logDataSensor)
    }

    mainBinding.tulis.setOnClickListener {
        if (mainBinding.FileName.text.isNotEmpty()) {
            try {
                repo.addNote(
                    Note(
                        mainBinding.FileName.text.toString(),
                        mainBinding.Catatan.text.toString()
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(this, "File Write Failed", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            mainBinding.FileName.text.clear()
            mainBinding.Catatan.text.clear()
        } else {
            Toast.makeText(this, "Please provide a Filename", Toast.LENGTH_LONG).show()
        }
    }
    mainBinding.baca.setOnClickListener {
        if (mainBinding.FileName.text.isNotEmpty()) {
            try {
                val note = repo.getNote(mainBinding.FileName.text.toString())
                mainBinding.Catatan.setText(note.noteText)
            } catch (e: Exception) {
                Toast.makeText(this, "File Read Failed", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Please provide a Filename", Toast.LENGTH_LONG).show()
        }
    }
    mainBinding.hapus.setOnClickListener {
        if (mainBinding.FileName.text.isNotEmpty()) {
            try {
                if (repo.deleteNote(mainBinding.FileName.text.toString())) {
                    Toast.makeText(this, "File Deleted", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "File Could Not Be Deleted", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "File Delete Failed", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            mainBinding.FileName.text.clear()
            mainBinding.Catatan.text.clear()
        } else {
            Toast.makeText(this, "Please provide a Filename", Toast.LENGTH_LONG).show()
        }
    }
}

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->

                    val location: Location? = task.result
                    if (location != null) {

                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)


                        mainBinding.apply {
                            tvLatitude.text = "Latitude\n${list[0].latitude}"
                            tvLongitude.text = "Longitude\n${list[0].longitude}"
                            tvCountryName.text = "Nama Negara\n${list[0].countryName}"
                            tvLocality.text = "Wilayah/Area\n${list[0].locality}"
                            tvAddress.text = "Alamat\n${list[0].getAddressLine(0)}"
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

}