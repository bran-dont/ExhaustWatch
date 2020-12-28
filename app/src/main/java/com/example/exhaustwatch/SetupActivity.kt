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
import java.io.IOException


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

                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra("CO2", 370.291666666666) //1994 toyota corolla, test value
                startActivity(intent)
            }
        })
    }

    fun temporaryPopulateSpinners() {
        val makesArrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        val modelsArrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        val years = ArrayList<String>()
        val yearSpinner = findViewById<Spinner>(R.id.yearSpinner)
        val makeSpinner = findViewById<Spinner>(R.id.makeSpinner)
        val modelSpinner = findViewById<Spinner>(R.id.modelSpinner)

        println("hello")
        var rows: List<Array<String>> = ArrayList()
        val csvReader = CSVReader(this@SetupActivity, "vehicles.csv")
        try {
            rows = csvReader.readCSV()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        for (i in rows.indices) {
            println("hello1")
            years.add(rows[i][63])
            makesArrayAdapter.add(rows[i][46])
            modelsArrayAdapter.add(rows[i][47])
        }

    println("hello2")

        years.add("Year")
        years.add("1994")
        years.add("2000")

        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        arrayAdapter.addAll(years)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = arrayAdapter



        makesArrayAdapter.add("Toyota")
        makesArrayAdapter.add("Honda")
        makesArrayAdapter.add("Acura")
        makesArrayAdapter.add("Subaru")
        makesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        makeSpinner.adapter = makesArrayAdapter



        modelsArrayAdapter.add("Camry")
        modelsArrayAdapter.add("Corolla")
        modelsArrayAdapter.add("Impreza")
        modelsArrayAdapter.add("Crosstrek")
        modelsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSpinner.adapter = modelsArrayAdapter
    }
}
