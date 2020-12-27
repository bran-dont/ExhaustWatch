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
        br.readLine()
        while (br.readLine().also { line = it } != null) {
            val row = line.split(csvSplitBy).toTypedArray()
            rows.add(row)
        }
        return rows
    }
}