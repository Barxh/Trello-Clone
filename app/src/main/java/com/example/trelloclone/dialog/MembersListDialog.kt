package com.example.trelloclone.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.MemberListItemsAdapter
import com.example.trelloclone.databinding.DialogListBinding
import com.example.trelloclone.models.User

abstract class MembersListDialog(
    context: Context,
    private var membersList : ArrayList<User>,
    private val title: String): Dialog(context) {

        private var adapter: MemberListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list , null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)

    }

    private fun setUpRecyclerView(view: View?) {
        val binding: DialogListBinding = DialogListBinding.bind(view!!)
        binding.tvTitle.text = title

        if(membersList.size > 0){
            binding.rvList.layoutManager = LinearLayoutManager(context)
            binding.rvList.setHasFixedSize(true)
            adapter = MemberListItemsAdapter(context, membersList)
            binding.rvList.adapter = adapter

            adapter!!.setOnClickListener(object : MemberListItemsAdapter.OnClickListener{
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }

            })
        }
    }

    protected abstract fun onItemSelected(user: User, action: String)

}