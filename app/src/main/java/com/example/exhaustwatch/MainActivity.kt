package com.example.exhaustwatch

import android.os.Bundle
import android.provider.SyncStateContract
import java.io.IOException


import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


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

        //permissions

        // permission code (random but needed)

        val ACTIVITY_PERMISSION = 100

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "I have permission", Toast.LENGTH_SHORT).show()
            }
            else {
                requestStoragePermission()
            }
        }
        else{
            Toast.makeText(this, "we aint Q", Toast.LENGTH_SHORT).show()
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACTIVITY_RECOGNITION)) {
                showDialog()
            }
            else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("com.google.android.gms.permission.ACTIVITY_RECOGNITION"),
                    ACTIVITY_PERMISSION
                )
            }
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACTIVITY_RECOGNITION)) {
            showDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 100)
        }
    }

    private fun showDialog() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed to calculate CO2 exhaust saved.")
                .setPositiveButton("OK") { dialog, which ->
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 100)
                }
                .setNegativeButton("cancel") { dialog, which -> dialog.dismiss() }
                .create().show()
        }
        else { //doesnt really work TODO
            android.app.AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is needed to calculate CO2 exhaust saved.")
                .setPositiveButton("OK") { dialog, which ->
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf("com.google.android.gms.permission.ACTIVITY_RECOGNITION"), 100)
                }
                .setNegativeButton("cancel") { dialog, which -> dialog.dismiss() }
                .create().show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
