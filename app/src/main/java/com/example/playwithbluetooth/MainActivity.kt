package com.example.playwithbluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

import android.location.LocationManager


private const val TAG = "Main Activity"


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "On create started")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.scan_button)
        button.setOnClickListener {
            Log.v(TAG, "button.setOnClickListener started")
            val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                Log.e(TAG, "bluetoothAdapter is null")
                return@setOnClickListener
            }

            if (!bluetoothAdapter.isEnabled) {
                // TODO to enable BT
                Log.e(TAG, "bluetooth is disabled")
                return@setOnClickListener
            }

            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!isGpsEnabled) {
                // TODO to enable GPS
                Log.e(TAG, "location is disabled")
                return@setOnClickListener
            }
            try {
                val location: Location? =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (e: SecurityException) {
                Log.e(TAG, e.localizedMessage)
                return@setOnClickListener
            }

            if (bluetoothAdapter.isDiscovering) {
                Log.v(TAG, "Discovering...")
                val discoveryCancelled = bluetoothAdapter.cancelDiscovery()
                Log.v(TAG, "discoveryCancelled: $discoveryCancelled")
            }

            // Register for broadcasts when a device is discovered.
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)

            if (!bluetoothAdapter.startDiscovery()) {
                Log.e(TAG, "startDiscovery() failed")
                return@setOnClickListener
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    Log.v(TAG, "On BluetoothDevice.ACTION_FOUND")
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                    Log.v(TAG, "$deviceName: $deviceHardwareAddress")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }
}

