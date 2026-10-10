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
public val arrow_right_alt: ImageVector
  get() {
    if (_arrow_right_alt != null) {
      return _arrow_right_alt!!
    }
    _arrow_right_alt =
      ImageVector.Builder(
          name = "arrow_right_alt",
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
            moveTo(28f, 37f)
            lineTo(24.75f, 33.85f)
            lineToRelative(7.65f, -7.7f)
            horizontalLineTo(6.95f)
            verticalLineTo(21.6f)
            horizontalLineTo(32.4f)
            lineTo(24.7f, 13.85f)
            lineTo(27.95f, 10.8f)
            lineTo(41.1f, 23.9f)
            lineTo(28f, 37f)
            close()
          }
        }
        .build()
    return _arrow_right_alt!!
  }

private var _arrow_right_alt: ImageVector? = null

@Suppress("CheckReturnValue")
public val arrow_right_alt_filled: ImageVector
  get() {
    if (_arrow_right_alt_filled != null) {
      return _arrow_right_alt_filled!!
    }
    _arrow_right_alt_filled =
      ImageVector.Builder(
          name = "arrow_right_alt",
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
            moveTo(28f, 37f)
            lineTo(24.75f, 33.85f)
            lineToRelative(7.65f, -7.7f)
            horizontalLineTo(6.95f)
            verticalLineTo(21.6f)
            horizontalLineTo(32.4f)
            lineTo(24.7f, 13.85f)
            lineTo(27.95f, 10.8f)
            lineTo(41.1f, 23.9f)
            lineTo(28f, 37f)
            close()
          }
        }
        .build()
    return _arrow_right_alt_filled!!
  }

private var _arrow_right_alt_filled: ImageVector? = null
