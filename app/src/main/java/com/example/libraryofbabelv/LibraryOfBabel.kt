package com.example.libraryofbabelv

import java.util.regex.Pattern
import java.util.zip.CRC32
import kotlin.math.sin

class LibraryOfBabel {

    class Configuration(
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
            alphabet, digs, lengthOfPage, lengthOfTitle,
            wall, shelf, volume, page
        )
    }

    companion object {
        private var seed: Long = 0L

        // Основная функция генерации страницы
        fun generatePage(coords: PageCoordinates, config: Configuration): SearchResult {
            return SearchResult(
                coordinates = coords,
                title = generateTitle(coords, config),
                content = generateContent(coords, config)
            )
        }

        // Поиск по координатам
        fun getPageByCoordinates(coords: PageCoordinates, config: Configuration): SearchResult {
            return generatePage(coords, config)
        }

        // Поиск по hex-ключу
        fun getPageByHexAddress(hexAddress: String, config: Configuration): SearchResult {
            val parts = hexAddress.split("-")
            val coords = PageCoordinates(
                parts[1].toInt(),
                parts[2].toInt(),
                parts[3].toInt(),
                parts[4].toInt()
            )
            return generatePage(coords, config)
        }

        // Поиск по тексту (оригинальная логика)
        fun search(searchText: String, config: Configuration): SearchResult {
            val wall = (Math.random() * config.wall).toInt() + 1
            val shelf = (Math.random() * config.shelf).toInt() + 1
            val volume = (Math.random() * config.volume).toInt() + 1
            val page = (Math.random() * config.page).toInt() + 1

            val coords = PageCoordinates(wall, shelf, volume, page)
            val locHash = getHash("$wall$shelf$volume$page")

            seed = locHash
            val depth = (Math.random() * (config.lengthOfPage - searchText.length)).toInt()

            var processedText = searchText
            repeat(depth) {
                processedText = config.alphabet[rnd(0, config.alphabet.length)] + processedText
            }

            var hex = ""
            for (c in processedText) {
                val index = config.alphabet.indexOf(c).takeIf { it != -1 } ?: continue
                val rand = rnd(0, config.alphabet.length)
                val newIndex = mod(index + rand, config.digs.length)
                hex += config.digs[newIndex]
            }

            return generatePage(coords, config).copy(
                content = hex + generateRemainingContent(hex.length, config)
            )
        }

        // Поиск по регулярному выражению
        fun searchByRegex(regex: String, config: Configuration, maxAttempts: Int = 1000): SearchResult? {
            val pattern = Pattern.compile(regex)
            repeat(maxAttempts) {
                val coords = generateRandomCoordinates(config)
                val page = generatePage(coords, config)
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
            val locHash = getHash("${coords.wall}${coords.shelf}${pad(coords.volume, 2)}")
            seed = locHash
            return buildString {
                repeat(config.lengthOfTitle) {
                    append(config.alphabet[rnd(0, config.alphabet.length)])
                }
            }
        }

        private fun generateContent(coords: PageCoordinates, config: Configuration): String {
            val locHash = getHash("${coords.wall}${coords.shelf}${pad(coords.volume, 2)}${pad(coords.page, 3)}")
            seed = locHash
            return buildString {
                repeat(config.lengthOfPage) {
                    append(config.digs[rnd(0, config.digs.length)])
                }
            }.chunked(80).joinToString("\n")
        }

        private fun generateRemainingContent(length: Int, config: Configuration): String {
            return buildString {
                repeat(config.lengthOfPage - length) {
                    append(config.digs[rnd(0, config.digs.length)])
                }
            }
        }
        fun toHexAddress(config: Configuration, result: SearchResult ): String {
            val hex = generateHex(result.coordinates, config)
            return "${hex}-${result.coordinates.wall}-${result.coordinates.shelf}-${result.coordinates.volume}-${result.coordinates.page}"
        }

        private fun generateHex(coords: PageCoordinates, config: Configuration): String {
            val locHash = getHash("${coords.wall}${coords.shelf}${coords.volume}${coords.page}")
            seed = locHash

            return buildString {
                repeat(40) {
                    val rand = rnd(0, config.digs.length)
                    append(config.digs[mod(rand, config.digs.length)])
                }
            }
        }
        private fun pad(number: Int, length: Int): String {
            return number.toString().padStart(length, '0')
        }

        private fun getHash(input: String): Long {
            val crc = CRC32()
            crc.update(input.toByteArray())
            return crc.value
        }

        private fun rnd(min: Int, max: Int): Int {
            val rng = createRNG(seed.toString())
            return (rng() * (max - min)).toInt() + min
        }

        private fun mod(a: Int, b: Int): Int = (a % b + b) % b

        private fun createRNG(seed: String): () -> Double {
            var state = seed.hashCode().toDouble()
            return {
                state = sin(state) * 10000
                state - state.toInt()
            }
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