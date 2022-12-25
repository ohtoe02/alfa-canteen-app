package com.example.canteenapp.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.canteenapp.databinding.ClassCardBinding
import com.example.canteenapp.utils.models.ClassData

class ClassAdapter(data: MutableList<ClassData>) : BaseQuickAdapter<ClassData, ClassAdapter.VH>(items = data) {
    class VH(
        parent: ViewGroup,
        val binding: ClassCardBinding = ClassCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: ClassData?) {
        holder.binding.classNumber.text = item?.title
        holder.binding.studentCount.text = String.format("%s чел.", item?.studentsCount.toString())
    }
}