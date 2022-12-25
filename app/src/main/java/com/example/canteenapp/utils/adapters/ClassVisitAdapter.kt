package com.example.canteenapp.utils.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.example.canteenapp.R
import com.example.canteenapp.utils.models.KidVisitData

class ClassVisitAdapter(private val data: MutableList<KidVisitData>, context: Context) :
    ArrayAdapter<KidVisitData>(context, R.layout.kid_checkbox, data) {
    private var listener: onCheckedChangeListener? = null

    fun setListener(listener: onCheckedChangeListener) {
        this.listener = listener
    }

    private class ViewHolder {
        lateinit var name: TextView
        lateinit var checkbox: CheckBox
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): KidVisitData {
        return data[position]
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        val result: View
        if (convertView == null) {
            viewHolder = ViewHolder()
            convertView =
                LayoutInflater.from(parent.context).inflate(R.layout.kid_checkbox, parent, false)
            viewHolder.name =
                convertView.findViewById(R.id.name)
            viewHolder.checkbox =
                convertView.findViewById(R.id.checkbox)
            result = convertView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
            result = convertView
        }

        val item: KidVisitData = getItem(position)
        viewHolder.name.text = item.kidData.name
        viewHolder.checkbox.isChecked = item.checked
        viewHolder.checkbox.setOnClickListener { listener?.onChange() }
        return result
    }

    interface onCheckedChangeListener {
        fun onChange()
    }
}