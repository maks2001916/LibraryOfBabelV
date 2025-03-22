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
                val coord = input.replace(" ", "-")
                try {
                    val coords = LibraryOfBabel.parseCoordinates(coord)
                    val result = LibraryOfBabel.generatePage(coords, config)
                    titleVT.setText(result.title)
                    textTV.setText(result.content)
                } catch (e: Exception) {
                    titleVT.setText("Ошибка координат")
                    textTV.setText("Некорректный формат координат: $coord")
                }
            }

            LibraryOfBabel.isValidRegex(input) -> {
                val result = LibraryOfBabel.searchByRegex(input, config)
                if (result != null) {
                    titleVT.setText(result.title)
                    textTV.setText(result.content)
                } else {
                    titleVT.setText("Не найдено")
                    textTV.setText("Совпадений с регулярным выражением не найдено")
                }
            }

            else -> {
                try {
                    val result = LibraryOfBabel.searchExactly(input, config)
                    titleVT.setText(result.title)
                    textTV.setText(result.content)
                } catch (e: Exception) {
                    titleVT.setText("Ошибка поиска")
                    textTV.setText("Невозможно выполнить поиск для: $input")
                }
            }
        }
    }
}