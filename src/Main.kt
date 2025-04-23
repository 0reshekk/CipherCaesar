import java.io.File
import java.io.IOException
import kotlin.math.abs

fun shiftedChar(base: Char, char: Char, shift: Int): Char {
    val alphabetSize = 32
    val offset = (char - base + shift).mod(alphabetSize)
    return base + offset
}

fun encrypt(text: String, shift: Int): String {
    return text.map { char ->
        when {
            char in 'а'..'я' -> shiftedChar('а', char, shift)
            char in 'А'..'Я' -> shiftedChar('А', char, shift)
            else -> char
        }
    }.joinToString("")
}

fun decrypt(text: String, shift: Int): String = encrypt(text, -shift)

fun calculateFrequency(text: String): Map<Char, Double> {
    val filteredText = text.filter { it.isLetter() && (it in 'а'..'я' || it in 'А'..'Я') }
    val totalLetters = filteredText.length
    if (totalLetters == 0) return emptyMap()

    val frequencyMap = mutableMapOf<Char, Int>()
    filteredText.forEach { char ->
        val lowerChar = char.lowercaseChar()
        frequencyMap[lowerChar] = frequencyMap.getOrDefault(lowerChar, 0) + 1
    }
    return frequencyMap.mapValues { (_, count) -> count.toDouble() / totalLetters }
}

fun compareFrequencies(freq1: Map<Char, Double>, freq2: Map<Char, Double>): Double {
    return freq1.keys.sumOf { char -> abs(freq1.getOrDefault(char, 0.0) - freq2.getOrDefault(char, 0.0)) }
}

fun bruteForceDecrypt(encryptedText: String, exampleText: String? = null): Pair<Int, String>? {
    val exampleFrequency = exampleText?.let { calculateFrequency(it) }
    var bestShift = 0
    var bestMatch = Double.MAX_VALUE
    val decryptedTexts = mutableListOf<Pair<Int, String>>()

    // Перебираем сдвиги от -31 до 31
    for (shift in -31..31) {
        val decryptedText = decrypt(encryptedText, shift)
        decryptedTexts.add(shift to decryptedText)

        if (exampleFrequency != null) {
            val decryptedFrequency = calculateFrequency(decryptedText)
            val match = compareFrequencies(decryptedFrequency, exampleFrequency)
            if (match < bestMatch) {
                bestMatch = match
                bestShift = shift
            }
        }
    }

    if (exampleFrequency != null) {
        return bestShift to decrypt(encryptedText, bestShift)
    } else {
        println("Все возможные варианты расшифровки:")
        decryptedTexts.forEach { (shift, text) ->
            println("Сдвиг $shift: $text")
        }

        println("Введите номер сдвига (-31..31), который кажется правильным: ")
        val selectedShift = readlnOrNull()?.toIntOrNull()
        return if (selectedShift != null && selectedShift in -31..31) {
            selectedShift to decrypt(encryptedText, selectedShift)
        } else {
            null
        }
    }
}

fun readShift(prompt: String = "Введите ключ шифрования (сдвиг от -31 до 31): "): Int {
    while (true) {
        print(prompt)
        val input = readlnOrNull()
        val shift = input?.toIntOrNull()
        if (shift != null && shift in -31..31) return shift
        println("Некорректный ввод. Пожалуйста, введите целое число от -31 до 31.")
    }
}

fun main() {
    try {
        println(
            "Выберите режим работы:\n" +
                    "1. Шифрование текста \n2. Расшифровка текста с известным ключом\n" +
                    "3. Расшифровка методом перебора"
        )

        when (readlnOrNull()?.toIntOrNull()) {
            1 -> { // Шифрование текста
                print("Введите путь к файлу с текстом для шифрования: ")
                val inputFile = readlnOrNull()?.trim() ?: ""
                print("Введите путь к файлу для сохранения зашифрованного текста: ")
                val outputFile = readlnOrNull()?.trim() ?: ""
                val shift = readShift()

                try {
                    if (File(inputFile).exists()) {
                        val text = File(inputFile).readText()
                        val encryptedText = encrypt(text, shift)
                        File(outputFile).writeText(encryptedText)
                        println("Текст успешно зашифрован и сохранён в $outputFile")
                    } else {
                        println("Файл не найден: $inputFile")
                    }
                } catch (e: IOException) {
                    println("Ошибка ввода-вывода: ${e.message}")
                }
            }
            2 -> { // Расшифровка текста с известным ключом
                print("Введите путь к файлу с зашифрованным текстом: ")
                val inputFile = readlnOrNull()?.trim() ?: ""
                print("Введите путь к файлу для сохранения расшифрованного текста: ")
                val outputFile = readlnOrNull()?.trim() ?: ""
                val shift = readShift()

                try {
                    if (File(inputFile).exists()) {
                        val text = File(inputFile).readText()
                        val decryptedText = decrypt(text, shift)
                        File(outputFile).writeText(decryptedText)
                        println("Текст успешно расшифрован и сохранён в $outputFile")
                    } else {
                        println("Файл не найден: $inputFile")
                    }
                } catch (e: IOException) {
                    println("Ошибка ввода-вывода: ${e.message}")
                }
            }
            3 -> { // Расшифровка методом перебора (brute force)
                print("Введите путь к файлу с зашифрованным текстом: ")
                val inputFile = readlnOrNull()?.trim() ?: ""
                print("Введите путь к файлу для сохранения расшифрованного текста: ")
                val outputFile = readlnOrNull()?.trim() ?: ""
                print("Введите путь к файлу с примером текста (опционально, можно оставить пустым): ")
                val exampleFile = readlnOrNull()?.trim() ?: ""

                try {
                    if (File(inputFile).exists()) {
                        val encryptedText = File(inputFile).readText()
                        val exampleText = if (exampleFile.isNotEmpty() && File(exampleFile).exists()) {
                            File(exampleFile).readText()
                        } else {
                            null
                        }

                        val result = bruteForceDecrypt(encryptedText, exampleText)
                        if (result != null) {
                            val (bestShift, decryptedText) = result
                            File(outputFile).writeText(decryptedText)
                            println("Текст успешно расшифрован с ключом $bestShift и сохранён в $outputFile")
                            println("Расшифрованный текст:\n$decryptedText")
                        } else {
                            println("Не удалось выбрать правильный сдвиг.")
                        }
                    } else {
                        println("Файл не найден: $inputFile")
                    }
                } catch (e: IOException) {
                    println("Ошибка ввода-вывода: ${e.message}")
                }
            }
            else -> println("Неверный выбор.")
        }
    } catch (e: Exception) {
        println("Произошла ошибка: ${e.message}")
    }
}