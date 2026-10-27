package com.eifi.core.design.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eifi.core.design.R
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import kotlin.math.floor

/**
 * QR code of [content], dark on white with its quiet zone, and [icon] on a primary tile in the centre.
 * Error correction Q restores the modules hidden by the icon (see EIFI-020).
 */
@Composable
fun EifiQrCode(
    content: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: Painter? = painterResource(R.drawable.ic_eifi_contactless),
) {
    val qrCode = remember(content) { encodeQrCode(content) }
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val matrix = qrCode.matrix
            val modules = matrix.width + 2 * QUIET_ZONE_MODULES
            // Whole pixels per module, so that no seam shows between two dark modules.
            val module = floor(size.minDimension / modules)
            val origin = (size.minDimension - module * modules) / 2 + module * QUIET_ZONE_MODULES
            drawRect(color = EifiColors.Surface)
            for (y in 0 until matrix.height) {
                for (x in 0 until matrix.width) {
                    if (matrix[x, y] == DARK) {
                        drawRect(
                            color = EifiColors.Ink,
                            topLeft = Offset(origin + x * module, origin + y * module),
                            size = Size(module, module),
                        )
                    }
                }
            }
        }
        if (icon != null) {
            // White ring, so that the tile does not touch the modules around it.
            Surface(shape = EifiRadius.Card, color = EifiColors.Surface) {
                EifiIconTile(
                    icon = icon,
                    containerColor = EifiColors.Primary,
                    contentColor = EifiColors.OnPrimary,
                    size = maxWidth * ICON_FRACTION,
                    modifier = Modifier.padding(ICON_RING),
                )
            }
        }
    }
}

/** QR code of [content] with error correction Q; the version is the smallest that fits. */
internal fun encodeQrCode(content: String): QRCode = Encoder.encode(content, ErrorCorrectionLevel.Q)

/** Value of a dark module in [QRCode.getMatrix]. */
private const val DARK: Byte = 1

/** Light border required around the code by the QR specification. */
private const val QUIET_ZONE_MODULES = 4

/** Width of the icon tile relative to the code: about 4% of its area, well below the 25% that Q restores. */
private const val ICON_FRACTION = 0.2f

private val ICON_RING = 4.dp

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun EifiQrCodePreview() {
    EifiTheme {
        EifiQrCode(
            content = "https://pay.eifi.app/p#v1.CgsMDQ4PaSB2gHnEEwhCb3V0aXF1ZQERBkgpE",
            contentDescription = "QR code",
            modifier = Modifier.fillMaxWidth().padding(EifiSpacing.Gutter),
        )
    }
}
