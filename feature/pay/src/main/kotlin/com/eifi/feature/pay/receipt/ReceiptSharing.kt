package com.eifi.feature.pay.receipt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.eifi.feature.pay.R
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Saves [receipt] as a PNG in the cache, then opens the share sheet with it (`receipt_paths.xml`). */
internal suspend fun shareReceipt(context: Context, receipt: Bitmap, paymentId: String) {
    val file = withContext(Dispatchers.IO) {
        val folder = File(context.cacheDir, RECEIPTS_FOLDER).apply { mkdirs() }
        File(folder, "eifi-$paymentId.png").apply {
            outputStream().use { receipt.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
        }
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.receipts", file)
    val send = Intent(Intent.ACTION_SEND)
        .setType("image/png")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, context.getString(R.string.receipt_share_title)))
}

private const val RECEIPTS_FOLDER = "receipts"

/** Ignored for PNG, which is lossless. */
private const val PNG_QUALITY = 100
