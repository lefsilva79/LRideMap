package com.lefsilva.lridemap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ServiceAdapter(
    private val context: Context,
    private val services: List<ServiceItem>,
    private val onServiceToggle: (String, Boolean) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val serviceIcon: ImageView = view.findViewById(R.id.serviceIcon)
        val serviceName: TextView = view.findViewById(R.id.serviceName)
        val serviceSwitch: Switch = view.findViewById(R.id.serviceSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.service_item, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]

        holder.serviceIcon.setImageResource(service.iconResId)
        holder.serviceName.text = service.name
        holder.serviceSwitch.isChecked = service.isEnabled

        holder.serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
            onServiceToggle(service.name, isChecked)
        }
    }

    override fun getItemCount() = services.size
}