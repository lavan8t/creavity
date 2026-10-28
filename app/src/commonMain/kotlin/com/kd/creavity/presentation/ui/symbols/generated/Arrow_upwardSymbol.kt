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
public val arrow_upward: ImageVector
  get() {
    if (_arrow_upward != null) {
      return _arrow_upward!!
    }
    _arrow_upward =
      ImageVector.Builder(
          name = "arrow_upward",
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
            moveTo(10.43f, 9.2f)
            lineTo(6.5f, 13.13f)
            quadTo(6.03f, 13.6f, 5.38f, 13.6f)
            quadToRelative(-0.65f, 0f, -1.13f, -0.47f)
            quadTo(3.8f, 12.65f, 3.8f, 12f)
            reflectiveQuadTo(4.28f, 10.88f)
            lineToRelative(6.6f, -6.6f)
            quadTo(11.1f, 4.05f, 11.4f, 3.92f)
            reflectiveQuadTo(12f, 3.8f)
            reflectiveQuadToRelative(0.6f, 0.13f)
            reflectiveQuadToRelative(0.52f, 0.35f)
            lineToRelative(6.63f, 6.63f)
            quadToRelative(0.48f, 0.48f, 0.48f, 1.11f)
            reflectiveQuadToRelative(-0.48f, 1.11f)
            quadTo(19.28f, 13.6f, 18.63f, 13.6f)
            quadToRelative(-0.65f, 0f, -1.13f, -0.47f)
            lineTo(13.58f, 9.2f)
            verticalLineTo(19.27f)
            quadToRelative(0f, 0.65f, -0.46f, 1.11f)
            reflectiveQuadTo(12f, 20.85f)
            reflectiveQuadTo(10.89f, 20.39f)
            quadTo(10.43f, 19.93f, 10.43f, 19.27f)
            verticalLineTo(9.2f)
            close()
          }
        }
        .build()
    return _arrow_upward!!
  }

private var _arrow_upward: ImageVector? = null
