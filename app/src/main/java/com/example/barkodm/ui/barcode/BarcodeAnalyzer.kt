package com.example.barkodm.ui.barcode

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeAnalyzer(private val onBarcodeDetected: (Barcode) -> Unit) : ImageAnalysis.Analyzer {
    private val isAnalyzing = AtomicBoolean(false)
    private val barcodeScanner = BarcodeScanning.getClient()

    override fun analyze(imageProxy: ImageProxy) {
        if (isAnalyzing.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            isAnalyzing.set(true)

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        // İlk bulunan barkodu işle
                        onBarcodeDetected(barcodes[0])
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
                .addOnCompleteListener {
                    isAnalyzing.set(false)
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
} 