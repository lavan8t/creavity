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
public val rectangle: ImageVector
  get() {
    if (_rectangle != null) {
      return _rectangle!!
    }
    _rectangle =
      ImageVector.Builder(
          name = "rectangle",
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
            moveTo(2.95f, 41.05f)
            verticalLineTo(6.9f)
            horizontalLineTo(45.1f)
            verticalLineTo(41.05f)
            horizontalLineTo(2.95f)
            close()
            moveTo(7.5f, 36.5f)
            horizontalLineToRelative(33f)
            verticalLineToRelative(-25f)
            horizontalLineTo(7.5f)
            verticalLineToRelative(25f)
            close()
            moveToRelative(0f, 0f)
            verticalLineToRelative(-25f)
            verticalLineToRelative(25f)
            close()
          }
        }
        .build()
    return _rectangle!!
  }

private var _rectangle: ImageVector? = null

@Suppress("CheckReturnValue")
public val rectangle_filled: ImageVector
  get() {
    if (_rectangle_filled != null) {
      return _rectangle_filled!!
    }
    _rectangle_filled =
      ImageVector.Builder(
          name = "rectangle",
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
            moveTo(2.95f, 41.05f)
            verticalLineTo(6.9f)
            horizontalLineTo(45.1f)
            verticalLineTo(41.05f)
            horizontalLineTo(2.95f)
            close()
          }
        }
        .build()
    return _rectangle_filled!!
  }

private var _rectangle_filled: ImageVector? = null
