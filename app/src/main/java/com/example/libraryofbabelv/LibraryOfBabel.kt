package com.example.libraryofbabelv

import java.util.regex.Pattern
import java.util.zip.CRC32
import kotlin.math.sin

class LibraryOfBabel {

    class Configuration(
        var alphabet: String = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя, .",
        var lengthOfPage: Int = 4819,
        var lengthOfTitle: Int = 31,
        var wall: Int = 5,
        var shelf: Int = 7,
        var volume: Int = 31,
        var page: Int = 421
    ) {
        fun copy() = Configuration(
            alphabet, lengthOfPage, lengthOfTitle,
            wall, shelf, volume, page
        )
    }

    companion object {
        private const val MAX_SEARCH_ATTEMPTS = 10000

        // Основная функция генерации
        fun generatePage(coords: PageCoordinates, config: Configuration): SearchResult {
            return SearchResult(
                coordinates = coords,
                title = generateTitle(coords, config),
                content = generateContent(coords, config))
        }

        // Поиск по координатам
        fun searchByCoordinates(coords: PageCoordinates, config: Configuration): SearchResult {
            return generatePage(coords, config)
        }

        // Поиск по тексту
        fun searchByText(query: String, config: Configuration): SearchResult? {
            repeat(MAX_SEARCH_ATTEMPTS) {
                val randomCoords = generateRandomCoordinates(config)
                val page = generatePage(randomCoords, config)
                if (page.content.contains(query)) {
                    return page
                }
            }
            return null
        }

        // Поиск по регулярному выражению
        fun searchByRegex(regex: String, config: Configuration): SearchResult? {
            val pattern = Pattern.compile(regex)
            repeat(MAX_SEARCH_ATTEMPTS) {
                val randomCoords = generateRandomCoordinates(config)
                val page = generatePage(randomCoords, config)
                if (pattern.matcher(page.content).find()) {
                    return page
                }
            }
            return null
        }

        private fun generateRandomCoordinates(config: Configuration): PageCoordinates {
            return PageCoordinates(
                wall = (Math.random() * config.wall).toInt() + 1,
                shelf = (Math.random() * config.shelf).toInt() + 1,
                volume = (Math.random() * config.volume).toInt() + 1,
                page = (Math.random() * config.page).toInt() + 1
            )
        }

        private fun generateTitle(coords: PageCoordinates, config: Configuration): String {
            val seed = createSeed(coords, "title")
            val rng = createRNG(seed)
            return buildString {
                repeat(config.lengthOfTitle) {
                    append(getRandomChar(rng(), config.alphabet))
                }
            }
        }

        private fun generateContent(coords: PageCoordinates, config: Configuration): String {
            val seed = createSeed(coords, "content")
            val rng = createRNG(seed)
            return buildString {
                repeat(config.lengthOfPage) {
                    append(getRandomChar(rng(), config.alphabet))
                }
            }.chunked(80).joinToString("\n")
        }

        private fun createSeed(coords: PageCoordinates, prefix: String): String {
            return "$prefix-${coords.wall}-${coords.shelf}-${coords.volume}-${coords.page}"
        }

        private fun createRNG(seed: String): () -> Double {
            var state = seed.hashCode().toDouble()
            return {
                state = sin(state) * 10000
                state - state.toInt()
            }
        }

        private fun getRandomChar(value: Double, charset: String): Char {
            val index = (value * charset.length).toInt()
            return charset[Math.abs(index) % charset.length]
        }

        // Проверка и парсинг координат
        fun isCoordinates(input: String): Boolean {
            val parts = input.split(" ")
            if (parts.size != 4) return false

            return parts.all { part ->
                try {
                    part.toInt()
                    true
                } catch (e: NumberFormatException) {
                    false
                }
            }
        }

        private fun parseCoordinates(input: String): PageCoordinates {
            val parts = input.split(" ").map { it.toInt() }
            return PageCoordinates(parts[0], parts[1], parts[2], parts[3])
        }

        // Проверка валидности регулярного выражения
        fun isValidRegex(input: String): Boolean {
            return try {
                Pattern.compile(input)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}