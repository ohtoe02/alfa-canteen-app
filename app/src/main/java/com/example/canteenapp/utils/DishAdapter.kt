package com.example.canteenapp.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.canteenapp.databinding.DishCardBinding
import com.squareup.picasso.Picasso

class DishAdapter(private val list: MutableList<DishData>) :
    RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    class DishViewHolder(val binding: DishCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val binding = DishCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        println(123)
        return DishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.dishTitle.text = this.dishTitle
                binding.dishCategory.text = this.dishCategory
                binding.dishPrice.text = String.format("%s₽", this.dishPrice)
                binding.dishWeight.text = String.format("%s г.", this.dishWeight)
                Picasso.get().load(this.dishPicture).into(binding.dishPicture)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}