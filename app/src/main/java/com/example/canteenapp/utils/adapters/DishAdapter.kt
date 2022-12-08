package com.example.canteenapp.utils.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.canteenapp.databinding.DishCardBinding
import com.example.canteenapp.utils.models.DishData
import com.squareup.picasso.Picasso

class DishAdapter(private val list: MutableList<DishData>) :
    RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    private var listener:DishAdapterClicksInterface? = null
    fun setListener(listener: DishAdapterClicksInterface) {
        this.listener = listener
    }

    class DishViewHolder(val binding: DishCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val binding = DishCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.dishTitle.text = this.title
                binding.dishCategory.text = this.category
                binding.dishPrice.text = String.format("%s₽", this.price)
                binding.dishWeight.text = String.format("%s г.", this.weight)
                binding.dishCard.setCardBackgroundColor(
                    if (this.isActive)
                        Color.parseColor("#FFE4BC")
                    else
                        Color.parseColor("#F9FBFC"))
                binding.checkedCardIcon.visibility = if (this.isActive) View.VISIBLE else View.INVISIBLE
                binding.checkedCardIcon.animate().alpha(if (this.isActive) 1.0f else 0.0f).duration =
                    200

                Picasso.get().load(this.picture).into(binding.dishPicture)

                binding.root.setOnClickListener {
                    listener?.onCardClicked(this)
                }
            }
        }
    }



    override fun getItemCount(): Int {
        return list.size
    }

    interface DishAdapterClicksInterface {
        fun onCardClicked(dishData: DishData)
    }

}