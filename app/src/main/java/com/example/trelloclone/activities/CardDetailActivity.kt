package com.example.trelloclone.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.CardMemberListAdapter
import com.example.trelloclone.databinding.ActivityCardDetailBinding
import com.example.trelloclone.databinding.ItemCardSelectedMemberBinding
import com.example.trelloclone.dialog.LabelColorListDialog
import com.example.trelloclone.dialog.MembersListDialog
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.Card
import com.example.trelloclone.models.SelectedMembers
import com.example.trelloclone.models.Task
import com.example.trelloclone.models.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailBinding
    private lateinit var boardDetails: Board
    private var taskListPosition = -1
    private var cardPosition = -1
    private var selectedColor = ""
    private lateinit var membersDetailList: ArrayList<User>
    private var selectedDueDateMiliseconds: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar(this, binding.toolbarCardDetailsActivity)
        getIntentData()
        binding.toolbarCardDetailsActivity.title = boardDetails
            .taskList[taskListPosition].cards[cardPosition].name

        binding.etNameCardDetails.setText(
            boardDetails
                .taskList[taskListPosition].cards[cardPosition].name
        )
        binding.etNameCardDetails.setSelection(binding.etNameCardDetails.text.toString().length)

        selectedColor = boardDetails.taskList[taskListPosition].cards[cardPosition].labelColor
        if (selectedColor.isNotEmpty()) {
            setColor()
        }
        binding.btnUpdateCardDetails.setOnClickListener {
            if (binding.etNameCardDetails.text.toString().isNotEmpty()) {

                updateCardDetails()
            } else {
                Toast.makeText(this, "Enter a card name", Toast.LENGTH_LONG).show()
            }
        }
        binding.tvSelectLabelColor.setOnClickListener {
            labelColorListDialog()
        }
        binding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }
        setupSelectedMembersList()

        selectedDueDateMiliseconds = boardDetails.
        taskList[taskListPosition].cards[cardPosition].dueDate
        if(selectedDueDateMiliseconds > 0){
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            binding.tvSelectDueDate.text = sdf.format(Date(selectedDueDateMiliseconds))
        }
        binding.tvSelectDueDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun getIntentData() {
        if (intent.hasExtra("taskListItemPosition")) {
            taskListPosition = intent.getIntExtra("taskListItemPosition", -1)

        }
        if (intent.hasExtra("cardListItemPosition")) {
            cardPosition = intent.getIntExtra("cardListItemPosition", -1)

        }
        if (intent.hasExtra("boardDetail")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                boardDetails = intent.getParcelableExtra("boardDetail", Board::class.java)!!
            }

        }
        if (intent.hasExtra("boardMembersList")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                membersDetailList =
                    intent.getParcelableArrayListExtra("boardMembersList", User::class.java)!!
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {
        val card = Card(
            binding.etNameCardDetails.text.toString(),
            boardDetails.taskList[taskListPosition].cards[cardPosition].createdBy,
            boardDetails.taskList[taskListPosition].cards[cardPosition].assignedTo,
            selectedColor,
            selectedDueDateMiliseconds

        )

        val taskList: ArrayList<Task> =boardDetails.taskList
        taskList.removeAt(taskList.size -1)
        boardDetails.taskList[taskListPosition].cards[cardPosition] = card
        showProgressDialog("Please wait")

        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_card -> {

                alertDialogForDeleteCard(boardDetails.taskList[taskListPosition].cards[cardPosition].name)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteCard() {
        val cardsList: ArrayList<Card> = boardDetails
            .taskList[taskListPosition].cards
        cardsList.removeAt(cardPosition)
        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[taskListPosition].cards = cardsList
        showProgressDialog("Please wait")
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }

    private fun alertDialogForDeleteCard(title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete card \"$title\".")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss()

            deleteCard()


        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun colorsList(): ArrayList<String> {
        val colorList: ArrayList<String> = ArrayList()
        with(colorList) {
            add("#43C86F")
            add("#0C90F1")
            add("#F72400")
            add("#7A8089")
            add("#D57C1D")
            add("#770000")
            add("#0022F8")
        }
        return colorList
    }

    private fun setColor() {
        binding.tvSelectLabelColor.text = ""
        binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(selectedColor))
    }

    private fun labelColorListDialog() {
        val colorList: ArrayList<String> = colorsList()
        val listDialog =
            object : LabelColorListDialog(this, colorList, "Select Label Color", selectedColor) {
                override fun onItemSelected(color: String) {
                    selectedColor = color
                    setColor()
                }

            }
        listDialog.show()

    }

    private fun membersListDialog() {
        var cardAssignedMembersList = boardDetails.taskList[taskListPosition]
            .cards[cardPosition].assignedTo
        if (cardAssignedMembersList.size > 0) {
            for (i in membersDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (membersDetailList[i].id == j) {
                        membersDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in membersDetailList.indices) {

                membersDetailList[i].selected = false


            }
        }
        val listDialog = object : MembersListDialog(this, membersDetailList, "Select Member"){
            override fun onItemSelected(user: User, action: String) {


                if(action == "Select"){
                    if(!boardDetails.taskList[taskListPosition]
                        .cards[cardPosition].assignedTo.contains(user.id))
                        boardDetails.taskList[taskListPosition]
                            .cards[cardPosition].assignedTo.add(user.id)
                }else{
                    boardDetails.taskList[taskListPosition]
                        .cards[cardPosition].assignedTo.remove(user.id)

                    for(i in membersDetailList.indices){
                        if(membersDetailList[i].id == user.id){
                            membersDetailList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()

            }

        }
        listDialog.show()
    }

    private fun setupSelectedMembersList(){
        val cardAssignedMembersList =
            boardDetails.taskList[taskListPosition]
                .cards[cardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in membersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (membersDetailList[i].id == j) {
                    selectedMembersList.add(SelectedMembers(
                        membersDetailList[i].id,
                        membersDetailList[i].image))
                }
            }
        }
        val itemBinding = ItemCardSelectedMemberBinding.inflate(
            layoutInflater
        )
        binding.root.addView(itemBinding.root)
        if(selectedMembersList.size > 0){
            selectedMembersList.add((SelectedMembers("", "")))


            binding.tvSelectMembers.visibility = View.GONE
            binding.rvCardDetail.visibility = View.VISIBLE
            binding.rvCardDetail.layoutManager = GridLayoutManager(
                this, 6
            )
            val adapter = CardMemberListAdapter(this, selectedMembersList, true)
            binding.rvCardDetail.adapter = adapter
            adapter.setOnClickListener(object : CardMemberListAdapter.OnClickListener{
                override fun onClick() {
                    membersListDialog()
                }

            })
        }else{
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvCardDetail.visibility = View.GONE
        }
    }
    private fun showDatePicker(){
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOdYear, dayOfMonth ->

                val sDayOfMonth = if(dayOfMonth<10)  "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOdYear = if((monthOdYear+1)<10) "0${monthOdYear +1}" else "${monthOdYear +1}"
                val selectedDate = "$sDayOfMonth/$sMonthOdYear/$year"
                binding.tvSelectDueDate.text = selectedDate
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                selectedDueDateMiliseconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show()
    }
}
