package com.example.exhaustwatch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        val mFullName = findViewById<EditText>(R.id.fullName)
        val mEmail = findViewById<EditText>(R.id.email)
        val mPassword = findViewById<EditText>(R.id.setPassword)
        val mPhone = findViewById<EditText>(R.id.phoneNumber)
        val mRegisterBtn = findViewById<Button>(R.id.registerButton)
        val mLoginBtn = findViewById<TextView>(R.id.createText)
        val mYearSpinner = findViewById<Spinner>(R.id.yearSpinner)
        val mMakeSpinner = findViewById<Spinner>(R.id.makeSpinner)
        val mModelSpinner = findViewById<Spinner>(R.id.modelSpinner)
        val REQUEST_CODE = 100

        val fAuth = FirebaseAuth.getInstance()
        val fStore = FirebaseFirestore.getInstance()

//        if(fAuth.currentUser != null)
//        {
//            startActivity(Intent(applicationContext, MainActivity::class.java))
//            finish()
//        }


        mLoginBtn.setOnClickListener { startActivity(
            Intent(
                applicationContext,
                LoginActivity::class.java
            )
        ) }
        temporaryPopulateSpinners()

        mRegisterBtn.setOnClickListener(object : View.OnClickListener {
            @Override
            override fun onClick(v: View) {
                val email = mEmail.text.toString().trim()
                val password = mPassword.text.toString().trim()
                val fullName = mFullName.text.toString().trim()
                val phoneNumber = mPhone.text.toString().trim()
                val year = mYearSpinner.selectedItem
                val make = mMakeSpinner.selectedItem.toString().trim()
                val model = mModelSpinner.selectedItem.toString().trim()

                if (TextUtils.isEmpty(fullName)) {
                    mFullName.error = "Full Name is Required"
                    return
                }
                if (TextUtils.isEmpty(email)) {
                    mEmail.error = "Email is Required"
                    return
                }
                if (TextUtils.isEmpty(password)) {
                    mPassword.error = "Password is Required"
                    return
                }
                if (TextUtils.isEmpty(phoneNumber)) {
                    mPhone.error = "Phone is Required"
                    return
                }
                if (password.length < 6) {
                    mPassword.error = "Password must be at least 6 characters"
                    return
                }
                if (phoneNumber.length < 10) {
                    mPhone.error = "Phone Number must be at least 10 characters"
                    return
                }

                if (ContextCompat.checkSelfPermission(this@SetupActivity, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this@SetupActivity,
                        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                        REQUEST_CODE)
                    // Permission is not granted
                    //TODO tell what to do if no access
                }

                //register user to firebase
                fAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@SetupActivity, "User Created.", Toast.LENGTH_SHORT)
                                .show()
                            val userID = fAuth.currentUser?.uid
                            val documentReference = userID?.let {
                                fStore.collection("users").document(
                                    it
                                )
                            }
                            val user = HashMap<String, Any>()
                            user["fName"] = fullName
                            user["email"] = email
                            user["phone"] = phoneNumber
                            user["year"] = year
                            user["make"] = make
                            user["model"] = model
                            documentReference?.set(user)?.addOnSuccessListener {
                                Log.d(
                                    "TAG",
                                    "on Success: user profile created for $userID"
                                )
                            }
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                        } else {
                            Toast.makeText(
                                this@SetupActivity,
                                "Error! " + task.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        })
    }

    fun temporaryPopulateSpinners() {
        val mYearSpinner = findViewById<Spinner>(R.id.yearSpinner)
        val mMakeSpinner = findViewById<Spinner>(R.id.makeSpinner)
        val mModelSpinner = findViewById<Spinner>(R.id.modelSpinner)

        val years = ArrayList<Int>()
        years.add(1994)
        years.add(2000)
        val makes = ArrayList<String>()
        makes.add("Audi")
        makes.add("Toyota")
        val models = ArrayList<String>()
        makes.add("A8")
        makes.add("Camry")

        val yearArrayAdapter: ArrayAdapter<Int> = ArrayAdapter<Int>(this, android.R.layout.simple_spinner_item)
        yearArrayAdapter.addAll(years)
        yearArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mYearSpinner.adapter = yearArrayAdapter

        val makeArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        makeArrayAdapter.addAll(makes)
        makeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mYearSpinner.adapter = makeArrayAdapter

        val modelArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        modelArrayAdapter.addAll(models)
        modelArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mYearSpinner.adapter = modelArrayAdapter
    }
}
