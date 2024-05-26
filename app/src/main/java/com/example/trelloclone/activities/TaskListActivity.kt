package com.example.trelloclone.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.TaskListItemsAdapter
import com.example.trelloclone.databinding.ActivityTaskListBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.Card
import com.example.trelloclone.models.Task
import com.example.trelloclone.models.User
import kotlin.math.log

class TaskListActivity : BaseActivity() {
    private lateinit var boardDetails : Board
    private lateinit var binding : ActivityTaskListBinding
    private lateinit var launcher : ActivityResultLauncher<Intent>
    private lateinit var  boardDocumentId: String
     lateinit var assignedMembersDetailList: ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(intent.hasExtra("documentId")){
            boardDocumentId = intent.getStringExtra("documentId")!!
        }
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
            if(result.resultCode == Activity.RESULT_OK){
                showProgressDialog("Please wait")
                FirestoreClass().getBoardDetails(this, boardDocumentId)

            }
        }

        setupActionBar(this, binding.toolbarTaskListActivity)


        showProgressDialog("Please wait")
        FirestoreClass().getBoardDetails(this, boardDocumentId)

    }

    fun boardDetails(board: Board) {

        boardDetails = board
        hideProgressDialog()
        binding.toolbarTaskListActivity.title = board.name

        showProgressDialog("Please wait")
        FirestoreClass().getAssignedMembersListDetails(this, board.assistedTo)
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog("Please wait")

        FirestoreClass().getBoardDetails(this , boardDetails.documentId)
    }
    fun createTaskList(taskListName: String){
        val task = Task(taskListName, FirestoreClass().getCurrentUserId())
        boardDetails.taskList.add(0,task)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog("Please wait")
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }
    fun updateTaskList(position : Int, listName: String, model : Task){
        val task = Task(listName, model.createdBy)
        boardDetails.taskList[position] = task
        boardDetails.taskList.removeAt(boardDetails.taskList.size -1)
        showProgressDialog("Please wait")
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }
    fun deleteTaskList(position: Int){
        boardDetails.taskList.removeAt(position)
        boardDetails.taskList.removeAt(boardDetails.taskList.size-1)
        showProgressDialog("Please wait")
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }
    fun addCardToTaskList(position: Int, cardName : String){
        boardDetails.taskList.removeAt(boardDetails.taskList.size-1)
        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserId())


        val card = Card(cardName, FirestoreClass().getCurrentUserId(), cardAssignedUsersList)


        val cardsList = boardDetails.taskList[position].cards
        cardsList.add(card)
        val task = Task(
            boardDetails.taskList[position].title,
            boardDetails.taskList[position].createdBy,
            cardsList
        )
        boardDetails.taskList[position]=task


        showProgressDialog("Please wait")
        FirestoreClass().addUpdateTaskList(this, boardDetails)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.members->{
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra("boardDetail", boardDetails)
                launcher.launch(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onResume() {


        super.onResume()

    }

    fun cardDetails(taskPosition: Int, cardPosition: Int){
        intent = Intent(this, CardDetailActivity::class.java)
        intent.putExtra("taskListItemPosition", taskPosition)
        intent.putExtra("cardListItemPosition", cardPosition)
        intent.putExtra("boardDetail", boardDetails)
        intent.putParcelableArrayListExtra("boardMembersList", assignedMembersDetailList)
        for(i in assignedMembersDetailList){
            Log.e("intent error", i.toString())
        }
        launcher.launch(intent)
    }
    fun boardMembersDetailsList(list: ArrayList<User>){

        assignedMembersDetailList = list
        hideProgressDialog()
        val addTaskList = Task("Add List")
        boardDetails.taskList.add(addTaskList)
        binding.rvTaskList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTaskList.setHasFixedSize(true)
        binding.rvTaskList.adapter = TaskListItemsAdapter(this, boardDetails.taskList)

    }
    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>){
        boardDetails.taskList.removeAt(boardDetails.taskList.size -1)
        boardDetails.taskList[taskListPosition].cards = cards
        showProgressDialog("Please wait")
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }
}