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
public val check: ImageVector
  get() {
    if (_check != null) {
      return _check!!
    }
    _check =
      ImageVector.Builder(
          name = "check",
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
            moveTo(18.9f, 36.75f)
            lineTo(6.65f, 24.5f)
            lineToRelative(3.3f, -3.3f)
            lineToRelative(8.95f, 9f)
            lineTo(38f, 11.1f)
            lineToRelative(3.3f, 3.25f)
            lineTo(18.9f, 36.75f)
            close()
          }
        }
        .build()
    return _check!!
  }

private var _check: ImageVector? = null

@Suppress("CheckReturnValue")
public val check_filled: ImageVector
  get() {
    if (_check_filled != null) {
      return _check_filled!!
    }
    _check_filled =
      ImageVector.Builder(
          name = "check",
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
            moveTo(18.9f, 36.75f)
            lineTo(6.65f, 24.5f)
            lineToRelative(3.3f, -3.3f)
            lineToRelative(8.95f, 9f)
            lineTo(38f, 11.1f)
            lineToRelative(3.3f, 3.25f)
            lineTo(18.9f, 36.75f)
            close()
          }
        }
        .build()
    return _check_filled!!
  }

private var _check_filled: ImageVector? = null
