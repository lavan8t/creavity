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
public val arrow_back: ImageVector
  get() {
    if (_arrow_back != null) {
      return _arrow_back!!
    }
    _arrow_back =
      ImageVector.Builder(
          name = "arrow_back",
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
            moveTo(9.2f, 13.58f)
            lineToRelative(3.92f, 3.93f)
            quadToRelative(0.48f, 0.48f, 0.48f, 1.13f)
            reflectiveQuadToRelative(-0.48f, 1.13f)
            quadTo(12.65f, 20.2f, 12f, 20.2f)
            reflectiveQuadTo(10.88f, 19.73f)
            lineToRelative(-6.6f, -6.6f)
            quadTo(4.05f, 12.9f, 3.93f, 12.6f)
            reflectiveQuadTo(3.8f, 12f)
            reflectiveQuadTo(3.93f, 11.4f)
            reflectiveQuadTo(4.28f, 10.88f)
            lineTo(10.9f, 4.25f)
            quadTo(11.38f, 3.77f, 12.01f, 3.77f)
            reflectiveQuadToRelative(1.11f, 0.48f)
            quadTo(13.6f, 4.72f, 13.6f, 5.38f)
            quadToRelative(0f, 0.65f, -0.48f, 1.13f)
            lineTo(9.2f, 10.43f)
            horizontalLineTo(19.28f)
            quadToRelative(0.65f, 0f, 1.11f, 0.46f)
            reflectiveQuadTo(20.85f, 12f)
            reflectiveQuadToRelative(-0.46f, 1.11f)
            reflectiveQuadToRelative(-1.11f, 0.46f)
            horizontalLineTo(9.2f)
            close()
          }
        }
        .build()
    return _arrow_back!!
  }

private var _arrow_back: ImageVector? = null
