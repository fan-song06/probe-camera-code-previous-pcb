package com.lightresearch.probecamera

import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextWithShadow(
    text: String,
    modifier: Modifier,
    maxLines: Int?,
    color: Color,
    fontSize: TextUnit?,
    textAlign: TextAlign?
) {
    Text(
        text = text,
        color = color,
        modifier = modifier,
        maxLines=maxLines ?: 1,
        fontSize=fontSize ?: 16.sp,
        textAlign = textAlign,
        style = MaterialTheme.typography.headlineSmall.copy(
            shadow = Shadow(
                color = Color(0xff000000), offset = Offset(4f, 4f), blurRadius = 8f
            )
        )
    )
}