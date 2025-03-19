package com.example.libraryofbabelv

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var wallET: EditText
    private lateinit var shelfET: EditText
    private lateinit var volumeET: EditText
    private lateinit var pageET: EditText
    private lateinit var searchBTN: Button
    private lateinit var countCharsInLinesET: EditText
    private lateinit var titleVT: TextView
    private lateinit var textTV: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        wallET = findViewById(R.id.wallET)
        shelfET = findViewById(R.id.shelfET)
        volumeET = findViewById(R.id.volumeET)
        pageET = findViewById(R.id.pageET)
        searchBTN = findViewById(R.id.searchBTN)
        countCharsInLinesET = findViewById(R.id.countCharsInLinesET)
        titleVT = findViewById(R.id.titleTV)
        textTV = findViewById(R.id.textTV)

        searchBTN.setOnClickListener {
            val wall = wallET.text.toString().toInt()
            val shelf = shelfET.text.toString().toInt()
            val volume = volumeET.text.toString().toInt()
            val page = pageET.text.toString().toInt()
            val config = LibraryOfBabel.Configuration()
            val (title, content) = LibraryOfBabel.getPageByKey(wall, shelf, volume, page, config)
            titleVT.setText(title)
            textTV.setText(content)

        }

    }
}