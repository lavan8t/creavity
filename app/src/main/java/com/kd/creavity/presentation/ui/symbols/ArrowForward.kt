package com.kd.creavity.presentation.ui.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val arrow_forward: ImageVector
  get() {
    if (_arrow_forward != null) {
      return _arrow_forward!!
    }
    _arrow_forward =
      ImageVector.Builder(
          name = "arrow_forward",
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
            moveTo(32.35f, 26.25f)
            horizontalLineTo(6.95f)
            verticalLineTo(21.7f)
            horizontalLineToRelative(25.4f)
            lineTo(20.75f, 10.1f)
            lineToRelative(3.3f, -3.2f)
            lineTo(41.1f, 23.95f)
            lineTo(24f, 41.05f)
            lineTo(20.75f, 37.9f)
            lineTo(32.35f, 26.25f)
            close()
          }
        }
        .build()
    return _arrow_forward!!
  }

private var _arrow_forward: ImageVector? = null

@Suppress("CheckReturnValue")
public val arrow_forward_filled: ImageVector
  get() {
    if (_arrow_forward_filled != null) {
      return _arrow_forward_filled!!
    }
    _arrow_forward_filled =
      ImageVector.Builder(
          name = "arrow_forward",
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
            moveTo(32.35f, 26.25f)
            horizontalLineTo(6.95f)
            verticalLineTo(21.7f)
            horizontalLineToRelative(25.4f)
            lineTo(20.75f, 10.1f)
            lineToRelative(3.3f, -3.2f)
            lineTo(41.1f, 23.95f)
            lineTo(24f, 41.05f)
            lineTo(20.75f, 37.9f)
            lineTo(32.35f, 26.25f)
            close()
          }
        }
        .build()
    return _arrow_forward_filled!!
  }

private var _arrow_forward_filled: ImageVector? = null
