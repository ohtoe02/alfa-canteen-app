package com.example.canteenapp.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.canteenapp.databinding.DishCardCompactBinding
import com.example.canteenapp.databinding.RequestsDishCardBinding
import com.example.canteenapp.utils.models.DishCountData
import com.example.canteenapp.utils.models.DishData
import com.squareup.picasso.Picasso

class RequestDishCardAdapter(data: MutableList<DishCountData>) : BaseQuickAdapter<DishCountData, RequestDishCardAdapter.VH>(items = data) {
    class VH(
        parent: ViewGroup,
        val binding: RequestsDishCardBinding = RequestsDishCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: DishCountData?) {
        holder.binding.title.text = item?.dishData?.title
        holder.binding.count.text = item?.count.toString()
        Picasso.get().load(item?.dishData?.picture).into(holder.binding.shapeableImageView)
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}