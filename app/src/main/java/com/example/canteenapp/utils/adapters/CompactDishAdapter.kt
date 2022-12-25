package com.example.canteenapp.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.canteenapp.databinding.DishCardCompactBinding
import com.example.canteenapp.utils.models.DishData
import com.squareup.picasso.Picasso

class CompactDishAdapter(data: MutableList<DishData>) : BaseQuickAdapter<DishData, CompactDishAdapter.VH>(items = data) {
    class VH(
        parent: ViewGroup,
        val binding: DishCardCompactBinding = DishCardCompactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: DishData?) {
        holder.binding.category.text = item?.category
        holder.binding.title.text = item?.title
        holder.binding.weight.text = String.format("%s г.", item?.weight)
        holder.binding.price.text = String.format("%s₽", item?.price)
        Picasso.get().load(item?.picture).into(holder.binding.shapeableImageView)
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}