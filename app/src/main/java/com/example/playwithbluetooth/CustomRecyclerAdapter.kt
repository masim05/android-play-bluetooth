package com.example.playwithbluetooth

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomRecyclerAdapter(private val devices: List<BluetoothDevice>) :
    RecyclerView.Adapter<CustomRecyclerAdapter.DeviceHolder>() {

    class DeviceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.deviceView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)
        return DeviceHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        val device = devices[position]

        "${device.name}: ${device.address}".also { holder.textView.text = it }
    }

    override fun getItemCount() = devices.size
}