package com.example.libraryofbabelv

import java.util.regex.Pattern
import kotlin.math.sin

class LibraryOfBabel {

    class Configuration (
        var alphabet: String = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя, .",
        var digs: String = "0123456789abcdefghijklmnopqrstuvwxyz",
        var lengthOfPage: Int = 4819,
        var lengthOfTitle: Int = 31,
        var wall: Int = 5,
        var shelf: Int = 7,
        var volume: Int = 31,
        var page: Int = 421
    ) {
        fun copy() = Configuration(
            alphabet,
            digs,
            lengthOfPage,
            lengthOfTitle,
            wall,
            shelf,
            volume,
            page
        )
    }

    companion object {
        // Основная функция генерации страницы
        fun generatePage(config: Configuration): SearchResult {
            val (titleSeed, contentSeed) = createSeeds(config)
            val coords = PageCoordinates(
                config.wall,
                config.shelf,
                config.volume,
                config.page
            )
            return SearchResult(
                coordinates = coords,
                title = generateTitle(titleSeed, config),
                content = generateContent(contentSeed, config))
        }

        fun getPageByKey(
            wall: Int,
            shelf: Int,
            volume: Int,
            page: Int,
            config: Configuration
        ): SearchResult {
            val newConfig = config.copy().apply {
                this.wall = wall
                this.shelf = shelf
                this.volume = volume
                this.page = page
            }
            return generatePage(newConfig)
        }

        // Поиск по хешу
        fun findTextByHash(
            searchText: String,
            config: Configuration,
            maxAttempt: Int = 1000
        ): SearchResult? {
            val tempConfig = config.copy()
            val targetHash = searchText.hashCode()

            repeat(maxAttempt) { attempt ->
                tempConfig.page = config.page + attempt
                val result = generatePage(tempConfig)

                if (result.content.hashCode() == targetHash &&
                    result.content.contains(searchText)) {
                    return result
                }
            }
            return null
        }

        // Поиск по регулярному выражению
        fun searchByRegex(
            regex: String,
            config: Configuration,
            maxAttempt: Int = 1000
        ): SearchResult? {
            val tempConfig = config.copy()
            val pattern = Pattern.compile(regex)

            repeat(maxAttempt) { attempt ->
                tempConfig.page = config.page + attempt
                val result = generatePage(tempConfig)
                val matcher = pattern.matcher(result.content)
                if (matcher.find()) return result
            }
            return null
        }

        // Создание уникального сида
        private fun createSeeds(config: Configuration): Pair<String, String> {
            val baseSeed = "${config.wall}-${config.shelf}-${config.volume}-${config.page}"
            return Pair(
                "title:$baseSeed",
                "content:$baseSeed"
            )
        }

        private fun generateTitle(seed: String, config: Configuration): String {
            val rng = createRNG(seed)
            return buildString {
                repeat(config.lengthOfTitle) {
                    append(getRandomChar(rng(), config.alphabet))
                }
            }
        }

        private fun generateContent(seed: String, config: Configuration): String {
            val rng = createRNG(seed)
            return buildString {
                repeat(config.lengthOfPage) {
                    append(getRandomChar(rng(), config.alphabet + config.digs))
                }
            }.chunked(80).joinToString("\n")
        }

        // Детерминированный RNG
        private fun createRNG(seed: String): () -> Double {
            var state = seed.hashCode().toDouble()
            return {
                state = sin(state) * 10000
                state - state.toInt()
            }
        }

        // Получение случайного символа
        private fun getRandomChar(value: Double, charset: String): Char {
            val index = (value * charset.length).toInt()
            return  charset[Math.abs(index) % charset.length]
        }

    }

}