package com.example.canteenapp.utils.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.canteenapp.databinding.DishCardCompactBinding
import com.example.canteenapp.databinding.FragmentAddDishDialogBinding
import com.example.canteenapp.databinding.KidListItemBinding
import com.example.canteenapp.ui.dialogs.AddDishDialogFragment
import com.example.canteenapp.utils.models.DishData
import com.example.canteenapp.utils.models.KidData

class KidsListAdapter(data: MutableList<KidData>) :
    BaseQuickAdapter<KidData, KidsListAdapter.VH>(items = data) {
    class VH(
        parent: ViewGroup,
        val binding: KidListItemBinding = KidListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    private var listener: KidsAdapterClickInterface? = null
    fun setListener(listener: KidsAdapterClickInterface) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: KidData?) {
        val words = item?.name?.split("\\s".toRegex())?.toTypedArray()
        holder.binding.name.text = String.format("%s %s", words?.getOrNull(1) ?: "", words?.getOrNull(2) ?: "")
        holder.binding.addToOrderButton.setOnClickListener {
            if (item != null) {
                listener?.onAddToOrderClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    interface KidsAdapterClickInterface {
        fun onAddToOrderClicked(kidData: KidData)
    }
}