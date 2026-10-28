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
public val ink_eraser: ImageVector
  get() {
    if (_ink_eraser != null) {
      return _ink_eraser!!
    }
    _ink_eraser =
      ImageVector.Builder(
          name = "ink_eraser",
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
            moveTo(18.6f, 17.7f)
            horizontalLineToRelative(3.18f)
            quadToRelative(0.65f, 0f, 1.11f, 0.46f)
            reflectiveQuadToRelative(0.46f, 1.11f)
            quadToRelative(0f, 0.65f, -0.46f, 1.11f)
            reflectiveQuadToRelative(-1.11f, 0.46f)
            horizontalLineTo(15.45f)
            lineTo(18.6f, 17.7f)
            close()
            moveTo(4.45f, 20.85f)
            quadToRelative(-0.33f, 0f, -0.6f, -0.11f)
            reflectiveQuadTo(3.35f, 20.4f)
            lineTo(1.63f, 18.68f)
            quadTo(0.7f, 17.75f, 0.7f, 16.46f)
            quadToRelative(0f, -1.29f, 0.93f, -2.24f)
            lineTo(12.4f, 3.35f)
            quadTo(13.33f, 2.4f, 14.63f, 2.41f)
            reflectiveQuadToRelative(2.23f, 0.94f)
            lineTo(21.3f, 7.8f)
            quadToRelative(0.93f, 0.92f, 0.93f, 2.22f)
            quadToRelative(0f, 1.3f, -0.93f, 2.23f)
            lineToRelative(-8.07f, 8.13f)
            quadTo(13f, 20.6f, 12.73f, 20.73f)
            reflectiveQuadToRelative(-0.6f, 0.13f)
            horizontalLineTo(4.45f)
            close()
          }
        }
        .build()
    return _ink_eraser!!
  }

private var _ink_eraser: ImageVector? = null
