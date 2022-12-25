package com.example.canteenapp.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.canteenapp.databinding.MyRequestCardBinding
import com.example.canteenapp.utils.models.MyRequestData

class MyRequestsAdapter(data: MutableList<MyRequestData>) :
    BaseQuickAdapter<MyRequestData, MyRequestsAdapter.VH>(items = data) {
    class VH(
        parent: ViewGroup,
        val binding: MyRequestCardBinding = MyRequestCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: MyRequestData?) {
        holder.binding.totalPrice.text = String.format("%s₽", item?.info?.get("price") ?: "0")

        if (item?.dishes?.get("mainDish") == null) {
            holder.binding.mainDishTitle.visibility = View.INVISIBLE
        } else {
            holder.binding.mainDishTitle.text = String.format("- %s", item.dishes["mainDish"]?.second)
        }
        if (item?.dishes?.get("secondDish") == null) {
            holder.binding.secondDishTitle.visibility = View.INVISIBLE
        } else {
            holder.binding.secondDishTitle.text = String.format("- %s", item.dishes["secondDish"]?.second)
        }
        if (item?.dishes?.get("drink") == null) {
            holder.binding.drinkTitle.visibility = View.INVISIBLE
        } else {
            holder.binding.drinkTitle.text = String.format("- %s", item.dishes["drink"]?.second)
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}