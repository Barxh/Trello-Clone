package com.example.trelloclone.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.databinding.ItemTaskBinding
import com.example.trelloclone.models.Task
import java.util.Collections

open class TaskListItemsAdapter(private val context: Context, private var list: ArrayList<Task>) :
    RecyclerView.Adapter<TaskListItemsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    private var positionDraggedFrom = -1
    private var positionDraggedTo = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.setMargins(((15).toDp()).toPx(), 0, (40).toDp().toPx(), 0)
        v.layoutParams = layoutParams
        val view = ViewHolder(ItemTaskBinding.bind(v))
        return view
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val model = list[position]
        if (holder is ViewHolder) {
            if (position == list.size - 1) {
                holder.binding.tvAddTaskList.visibility = View.VISIBLE
                holder.binding.llTaskItem.visibility = View.GONE

            } else {
                holder.binding.tvAddTaskList.visibility = View.GONE
                holder.binding.llTaskItem.visibility = View.VISIBLE


            }

            holder.binding.tvTaskListTitle.text = model.title
            holder.binding.tvAddTaskList.setOnClickListener {
                holder.binding.tvAddTaskList.visibility = View.GONE
                holder.binding.cvAddTaskListName.visibility = View.VISIBLE


            }
            holder.binding.ibCloseListName.setOnClickListener {
                holder.binding.tvAddTaskList.visibility = View.VISIBLE
                holder.binding.cvAddTaskListName.visibility = View.GONE
            }
            holder.binding.ibDoneListName.setOnClickListener {

                val listName = holder.binding.etTaskListName.text.toString()
                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please enter task list name.", Toast.LENGTH_LONG)
                        .show()
                }
            }

            holder.binding.ibEditListName.setOnClickListener {
                holder.binding.etEditTaskListName.setText(model.title)
                holder.binding.llTitleView.visibility = View.GONE
                holder.binding.cvEditTaskListName.visibility = View.VISIBLE


            }
            holder.binding.ibCloseEditableView.setOnClickListener {
                holder.binding.llTitleView.visibility = View.VISIBLE
                holder.binding.cvEditTaskListName.visibility = View.GONE
            }

            holder.binding.ibDoneEditListName.setOnClickListener {

                val listName = holder.binding.etEditTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                } else {
                    Toast.makeText(context, "Please enter task list name.", Toast.LENGTH_LONG)
                        .show()
                }
            }

            holder.binding.ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }




            holder.binding.tvAddCard.setOnClickListener {
                holder.binding.tvAddCard.visibility = View.GONE
                holder.binding.cvAddCard.visibility = View.VISIBLE

            }
            holder.binding.ibCloseListName.setOnClickListener {

                holder.binding.tvAddCard.visibility = View.VISIBLE
                holder.binding.cvAddCard.visibility = View.GONE


            }

            holder.binding.ibDoneCardName.setOnClickListener {
                val cardName = holder.binding.etCardName.text.toString()

                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {

                        context.addCardToTaskList(position, cardName)
                    }

                } else {
                    Toast.makeText(context, "Please enter a card name", Toast.LENGTH_LONG).show()
                }

            }
            holder.binding.rvCardList.layoutManager =
            LinearLayoutManager(context)
            holder.binding.rvCardList.setHasFixedSize(true)
            val adapter = CardListItemsAdapter(context, model.cards)

            holder.binding.rvCardList.adapter = adapter
            adapter.setOnClickListener(object : CardListItemsAdapter.OnClickListener{
                override fun onClick(cardPosition: Int) {
                    if(context is TaskListActivity){
                        context.cardDetails(position, cardPosition)
                    }
                }

            })
            val dividerItemDecoration =  DividerItemDecoration(context, DividerItemDecoration.VERTICAL)

            holder.binding.rvCardList.addItemDecoration(dividerItemDecoration)
            val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
                ){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                   val draggedPosition = viewHolder.adapterPosition
                    val targetPosition = target.adapterPosition
                    if(positionDraggedFrom == -1){
                        positionDraggedFrom = draggedPosition
                    }
                    positionDraggedTo = targetPosition
                    Collections.swap(list[position].cards, draggedPosition, targetPosition)
                    adapter.notifyItemMoved(draggedPosition, targetPosition)
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)

                    if(positionDraggedFrom!=-1 && positionDraggedTo !=1 && positionDraggedTo!= positionDraggedFrom){
                        (context as TaskListActivity).updateCardsInTaskList(
                            position,list[position].cards
                        )
                    }
                    positionDraggedTo = -1
                    positionDraggedFrom = -1
                }

            })
            helper.attachToRecyclerView(holder.binding.rvCardList)
        }
    }

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()


    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss()
            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }

        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}