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
public val slideshow: ImageVector
  get() {
    if (_slideshow != null) {
      return _slideshow!!
    }
    _slideshow =
      ImageVector.Builder(
          name = "slideshow",
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
            moveTo(19.15f, 32.8f)
            lineTo(32.8f, 24f)
            lineTo(19.15f, 15.15f)
            verticalLineTo(32.8f)
            close()
            moveTo(9.45f, 43.1f)
            quadToRelative(-1.86f, 0f, -3.2f, -1.35f)
            reflectiveQuadTo(4.9f, 38.55f)
            verticalLineTo(9.45f)
            quadTo(4.9f, 7.57f, 6.25f, 6.21f)
            reflectiveQuadTo(9.45f, 4.85f)
            horizontalLineToRelative(29.1f)
            quadToRelative(1.88f, 0f, 3.24f, 1.36f)
            reflectiveQuadToRelative(1.36f, 3.24f)
            verticalLineToRelative(29.1f)
            quadToRelative(0f, 1.86f, -1.36f, 3.2f)
            reflectiveQuadTo(38.55f, 43.1f)
            horizontalLineTo(9.45f)
            close()
            moveToRelative(0f, -4.55f)
            horizontalLineToRelative(29.1f)
            verticalLineTo(9.45f)
            horizontalLineTo(9.45f)
            verticalLineToRelative(29.1f)
            close()
            moveToRelative(0f, -29.1f)
            verticalLineToRelative(29.1f)
            verticalLineTo(9.45f)
            close()
          }
        }
        .build()
    return _slideshow!!
  }

private var _slideshow: ImageVector? = null

@Suppress("CheckReturnValue")
public val slideshow_filled: ImageVector
  get() {
    if (_slideshow_filled != null) {
      return _slideshow_filled!!
    }
    _slideshow_filled =
      ImageVector.Builder(
          name = "slideshow",
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
            moveTo(19.15f, 32.8f)
            lineTo(32.8f, 24f)
            lineTo(19.15f, 15.15f)
            verticalLineTo(32.8f)
            close()
            moveTo(9.45f, 43.1f)
            quadToRelative(-1.86f, 0f, -3.2f, -1.35f)
            reflectiveQuadTo(4.9f, 38.55f)
            verticalLineTo(9.45f)
            quadTo(4.9f, 7.57f, 6.25f, 6.21f)
            reflectiveQuadTo(9.45f, 4.85f)
            horizontalLineToRelative(29.1f)
            quadToRelative(1.88f, 0f, 3.24f, 1.36f)
            reflectiveQuadToRelative(1.36f, 3.24f)
            verticalLineToRelative(29.1f)
            quadToRelative(0f, 1.86f, -1.36f, 3.2f)
            reflectiveQuadTo(38.55f, 43.1f)
            horizontalLineTo(9.45f)
            close()
          }
        }
        .build()
    return _slideshow_filled!!
  }

private var _slideshow_filled: ImageVector? = null
