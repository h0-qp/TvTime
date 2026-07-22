package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShareHelper {
    fun shareText(context: Context, title: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة عبر"))
    }

    suspend fun shareAsImage(context: Context, title: String, imageUrl: String, rating: String) = withContext(Dispatchers.IO) {
        try {
            // 1. Download image
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Disable hardware bitmaps for Canvas drawing
                .build()
            
            val result = loader.execute(request)
            if (result !is SuccessResult) return@withContext
            
            val originalBitmap = result.drawable.toBitmap()
            
            // 2. Create new bitmap with extra space for text
            val padding = 40
            val textHeight = 120
            val width = originalBitmap.width
            val height = originalBitmap.height + textHeight
            
            val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(newBitmap)
            
            // Draw background
            canvas.drawColor(Color.parseColor("#121212")) // Dark background
            
            // Draw image
            canvas.drawBitmap(originalBitmap, 0f, 0f, null)
            
            // Draw text
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 48f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            
            canvas.drawText(title, width / 2f, originalBitmap.height + 60f, textPaint)
            
            val ratingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700") // Gold
                textSize = 36f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("التقييم: $rating", width / 2f, originalBitmap.height + 105f, ratingPaint)
            
            // 3. Save to cache
            val imagesDir = File(context.cacheDir, "images")
            imagesDir.mkdirs()
            val imageFile = File(imagesDir, "share_image.png")
            FileOutputStream(imageFile).use { out ->
                newBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            // 4. Share
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "شاهد $title على TrackVerse!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(intent, "مشاركة كصورة"))
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
