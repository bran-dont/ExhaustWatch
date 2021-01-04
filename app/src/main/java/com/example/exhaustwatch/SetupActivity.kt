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
import kotlinx.coroutines.selects.select
import java.io.IOException


class SetupActivity : AppCompatActivity() {

    var filteredData : HashSet<Array<String>> = HashSet()

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
        var selectedTransmission : Boolean = false
        var transmission = ""
        var exhaust : Double = 0.0

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

        populateSpinners()

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
                if (mYearSpinner.selectedItem == "Year") {
                    Toast.makeText(this@SetupActivity, "Please select a valid year for your vehicle.", Toast.LENGTH_SHORT).show()
                    return
                }
                if (mMakeSpinner.selectedItem == "Make") {
                    Toast.makeText(this@SetupActivity, "Please select a valid make for your vehicle.", Toast.LENGTH_SHORT).show()
                    return
                }
                if (mModelSpinner.selectedItem == "Model") {
                    Toast.makeText(this@SetupActivity, "Please select a valid model for your vehicle.", Toast.LENGTH_SHORT).show()
                    return
                }


                if (filteredData.size != 1) {
                    selectedTransmission = false

                    val transmissions: ArrayList<CharSequence> = ArrayList()
                    for (i in filteredData.indices) {
                        transmissions.add(filteredData.elementAt(i)[57].trim())
                    }
                    var array: Array<CharSequence> = arrayOf<CharSequence>()
                    var selectedIndex: Int = -1
                    android.app.AlertDialog.Builder(this@SetupActivity)
                            .setTitle("Transmission Type")
                            .setSingleChoiceItems(transmissions.toArray(array), 0) { dialogInterface, position ->
                                selectedIndex = position
                            }
                            .setPositiveButton("OK") { _, _ ->
                                if (selectedIndex != -1) {
                                    for (j in filteredData.indices) {
                                        try {
                                            if (filteredData.elementAt(j)[57].trim() == transmissions[selectedIndex].trim()) {
                                                try {
                                                    exhaust = filteredData.elementAt(j)[14].trim().toDouble()
                                                    selectedTransmission = true
                                                    transmission = transmissions[selectedIndex].toString()

                                                    submitInformation(transmission, exhaust)
                                                } catch (e: java.lang.NumberFormatException) {
                                                    throw Exception("CO2 Exhaust int expected, found: " + filteredData.elementAt(j)[14].trim())
                                                } catch (e: Exception) {
                                                    throw Exception("Exception found in confirming transmission, value: " + filteredData.elementAt(j)[14].trim())
                                                }
                                            }
                                        } catch (e: Exception) {
                                            println("exception: selectedIndex = $selectedIndex")
                                            println(e.toString())
                                        }
                                    }
                                } else {
                                    throw Exception("Selected transmission with index = -1")
                                }
                            }
                            .setNegativeButton("CANCEL") { dialog, which ->
                                selectedTransmission = false
                                dialog.dismiss()
                            }
                            .setNeutralButton("DON'T KNOW") { dialog, which ->
                                var average: Double = 0.0
                                for (i in filteredData.indices) {
                                    try {
                                        average += filteredData.elementAt(i)[14].trim().toDouble()
                                    } catch (e: java.lang.NumberFormatException) {
                                        throw Exception("Tried to calculate average CO2 exhaust; expected double, encountered other: " + filteredData.elementAt(i)[14].trim())
                                    } catch (e: Exception) {
                                        throw Exception("Encountered exception when calculating average CO2, at value: " + filteredData.elementAt(i)[14].trim())
                                    }

                                }
                                average /= filteredData.size
                                exhaust = average
                                selectedTransmission = true
                                transmission = "average"

                                submitInformation(transmission, exhaust)
                            }
                            .create().show()
                    Toast.makeText(this@SetupActivity, "Please select the transmission for your vehicle. " +
                            "If you don't know, that is okay; the average CO2 exhaust for your vehicle will be used instead.", Toast.LENGTH_LONG).show()
                } else {
                    selectedTransmission = true
                }

