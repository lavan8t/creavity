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
public val slideshow: ImageVector
  get() {
    if (_slideshow != null) {
      return _slideshow!!
    }
    _slideshow =
      ImageVector.Builder(
          name = "slideshow",
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
            moveTo(15.83f, 12.68f)
            quadTo(16.18f, 12.45f, 16.18f, 12f)
            reflectiveQuadTo(15.83f, 11.33f)
            lineToRelative(-5.3f, -3.4f)
            quadTo(10.13f, 7.68f, 9.71f, 7.9f)
            quadTo(9.3f, 8.13f, 9.3f, 8.6f)
            verticalLineToRelative(6.8f)
            quadToRelative(0f, 0.47f, 0.41f, 0.7f)
            reflectiveQuadToRelative(0.81f, -0.03f)
            lineToRelative(5.3f, -3.4f)
            close()
            moveTo(5.3f, 21.85f)
            quadToRelative(-1.33f, 0f, -2.24f, -0.91f)
            reflectiveQuadTo(2.15f, 18.7f)
            verticalLineTo(5.3f)
            quadToRelative(0f, -1.33f, 0.91f, -2.24f)
            reflectiveQuadTo(5.3f, 2.15f)
            horizontalLineTo(18.7f)
            quadToRelative(1.33f, 0f, 2.24f, 0.91f)
            quadToRelative(0.91f, 0.91f, 0.91f, 2.24f)
            verticalLineTo(18.7f)
            quadToRelative(0f, 1.32f, -0.91f, 2.24f)
            quadToRelative(-0.91f, 0.91f, -2.24f, 0.91f)
            horizontalLineTo(5.3f)
            close()
          }
        }
        .build()
    return _slideshow!!
  }

private var _slideshow: ImageVector? = null
