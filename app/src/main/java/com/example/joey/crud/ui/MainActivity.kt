package com.example.joey.crud.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.joey.crud.R
import com.example.joey.crud.adapter.UserAdapter
import com.example.joey.crud.data.User
import com.example.joey.crud.viewmodel.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, UserActivity::class.java)
            startActivityForResult(intent, ADD_USER_REQUEST_CODE)
        }

        initUI()
    }

    private fun initUI() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = UserAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get a new or existing ViewModel from the ViewModelProviders
        userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)

        // Add an Observer class on the LiveData
        userViewModel.allUsers.observe(this, Observer { users ->
            // Update the cached copy of the users in the adapter
            users?.let { adapter.setUsers(it) }
        })

        val itemTouch = object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                userViewModel.delete(adapter.swipeToDelete(viewHolder.adapterPosition))
                Toast.makeText(this@MainActivity, "User deleted.", Toast.LENGTH_SHORT).show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouch)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter.setOnItemClickListener(object : UserAdapter.OnItemClickListener {
            override fun onItemClick(user: User) {
                val intent = Intent(this@MainActivity, UserActivity::class.java)
                intent.putExtra(UserActivity.EXTRA_ID, user.id)
                intent.putExtra(UserActivity.EXTRA_NAME, user.name)
                intent.putExtra(UserActivity.EXTRA_EMAIL, user.email)
                intent.putExtra(UserActivity.EXTRA_MAJOR, user.major)
                startActivityForResult(intent, EDIT_USER_REQUEST_CODE)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater: MenuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.del) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you really sure you want to delete all users?")
            builder.setPositiveButton("OK") { _, _ ->
                userViewModel.deleteAll()
                Toast.makeText(this, "All users deleted.", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_USER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val user = User(
                    it.getStringExtra(UserActivity.EXTRA_ID), it.getStringExtra(UserActivity.EXTRA_NAME),
                    it.getStringExtra(UserActivity.EXTRA_EMAIL), it.getStringExtra(UserActivity.EXTRA_MAJOR)
                )
                userViewModel.insert(user)
            }
            Toast.makeText(this, "User created successfully.", Toast.LENGTH_SHORT).show()
        } else if (requestCode == EDIT_USER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val user = User(
                    it.getStringExtra(UserActivity.EXTRA_ID), it.getStringExtra(UserActivity.EXTRA_NAME),
                    it.getStringExtra(UserActivity.EXTRA_EMAIL), it.getStringExtra(UserActivity.EXTRA_MAJOR)
                )
                userViewModel.update(user)
            }
            Toast.makeText(this, "User updated successfully.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ADD_USER_REQUEST_CODE = 1
        const val EDIT_USER_REQUEST_CODE = 2
    }
}
