package com.example.trelloclone.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.databinding.ItemCardBinding
import com.example.trelloclone.databinding.ItemTaskBinding
import com.example.trelloclone.models.Card
import com.example.trelloclone.models.SelectedMembers

import kotlin.collections.ArrayList

open class CardListItemsAdapter(val context: Context, private val cardsList : ArrayList<Card> )
    :RecyclerView.Adapter<CardListItemsAdapter.ViewHolder>() {
        private var onClickListener: OnClickListener?  =null
    inner class ViewHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(ItemCardBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int =cardsList.size

    interface OnClickListener{
        fun onClick(cardPosition: Int)
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = cardsList[position]
        if(holder is ViewHolder){

            if(model.labelColor.isNotEmpty()){
                holder.binding.viewLabelColor.visibility = View.VISIBLE
                holder.binding.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            }else{
                holder.binding.viewLabelColor.visibility = View.GONE
            }
            holder.binding.tvCardName.text = model.name

            if((context as TaskListActivity).assignedMembersDetailList.size> 0){
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
                for(i in context.assignedMembersDetailList.indices){
                    for(j in model.assignedTo){
                        if(context.assignedMembersDetailList[i].id == j){
                            val selectedMembers = SelectedMembers(
                                context.assignedMembersDetailList[i].id,
                                context.assignedMembersDetailList[i].image
                            )
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }
                if(selectedMembersList.isNotEmpty()){
                    if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                        holder.binding.rvCardSelectedMembers.visibility = View.GONE
                    }else{
                        holder.binding.rvCardSelectedMembers.visibility = View.VISIBLE
                        holder.binding.rvCardSelectedMembers.layoutManager = GridLayoutManager(context, 4)
                        val adapter = CardMemberListAdapter(context, selectedMembersList, false)
                        holder.binding.rvCardSelectedMembers.adapter = adapter
                        adapter.setOnClickListener(object : CardMemberListAdapter.OnClickListener{
                            override fun onClick() {
                                if(onClickListener != null ){
                                    onClickListener!!.onClick(position)
                                }
                            }

                        })
                    }
                }else{
                    holder.binding.rvCardSelectedMembers.visibility = View.GONE
                }
            }
            holder.itemView.setOnClickListener {
                if(onClickListener!= null){
                    onClickListener!!.onClick(position)
                }
            }
        }
    }
}