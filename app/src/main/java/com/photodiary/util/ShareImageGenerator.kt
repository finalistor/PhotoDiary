package com.photodiary.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File

object ShareImageGenerator {

    private const val WIDTH = 1080
    private const val PADDING = 48
    private const val PHOTO_HEIGHT = 720
    private const val WATERMARK_HEIGHT = 80

    fun generate(title: String, content: String, photoPath: String?): Bitmap {
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1a1a1a.toInt()
            textSize = 52f
        }
        val contentPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF555555.toInt()
            textSize = 36f
        }
        val watermarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF999999.toInt()
            textSize = 28f
        }

        val contentWidth = WIDTH - PADDING * 2

        // Layout title
        val titleLayout = if (title.isNotEmpty()) {
            StaticLayout.Builder.obtain(title, 0, title.length, textPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f)
                .setIncludePad(false)
                .build()
        } else null

        // Layout content (max 10 lines)
        val displayContent = if (content.length > 500) content.take(500) + "..." else content
        val contentLayout = if (displayContent.isNotEmpty()) {
            StaticLayout.Builder.obtain(displayContent, 0, displayContent.length, contentPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.3f)
                .setIncludePad(false)
                .setMaxLines(10)
                .setEllipsizedWidth(contentWidth)
                .build()
        } else null

        val watermarkLayout = StaticLayout.Builder.obtain(
            "PhotoDiary", 0, "PhotoDiary".length, watermarkPaint, contentWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(false)
            .build()

        // Calculate total height
        var totalHeight = PADDING
        if (photoPath != null) totalHeight += PHOTO_HEIGHT + PADDING
        titleLayout?.let { totalHeight += it.height + PADDING / 2 }
        contentLayout?.let { totalHeight += it.height + PADDING }
        totalHeight += WATERMARK_HEIGHT + PADDING

        val bitmap = Bitmap.createBitmap(WIDTH, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFFFFFFFF.toInt())

        var y = PADDING

        // Draw photo
        if (photoPath != null) {
            val photoBitmap = loadAndScalePhoto(photoPath, contentWidth, PHOTO_HEIGHT)
            if (photoBitmap != null) {
                val left = PADDING + (contentWidth - photoBitmap.width) / 2
                canvas.drawBitmap(photoBitmap, left.toFloat(), y.toFloat(), null)
                photoBitmap.recycle()
            }
            y += PHOTO_HEIGHT + PADDING
        }

        // Draw title
        titleLayout?.let {
            canvas.save()
            canvas.translate(PADDING.toFloat(), y.toFloat())
            it.draw(canvas)
            canvas.restore()
            y += it.height + PADDING / 2
        }

        // Draw content
        contentLayout?.let {
            canvas.save()
            canvas.translate(PADDING.toFloat(), y.toFloat())
            it.draw(canvas)
            canvas.restore()
            y += it.height + PADDING
        }

        // Draw watermark
        canvas.save()
        canvas.translate(PADDING.toFloat(), y.toFloat())
        watermarkLayout.draw(canvas)
        canvas.restore()

        return bitmap
    }

    private fun loadAndScalePhoto(path: String, maxWidth: Int, maxHeight: Int): Bitmap? {
        val file = File(path)
        if (!file.exists()) return null

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)

        val scale = maxOf(
            options.outWidth.toFloat() / maxWidth,
            options.outHeight.toFloat() / maxHeight
        )
        val sampleSize = if (scale > 1f) scale.toInt() else 1

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val decoded = BitmapFactory.decodeFile(path, decodeOptions) ?: return null

        // Scale to fit
        val ratio = minOf(
            maxWidth.toFloat() / decoded.width,
            maxHeight.toFloat() / decoded.height
        )
        val scaledWidth = (decoded.width * ratio).toInt()
        val scaledHeight = (decoded.height * ratio).toInt()

        return Bitmap.createScaledBitmap(decoded, scaledWidth, scaledHeight, true).also {
            if (it !== decoded) decoded.recycle()
        }
    }
}
