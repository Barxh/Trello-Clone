package com.example.trelloclone.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.LabelColorListAdapter
import com.example.trelloclone.databinding.DialogListBinding

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title:String = "",
    private var selectedColor: String = "") : Dialog(context){

        private var adapter: LabelColorListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }
    private fun setUpRecyclerView(view: View){
        val binding: DialogListBinding = DialogListBinding.bind(view)
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListAdapter(context, list, selectedColor)
        binding.rvList.adapter = adapter
        adapter!!.onItemClickListener = object : LabelColorListAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)

            }

        }
    }

    protected  abstract fun onItemSelected(color: String)
}