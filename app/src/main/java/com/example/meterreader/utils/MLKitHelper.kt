package com.example.meterreader.utils

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object MLKitHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun recognizeText(bitmap: Bitmap, onResult: (String) -> Unit, onError: (Exception) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text
                // Извлекаем числовые значения (упрощённо: ищем последовательности цифр)
                val numbers = Regex("\\d+").findAll(resultText).map { it.value }.toList()
                val result = numbers.joinToString(" ")
                onResult(result)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}
