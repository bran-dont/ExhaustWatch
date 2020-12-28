package com.example.exhaustwatch

import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var rows: List<Array<String>> = ArrayList()
        val csvReader = CSVReader(this@MainActivity, "vehicles.csv")
        try {
            rows = csvReader.readCSV()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        for (i in rows.indices) {
            Log.d("h", java.lang.String.format("row %s: %s, %s", i, rows[i][0], rows[i][6]))

        val fullName = findViewById<TextView>(R.id.your_name)
        val email = findViewById<TextView>(R.id.your_email)
        val phone = findViewById<TextView>(R.id.your_number)

        val mToolbar = findViewById<Toolbar>(R.id.main_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = "Home"

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawable_layout)
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        //val navView = navigationView.inflateHeaderView(R.layout.navigation_header)
        val fAuth = FirebaseAuth.getInstance()
        val fStore = FirebaseFirestore.getInstance()
        val userId = fAuth.currentUser?.uid

        userId?.let { fStore.collection("users").document(it) }
            ?.addSnapshotListener { documentSnapshot, e ->
                if(e != null)
                {
                    Log.w("TAG", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (documentSnapshot != null) {
                    phone.text = documentSnapshot.getString("phone")
                    fullName.text = documentSnapshot.getString("fName")
                    email.text = documentSnapshot.getString("email")
                }
            }

        navigationView.setNavigationItemSelectedListener { item ->
            userMenuSelector(item)
            false

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawable_layout)
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun userMenuSelector(item: MenuItem)
    {
        when(item.itemId)
        {
            R.id.nav_home_menu ->
            {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }

            R.id.nav_logout ->
            {
                FirebaseAuth.getInstance().signOut()//logout
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }
        }
    }

}
