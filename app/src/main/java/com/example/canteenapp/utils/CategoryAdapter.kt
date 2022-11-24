package com.example.canteenapp.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.canteenapp.databinding.CategoryCardBinding

class CategoryAdapter(private val list: MutableList<CategoryData>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: CategoryCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = CategoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.categoryTitle.text = this.categoryTitle
                binding.categoryDishesCount.text = String.format("Блюд: %s", this.categoryDishesCount.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}