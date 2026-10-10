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
public val arrow_back: ImageVector
  get() {
    if (_arrow_back != null) {
      return _arrow_back!!
    }
    _arrow_back =
      ImageVector.Builder(
          name = "arrow_back",
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
            moveTo(15.65f, 26.25f)
            lineTo(27.25f, 37.9f)
            lineToRelative(-3.2f, 3.15f)
            lineTo(6.95f, 23.95f)
            lineTo(24f, 6.9f)
            lineToRelative(3.25f, 3.2f)
            lineTo(15.65f, 21.7f)
            horizontalLineTo(41.1f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(15.65f)
            close()
          }
        }
        .build()
    return _arrow_back!!
  }

private var _arrow_back: ImageVector? = null

@Suppress("CheckReturnValue")
public val arrow_back_filled: ImageVector
  get() {
    if (_arrow_back_filled != null) {
      return _arrow_back_filled!!
    }
    _arrow_back_filled =
      ImageVector.Builder(
          name = "arrow_back",
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
            moveTo(15.65f, 26.25f)
            lineTo(27.25f, 37.9f)
            lineToRelative(-3.2f, 3.15f)
            lineTo(6.95f, 23.95f)
            lineTo(24f, 6.9f)
            lineToRelative(3.25f, 3.2f)
            lineTo(15.65f, 21.7f)
            horizontalLineTo(41.1f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(15.65f)
            close()
          }
        }
        .build()
    return _arrow_back_filled!!
  }

private var _arrow_back_filled: ImageVector? = null
