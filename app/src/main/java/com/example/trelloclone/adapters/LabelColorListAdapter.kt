package com.example.trelloclone.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.databinding.ItemLabelColorBinding

class LabelColorListAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val selectedColor: String) : RecyclerView.Adapter<LabelColorListAdapter.ViewHolder>()  {


    inner class ViewHolder(val binding: ItemLabelColorBinding) : RecyclerView.ViewHolder(binding.root){

    }
    var onItemClickListener : OnItemClickListener? = null
    interface OnItemClickListener{
        fun onClick(position: Int, color: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLabelColorBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(holder is ViewHolder){
            holder.binding.viewMain.setBackgroundColor(Color.parseColor(list[position]))

            if(list[position] == selectedColor){
                holder.binding.ivSelectedColor.visibility = View.VISIBLE

            }else{
                holder.binding.ivSelectedColor.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                if(onItemClickListener!= null){
                    onItemClickListener!!.onClick(position, list[position])
                }
            }
        }
    }
}