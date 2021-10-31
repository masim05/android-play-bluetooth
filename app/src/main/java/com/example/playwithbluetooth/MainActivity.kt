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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        button.setOnClickListener {
            Log.v(TAG, "button.setOnClickListener started")
            foundDevices.clear()
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

            val recyclerView: RecyclerView = findViewById(R.id.deviceList)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = CustomRecyclerAdapter(foundDevices)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
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
                    foundDevices += device as BluetoothDevice
                    Log.v(TAG, "${device?.name}: ${device?.address}")
                }
            }
        }
    }

}

class CustomRecyclerAdapter(private val devices: List<BluetoothDevice>) :
    RecyclerView.Adapter<CustomRecyclerAdapter.DeviceHolder>(){

        class DeviceHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var textView: TextView? = null

        init {
            textView = itemView.findViewById(R.id.textView)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)
        return DeviceHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        val device = devices[position]

        "${device.name}: ${device.address}".also { holder.textView?.text = it }
    }

    override fun getItemCount() = devices.size
}
