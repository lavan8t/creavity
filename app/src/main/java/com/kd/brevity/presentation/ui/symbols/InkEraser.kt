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
public val ink_eraser: ImageVector
  get() {
    if (_ink_eraser != null) {
      return _ink_eraser!!
    }
    _ink_eraser =
      ImageVector.Builder(
          name = "ink_eraser",
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
            moveTo(35.4f, 36.5f)
            horizontalLineTo(46.05f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(30.8f)
            lineTo(35.4f, 36.5f)
            close()
            moveTo(7.65f, 41.05f)
            lineToRelative(-4.1f, -4.2f)
            quadTo(2.18f, 35.48f, 2.24f, 33.66f)
            reflectiveQuadTo(3.55f, 30.55f)
            lineTo(26.05f, 6.45f)
            quadTo(27.28f, 5.2f, 29.17f, 5.2f)
            reflectiveQuadTo(32.4f, 6.5f)
            lineToRelative(9.45f, 9.9f)
            quadToRelative(1.3f, 1.4f, 1.35f, 3.27f)
            reflectiveQuadTo(41.9f, 22.8f)
            lineTo(24.75f, 41.05f)
            horizontalLineTo(7.65f)
            close()
            moveTo(22.99f, 36.5f)
            lineToRelative(15.76f, -17f)
            lineTo(29.2f, 9.4f)
            lineTo(6.95f, 33.68f)
            lineTo(9.6f, 36.5f)
            horizontalLineTo(22.99f)
            close()
            moveTo(24f, 24f)
            close()
          }
        }
        .build()
    return _ink_eraser!!
  }

private var _ink_eraser: ImageVector? = null

@Suppress("CheckReturnValue")
public val ink_eraser_filled: ImageVector
  get() {
    if (_ink_eraser_filled != null) {
      return _ink_eraser_filled!!
    }
    _ink_eraser_filled =
      ImageVector.Builder(
          name = "ink_eraser",
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
            moveTo(35.4f, 36.5f)
            horizontalLineTo(46.05f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(30.8f)
            lineTo(35.4f, 36.5f)
            close()
            moveTo(7.65f, 41.05f)
            lineToRelative(-4.1f, -4.2f)
            quadTo(2.2f, 35.5f, 2.25f, 33.67f)
            reflectiveQuadToRelative(1.3f, -3.12f)
            lineTo(26.05f, 6.45f)
            quadTo(27.25f, 5.2f, 29.15f, 5.2f)
            reflectiveQuadTo(32.4f, 6.5f)
            lineToRelative(9.45f, 9.9f)
            quadToRelative(1.3f, 1.4f, 1.35f, 3.27f)
            reflectiveQuadTo(41.9f, 22.8f)
            lineTo(24.75f, 41.05f)
            horizontalLineTo(7.65f)
            close()
          }
        }
        .build()
    return _ink_eraser_filled!!
  }

private var _ink_eraser_filled: ImageVector? = null
