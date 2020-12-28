package com.example.exhaustwatch
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

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
        }

    }
}