                //register user to firebase
                if (selectedTransmission && transmission != "") {
                    submitInformation(transmission, exhaust)

                    /*fAuth.createUserWithEmailAndPassword(email, password)
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
                                    user["transmission"] = transmission
                                    user["exhaust"] = exhaust
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
                    intent.putExtra("CO2", exhaust)
                    startActivity(intent)*/
                }
            }
        })
    }

    private fun submitInformation(transmission: String, exhaust: Double) {
        val mFullName = findViewById<EditText>(R.id.fullName)
        val mEmail = findViewById<EditText>(R.id.email)
        val mPassword = findViewById<EditText>(R.id.setPassword)
        val mPhone = findViewById<EditText>(R.id.phoneNumber)
        val mYearSpinner = findViewById<Spinner>(R.id.yearSpinner)
        val mMakeSpinner = findViewById<Spinner>(R.id.makeSpinner)
        val mModelSpinner = findViewById<Spinner>(R.id.modelSpinner)

        val email = mEmail.text.toString().trim()
        val password = mPassword.text.toString().trim()
        val fullName = mFullName.text.toString().trim()
        val phoneNumber = mPhone.text.toString().trim()
        val year = mYearSpinner.selectedItem
        val make = mMakeSpinner.selectedItem.toString().trim()
        val model = mModelSpinner.selectedItem.toString().trim()

        val fAuth = FirebaseAuth.getInstance()
        val fStore = FirebaseFirestore.getInstance()

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
                        user["transmission"] = transmission
                        user["exhaust"] = exhaust
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
        intent.putExtra("CO2", exhaust)
        startActivity(intent)
    }


    private fun populateSpinners() {
        val yearsArrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        val makesArrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        val modelsArrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        val years = HashSet<String>()
        val yearSpinner = findViewById<Spinner>(R.id.yearSpinner)
        val makeSpinner = findViewById<Spinner>(R.id.makeSpinner)
        val modelSpinner = findViewById<Spinner>(R.id.modelSpinner)

        yearsArrayAdapter.add("Year")
        makesArrayAdapter.add("Make")
        modelsArrayAdapter.add("Model")

        yearsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        makesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        var rows: List<Array<String>> = ArrayList()
        val csvReader = CSVReader(this@SetupActivity, "vehicles.csv")
        try {
            rows = csvReader.readCSV()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        for (i in rows.indices) {
            try {
                years.add(rows[i][63].trim())
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Error in reading years", Toast.LENGTH_SHORT).show()
            }
        }

        yearsArrayAdapter.addAll(years.toSortedSet())
        yearsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearsArrayAdapter

        makeSpinner.adapter = makesArrayAdapter
        modelSpinner.adapter = modelsArrayAdapter

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent != null && 0 <= position && position <= parent.count) {

                    if (parent.getItemAtPosition(position) != "Year") {
                        try {
                            filteredData = filterMakes(rows, parent.getItemAtPosition(position).toString().trim().toInt())
                            val listOfMakes: HashSet<String> = HashSet()
                            for (i in filteredData.indices) {
                                listOfMakes.add(filteredData.elementAt(i)[46].trim())
                            }
                            makesArrayAdapter.clear()
                            makesArrayAdapter.add("Make")
                            makesArrayAdapter.addAll(listOfMakes.toSortedSet())
                            makesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            makeSpinner.adapter = makesArrayAdapter
                        } catch (e: java.lang.NumberFormatException) {
                            Toast.makeText(this@SetupActivity, "Clicked on something that was not a number.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@SetupActivity, "Unknown bug in yearspinner select listener.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        //selected is "Year"
                        makesArrayAdapter.clear()
                        makesArrayAdapter.add("Make")
                        makeSpinner.adapter = makesArrayAdapter
                    }

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                makesArrayAdapter.clear()
                makesArrayAdapter.add("Make")
                makeSpinner.adapter = makesArrayAdapter
            }
        }

        makeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if (parent != null && 0 <= position && position <= parent.count) {
                    if(parent.getItemAtPosition(position) != "Make") {
                        filteredData = filterModels(filteredData, parent.getItemAtPosition(position).toString())
                        val listOfModels: HashSet<String> = HashSet()
                        for (i in filteredData.indices) {
                            listOfModels.add(filteredData.elementAt(i)[47])
                        }
                        modelsArrayAdapter.clear()
                        modelsArrayAdapter.add("Model")
                        modelsArrayAdapter.addAll(listOfModels.toSortedSet())
                        modelsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        modelSpinner.adapter = modelsArrayAdapter
                    }
                    else {
                        //selected is "Make"
                        modelsArrayAdapter.clear()
                        modelsArrayAdapter.add("Model")
                        modelSpinner.adapter = modelsArrayAdapter
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                modelsArrayAdapter.clear()
                modelsArrayAdapter.add("Model")
                modelSpinner.adapter = modelsArrayAdapter
            }

        }

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if (parent != null && 0 <= position && position <= parent.count) {
                    if (parent.getItemAtPosition(position) != "Model") {
                        filteredData = filterFinal(filteredData, parent.getItemAtPosition(position).toString())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

    }

    fun filterMakes(rows: List<Array<String>>, year: Int) : HashSet<Array<String>> {
        val filtered: HashSet<Array<String>> = HashSet()

        for (i in rows.indices) {
            try {
                if (rows[i][63].trim() == year.toString().trim()) {
                    filtered.add(rows[i])
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error at value: " + rows[i][63].trim().toString() + ".", Toast.LENGTH_SHORT).show()
            }
        }
        return filtered
    }

    fun filterModels(rows: Set<Array<String>>, make: String) : HashSet<Array<String>> {
        val filtered: HashSet<Array<String>> = HashSet()
        for(i in rows.indices){
            if(rows.elementAt(i)[46] == make){
                filtered.add(rows.elementAt(i))
            }
        }
        return filtered
    }

    fun filterFinal(rows: Set<Array<String>>, model: String) : HashSet<Array<String>> {
        val filtered : HashSet<Array<String>> = HashSet()
        for(i in rows.indices) {
            if(rows.elementAt(i)[47] == model) {
                filtered.add(rows.elementAt(i))
            }
        }
        return filtered
    }
}
