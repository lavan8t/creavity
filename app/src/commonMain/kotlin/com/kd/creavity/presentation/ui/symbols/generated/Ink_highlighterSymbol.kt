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
public val ink_highlighter: ImageVector
  get() {
    if (_ink_highlighter != null) {
      return _ink_highlighter!!
    }
    _ink_highlighter =
      ImageVector.Builder(
          name = "ink_highlighter",
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
            moveTo(8.95f, 8.92f)
            lineToRelative(6.92f, 6.95f)
            lineToRelative(-5.1f, 5.1f)
            quadToRelative(-0.9f, 0.9f, -2.21f, 0.9f)
            reflectiveQuadTo(6.35f, 20.98f)
            quadTo(5.9f, 21.4f, 5.34f, 21.64f)
            reflectiveQuadTo(4.15f, 21.88f)
            horizontalLineTo(2.33f)
            quadTo(1.8f, 21.88f, 1.6f, 21.39f)
            reflectiveQuadTo(1.78f, 20.53f)
            lineTo(3.85f, 18.48f)
            quadTo(2.93f, 17.55f, 2.91f, 16.26f)
            quadTo(2.9f, 14.98f, 3.83f, 14.05f)
            lineTo(8.95f, 8.92f)
            close()
            moveTo(10.4f, 7.47f)
            lineToRelative(4.8f, -4.8f)
            quadTo(16.15f, 1.72f, 17.43f, 1.72f)
            reflectiveQuadToRelative(2.23f, 0.95f)
            lineToRelative(2.47f, 2.5f)
            quadToRelative(0.95f, 0.95f, 0.95f, 2.22f)
            reflectiveQuadTo(22.13f, 9.63f)
            lineToRelative(-4.8f, 4.8f)
            lineTo(10.4f, 7.47f)
            close()
          }
        }
        .build()
    return _ink_highlighter!!
  }

private var _ink_highlighter: ImageVector? = null
