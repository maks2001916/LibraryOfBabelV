package com.example.libraryofbabelv

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var wsvpET: EditText
    private lateinit var searchBTN: Button
    private lateinit var titleVT: TextView
    private lateinit var textTV: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        wsvpET = findViewById(R.id.wsvpET)
        searchBTN = findViewById(R.id.searchBTN)
        titleVT = findViewById(R.id.titleTV)
        textTV = findViewById(R.id.textTV)

        searchBTN.setOnClickListener {
            processInput(wsvpET.text.toString(), LibraryOfBabel.Configuration())
        }

    }

    fun processInput(input: String, config: LibraryOfBabel.Configuration) {
        when {
            LibraryOfBabel.isCoordinates(input) -> {
                val coord = input.split(" ")
                val result = LibraryOfBabel.searchByCoordinates(
                    PageCoordinates(
                    wall = coord[0].toInt(),
                    shelf = coord[1].toInt(),
                    volume = coord[2].toInt(),
                    page = coord[3].toInt()),
                    config = config
                )
                titleVT.setText(result.title)
                textTV.setText(result.content)
            }
            LibraryOfBabel.isValidRegex(input) -> {
                val result = LibraryOfBabel.searchByRegex(input, config)
                titleVT.setText(result?.title)
                textTV.setText(result?.content)
            }
            else -> {
                val result = LibraryOfBabel.searchByText(input, config)
                titleVT.setText(result?.title)
                textTV.setText(result?.content)
            }
        }
    }
}