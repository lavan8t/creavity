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
public val brush: ImageVector
  get() {
    if (_brush != null) {
      return _brush!!
    }
    _brush =
      ImageVector.Builder(
          name = "brush",
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
            moveTo(5.8f, 21.8f)
            quadToRelative(-1.28f, 0f, -2.64f, -0.76f)
            quadTo(1.8f, 20.27f, 0.8f, 18.73f)
            quadToRelative(0.9f, 0f, 1.6f, -0.41f)
            reflectiveQuadTo(3.2f, 16.85f)
            quadTo(3.33f, 15.5f, 4.31f, 14.54f)
            reflectiveQuadTo(6.8f, 13.58f)
            reflectiveQuadToRelative(2.56f, 1.06f)
            reflectiveQuadToRelative(1.06f, 2.56f)
            quadToRelative(0f, 1.93f, -1.35f, 3.26f)
            reflectiveQuadTo(5.8f, 21.8f)
            close()
            moveToRelative(6.45f, -6.53f)
            lineTo(8.73f, 11.75f)
            lineTo(18.83f, 1.65f)
            quadTo(19.1f, 1.38f, 19.51f, 1.36f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            lineToRelative(2.13f, 2.13f)
            quadToRelative(0.3f, 0.3f, 0.3f, 0.7f)
            quadToRelative(0f, 0.4f, -0.3f, 0.7f)
            lineToRelative(-10.1f, 10.1f)
            close()
          }
        }
        .build()
    return _brush!!
  }

private var _brush: ImageVector? = null
