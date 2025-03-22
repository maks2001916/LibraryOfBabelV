package com.example.libraryofbabelv

import java.util.regex.Pattern
import java.util.zip.CRC32
import kotlin.math.absoluteValue
import kotlin.math.sin
import kotlin.random.Random

class LibraryOfBabel {

    class Configuration(
        val alphabet: String = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя, .",
        val digs: String = "0123456789abcdefghijklmnopqrstuvwxyz",
        val lengthOfPage: Int = 4819,
        val lengthOfTitle: Int = 31,
        val walls: Int = 5,
        val shelves: Int = 7,
        val volumes: Int = 31,
        val pages: Int = 421
    ) {
        val alphabetIndexes = alphabet.mapIndexed { index, c -> c to index }.toMap()
        val digsIndexes = digs.mapIndexed { index, c -> c to index }.toMap()
    }

    companion object {
        private var seedState: Double = 0.0
        private const val MAX_REGEX_ATTEMPTS = 5000

        fun search(searchStr: String, config: Configuration): SearchResult {
            val coords = generateRandomCoordinates(config)
            val (title, content) = generatePageContent(coords, searchStr, config)
            return SearchResult(coords, title, content)
        }

        fun searchExactly(text: String, config: Configuration): SearchResult {
            val pos = Random.nextInt(config.lengthOfPage - text.length)
            val spacedText = " ".repeat(pos) + text +
                    " ".repeat(config.lengthOfPage - pos - text.length)
            return search(spacedText, config)
        }

        fun searchTitle(searchStr: String, config: Configuration): SearchResult {
            val coords = generateRandomCoordinates(config).copy(page = 0)
            val title = processTitle(searchStr, coords, config)
            val content = generateContent(coords, config)
            return SearchResult(coords, title, content)
        }

        fun getPageByAddress(address: String, config: Configuration): SearchResult {
            val parts = address.split("-")
            val coords = parseCoordinates(parts)
            return generatePage(coords, config)
        }

        fun getTitleByAddress(address: String, config: Configuration): SearchResult {
            val parts = address.split("-")
            val coords = parseCoordinates(parts.take(3))
            return generatePage(coords.copy(page = 0), config)
        }

        fun searchByRegex(regex: String, config: Configuration): SearchResult? {
            val pattern = try {
                Pattern.compile(regex)
            } catch (e: Exception) {
                return null
            }

            repeat(MAX_REGEX_ATTEMPTS) {
                val coords = generateRandomCoordinates(config)
                val page = generatePage(coords, config)
                if (pattern.matcher(page.content).find()) {
                    return page
                }
            }
            return null
        }

        internal fun generatePage(coords: PageCoordinates, config: Configuration): SearchResult {
            val title = generateTitle(coords, config)
            val content = generateContent(coords, config)
            return SearchResult(coords, title, content)
        }

        private fun generateRandomCoordinates(config: Configuration): PageCoordinates {
            return PageCoordinates(
                wall = (Random.nextDouble() * config.walls + 1).toInt(),
                shelf = (Random.nextDouble() * config.shelves + 1).toInt(),
                volume = (Random.nextDouble() * config.volumes + 1).toInt(),
                page = (Random.nextDouble() * config.pages + 1).toInt()
            )
        }

        private fun generatePageContent(coords: PageCoordinates, searchStr: String, config: Configuration): Pair<String, String> {
            val title = generateTitle(coords, config)
            val processedStr = addRandomPrefix(searchStr, config)
            val hexContent = processString(processedStr, config, encrypt = true)
            return title to hexContent
        }

        private fun generateTitle(coords: PageCoordinates, config: Configuration): String {
            seedState = getHash("${coords.wall}-${coords.shelf}-${coords.volume}").toDouble()
            return buildString {
                repeat(config.lengthOfTitle) {
                    append(config.alphabet[rnd(config.alphabet.length)])
                }
            }
        }

        private fun generateContent(coords: PageCoordinates, config: Configuration): String {
            seedState = getHash("${coords.wall}-${coords.shelf}-${coords.volume}-${coords.page}").toDouble()
            return buildString {
                repeat(config.lengthOfPage) {
                    append(config.digs[rnd(config.digs.length)])
                }
            }.chunked(80).joinToString("\n")
        }

        private fun processTitle(searchStr: String, coords: PageCoordinates, config: Configuration): String {
            val processed = searchStr.take(config.lengthOfTitle).padEnd(config.lengthOfTitle, ' ')
            seedState = getHash("${coords.wall}-${coords.shelf}-${coords.volume}").toDouble()
            return processString(processed, config, encrypt = true)
        }

        private fun addRandomPrefix(text: String, config: Configuration): String {
            val depth = Random.nextInt(config.lengthOfPage - text.length)
            return buildString {
                repeat(depth) {
                    append(config.alphabet[rnd(config.alphabet.length)])
                }
                append(text)
            }
        }

        private fun processString(input: String, config: Configuration, encrypt: Boolean): String {
            return buildString {
                for (c in input) {
                    val (source, target) = if (encrypt) {
                        config.alphabet to config.digs
                    } else {
                        config.digs to config.alphabet
                    }

                    val index = source.indexOf(c)
                    if (index == -1) continue

                    val rand = rnd(target.length)
                    val newIndex = (index + rand) % target.length
                    append(target[newIndex])
                }
            }
        }

        private fun parseCoordinates(parts: List<String>): PageCoordinates {
            return PageCoordinates(
                wall = parts[1].toInt(),
                shelf = parts[2].toInt(),
                volume = parts[3].toInt(),
                page = parts.getOrNull(4)?.toInt() ?: 0
            )
        }

        private fun rnd(max: Int): Int {
            val rng = createRNG()
            return (rng() * max).toInt()
        }

        private fun createRNG(): () -> Double {
            var state = seedState
            return {
                state = sin(state) * 10000
                seedState = state - state.toInt()
                seedState.absoluteValue
            }
        }

        private fun getHash(input: String): Long {
            val crc = CRC32()
            crc.update(input.toByteArray())
            return crc.value
        }

        // Проверка и парсинг координат
        fun isCoordinates(input: String): Boolean {
            return try {
                val parts = input.split(" ", "-")
                parts.size == 4 && parts.all { it.toInt() >= 0 }
            } catch (e: Exception) {
                false
            }
        }

        fun parseCoordinates(input: String): PageCoordinates {
            val parts = input.split("-").map { it.toInt() }
            return PageCoordinates(
                wall = parts[0],
                shelf = parts[1],
                volume = parts[2],
                page = parts[3]
            )
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