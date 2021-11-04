package com.example.playwithbluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

import android.location.LocationManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "Main Activity"
private val foundDevices = mutableListOf<BluetoothDevice>()

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "On create started")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.scan_button)
        val textMessage: TextView = findViewById(R.id.textMessage)

        val recyclerView: RecyclerView = findViewById(R.id.deviceList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CustomRecyclerAdapter(foundDevices)

        button.setOnClickListener {
            Log.v(TAG, "button.setOnClickListener started")
            foundDevices.clear()
            textMessage.text = ""
            @Suppress("NotifyDataSetChanged") recyclerView.adapter?.notifyDataSetChanged()
            val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                Log.e(TAG, "bluetoothAdapter is null")
                textMessage.text = getString(R.string.error_bluetooth_adapter_is_null)
                return@setOnClickListener
            }

            if (!bluetoothAdapter.isEnabled) {
                // TODO to enable BT
                Log.e(TAG, "bluetooth is disabled")
                textMessage.text = getString(R.string.error_bluetooth_is_disabled)
                return@setOnClickListener
            }

            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!isGpsEnabled) {
                // TODO to enable GPS
                Log.e(TAG, "location is disabled")
                textMessage.text = getString(R.string.error_location_is_disabled)
                return@setOnClickListener
            }
            try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (e: SecurityException) {
                Log.e(TAG, e.localizedMessage)
                textMessage.text = e.localizedMessage
                return@setOnClickListener
            }

            if (bluetoothAdapter.isDiscovering) {
                Log.v(TAG, "Discovering...")
                val discoveryCancelled = bluetoothAdapter.cancelDiscovery()
                Log.v(TAG, "discoveryCancelled: $discoveryCancelled")
            }

            // Register for broadcasts when a device is discovered.
            registerReceiver(actionFoundReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            registerReceiver(discoveryFinishedReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))

            if (!bluetoothAdapter.startDiscovery()) {
                Log.e(TAG, "startDiscovery() failed")
                textMessage.text = getString(R.string.error_start_discovery_failed)
                return@setOnClickListener
            }
            textMessage.text = getString(R.string.discovering)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(actionFoundReceiver)
        unregisterReceiver(discoveryFinishedReceiver)
    }

    private val actionFoundReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    Log.v(TAG, "On BluetoothDevice.ACTION_FOUND")
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    foundDevices += device as BluetoothDevice
                    val recyclerView: RecyclerView = findViewById(R.id.deviceList)
                    recyclerView.adapter?.notifyItemInserted(foundDevices.size - 1)
                    Log.v(TAG, "${device?.name}: ${device?.address}")
                }
            }
        }
    }

    private val discoveryFinishedReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.v(TAG, "On BluetoothAdapter.ACTION_DISCOVERY_FINISHED")
                    val textMessage: TextView = findViewById(R.id.textMessage)
                    textMessage.text = getString(R.string.discovery_finished)
                }
            }
        }
    }
}

