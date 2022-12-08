package com.example.canteenapp.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.canteenapp.databinding.DishCardBinding
import com.example.canteenapp.databinding.SettingsButtonBinding
import com.squareup.picasso.Picasso

class SettingsAdapter(private val list: MutableList<String>) :
    RecyclerView.Adapter<SettingsAdapter.SettingsButtonViewHolder>() {

    class SettingsButtonViewHolder(val binding: SettingsButtonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsButtonViewHolder {
        val binding = SettingsButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingsButtonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingsButtonViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.settingsButton.text = this
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}