package com.kd.creavity.presentation.ui.symbols.generated

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val close: ImageVector
  get() {
    if (_close != null) {
      return _close!!
    }
    _close =
      ImageVector.Builder(
          name = "close",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 14.2f)
            lineTo(7.5f, 18.7f)
            quadTo(7.05f, 19.15f, 6.4f, 19.15f)
            reflectiveQuadTo(5.3f, 18.7f)
            reflectiveQuadTo(4.85f, 17.6f)
            reflectiveQuadTo(5.3f, 16.5f)
            lineTo(9.8f, 12f)
            lineTo(5.3f, 7.5f)
            quadTo(4.85f, 7.05f, 4.85f, 6.4f)
            reflectiveQuadTo(5.3f, 5.3f)
            reflectiveQuadTo(6.4f, 4.85f)
            reflectiveQuadTo(7.5f, 5.3f)
            lineTo(12f, 9.8f)
            lineTo(16.5f, 5.3f)
            quadTo(16.95f, 4.85f, 17.6f, 4.85f)
            reflectiveQuadTo(18.7f, 5.3f)
            reflectiveQuadToRelative(0.45f, 1.1f)
            reflectiveQuadTo(18.7f, 7.5f)
            lineTo(14.2f, 12f)
            lineToRelative(4.5f, 4.5f)
            quadToRelative(0.45f, 0.45f, 0.45f, 1.1f)
            reflectiveQuadTo(18.7f, 18.7f)
            reflectiveQuadToRelative(-1.1f, 0.45f)
            reflectiveQuadTo(16.5f, 18.7f)
            lineTo(12f, 14.2f)
            close()
          }
        }
        .build()
    return _close!!
  }

private var _close: ImageVector? = null
