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

        // Carregar o estado do serviço salvo no SharedPreferences
        val sharedPreferences = context.getSharedPreferences("service_preferences", Context.MODE_PRIVATE)
        val isServiceEnabled = sharedPreferences.getBoolean(service.name, service.isEnabled)  // Valor padrão é 'service.isEnabled'
        holder.serviceSwitch.isChecked = isServiceEnabled

        holder.serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
            onServiceToggle(service.name, isChecked)

            // Salvar o estado do serviço no SharedPreferences
            val sharedPreferences = context.getSharedPreferences("service_preferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean(service.name, isChecked)
            editor.apply()  // Salva as alterações de maneira assíncrona
        }
    }

    override fun getItemCount() = services.size
}
