package com.kd.brevity.presentation.ui.symbols

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
          defaultWidth = 48.dp,
          defaultHeight = 48.dp,
          viewportWidth = 48f,
          viewportHeight = 48f,
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
            moveTo(12.45f, 38.7f)
            lineTo(9.3f, 35.55f)
            lineTo(20.85f, 24f)
            lineTo(9.3f, 12.5f)
            lineTo(12.45f, 9.3f)
            lineTo(24f, 20.8f)
            lineTo(35.55f, 9.3f)
            lineToRelative(3.15f, 3.2f)
            lineTo(27.2f, 24f)
            lineTo(38.7f, 35.55f)
            lineTo(35.55f, 38.7f)
            lineTo(24f, 27.2f)
            lineTo(12.45f, 38.7f)
            close()
          }
        }
        .build()
    return _close!!
  }

private var _close: ImageVector? = null

@Suppress("CheckReturnValue")
public val close_filled: ImageVector
  get() {
    if (_close_filled != null) {
      return _close_filled!!
    }
    _close_filled =
      ImageVector.Builder(
          name = "close",
          defaultWidth = 48.dp,
          defaultHeight = 48.dp,
          viewportWidth = 48f,
          viewportHeight = 48f,
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
            moveTo(12.45f, 38.7f)
            lineTo(9.3f, 35.55f)
            lineTo(20.85f, 24f)
            lineTo(9.3f, 12.5f)
            lineTo(12.45f, 9.3f)
            lineTo(24f, 20.8f)
            lineTo(35.55f, 9.3f)
            lineToRelative(3.15f, 3.2f)
            lineTo(27.2f, 24f)
            lineTo(38.7f, 35.55f)
            lineTo(35.55f, 38.7f)
            lineTo(24f, 27.2f)
            lineTo(12.45f, 38.7f)
            close()
          }
        }
        .build()
    return _close_filled!!
  }

private var _close_filled: ImageVector? = null
