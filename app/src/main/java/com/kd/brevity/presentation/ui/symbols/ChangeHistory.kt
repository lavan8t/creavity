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
public val change_history: ImageVector
  get() {
    if (_change_history != null) {
      return _change_history!!
    }
    _change_history =
      ImageVector.Builder(
          name = "change_history",
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
            moveTo(2f, 41.05f)
            lineTo(24.05f, 5.85f)
            lineTo(46f, 41.05f)
            horizontalLineTo(2f)
            close()
            moveTo(10.15f, 36.5f)
            horizontalLineToRelative(27.7f)
            lineTo(24.05f, 14.6f)
            lineTo(10.15f, 36.5f)
            close()
            moveTo(24f, 25.5f)
            close()
          }
        }
        .build()
    return _change_history!!
  }

private var _change_history: ImageVector? = null

@Suppress("CheckReturnValue")
public val change_history_filled: ImageVector
  get() {
    if (_change_history_filled != null) {
      return _change_history_filled!!
    }
    _change_history_filled =
      ImageVector.Builder(
          name = "change_history",
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
            moveTo(2f, 41.05f)
            lineTo(24.05f, 5.85f)
            lineTo(46f, 41.05f)
            horizontalLineTo(2f)
            close()
          }
        }
        .build()
    return _change_history_filled!!
  }

private var _change_history_filled: ImageVector? = null
