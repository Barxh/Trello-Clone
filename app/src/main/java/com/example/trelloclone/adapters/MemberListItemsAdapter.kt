package com.example.trelloclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ItemMemberBinding
import com.example.trelloclone.models.User

class MemberListItemsAdapter(val context: Context, val list : ArrayList<User>) : RecyclerView.Adapter<MemberListItemsAdapter.ViewHolder>(){
    inner class ViewHolder(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root) {

    }
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMemberBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = list.size

    interface OnClickListener{
        fun onClick(position: Int, user: User, action: String)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        if(holder is ViewHolder){
            Glide.with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.binding.ivMemberImage)

        }
        holder.binding.tvMemberName.text = model.name
        holder.binding.tvMemberEmail.text = model.email

        if(model.selected){

            holder.binding.imageViewSelectedMember.visibility = View.VISIBLE
        }else{
            holder.binding.imageViewSelectedMember.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {

            if(onClickListener!=null){
                if(model.selected){
                    onClickListener!!.onClick(position, model, "UnSelect")
                }else{
                    onClickListener!!.onClick(position, model, "Select")

                }
            }
        }
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
}