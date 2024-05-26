package com.example.trelloclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ItemBoardBinding
import com.example.trelloclone.models.Board

open class BoardItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Board>) :RecyclerView.Adapter<BoardItemsAdapter.ViewHolder>(){
    inner class ViewHolder(val binding: ItemBoardBinding): RecyclerView.ViewHolder(binding.root) {


    }

    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBoardBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }

    override fun getItemCount(): Int =list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        if(holder is ViewHolder){
            Glide.with(context).load(model.image)
                .centerCrop().placeholder(R.color.secondary_text_color).
                into(holder.binding.imageViewBoardImage)
            holder.binding.textViewName.text=model.name
            holder.binding.textViewCreatedBy.text = model.createdBy

            holder.itemView.setOnClickListener {

                if(onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }



    interface  OnClickListener{
        fun onClick(position: Int, model: Board)
    }
}