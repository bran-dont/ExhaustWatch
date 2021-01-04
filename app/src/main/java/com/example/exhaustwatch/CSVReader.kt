package com.example.exhaustwatch

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.jvm.Throws

class CSVReader(private var context: Context, private var fileName: String) {
    var rows: MutableList<Array<String>> = ArrayList()

    @Throws(IOException::class)
    fun readCSV(): List<Array<String>> {
        val `is`: InputStream = context.assets.open(fileName)
        val isr = InputStreamReader(`is`)
        val br = BufferedReader(isr)
        var line: String
        val csvSplitBy = ","
        //line = br.readLine()
        //line = br.readLine()
        br.forEachLine {
            val row = it.split(csvSplitBy).toTypedArray()
            rows.add(row)
            //line = br.readLine()
        }
        return rows
    }
}