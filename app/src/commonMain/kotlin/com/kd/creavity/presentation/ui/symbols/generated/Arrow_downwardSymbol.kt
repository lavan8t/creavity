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
public val arrow_downward: ImageVector
  get() {
    if (_arrow_downward != null) {
      return _arrow_downward!!
    }
    _arrow_downward =
      ImageVector.Builder(
          name = "arrow_downward",
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
            moveTo(10.43f, 14.8f)
            verticalLineTo(4.72f)
            quadToRelative(0f, -0.65f, 0.46f, -1.11f)
            reflectiveQuadTo(12f, 3.15f)
            reflectiveQuadToRelative(1.11f, 0.46f)
            reflectiveQuadToRelative(0.46f, 1.11f)
            verticalLineTo(14.8f)
            lineTo(17.5f, 10.88f)
            quadTo(17.98f, 10.4f, 18.63f, 10.4f)
            quadToRelative(0.65f, 0f, 1.13f, 0.48f)
            quadTo(20.2f, 11.35f, 20.2f, 12f)
            reflectiveQuadToRelative(-0.48f, 1.13f)
            lineToRelative(-6.6f, 6.6f)
            quadTo(12.9f, 19.95f, 12.6f, 20.08f)
            reflectiveQuadTo(12f, 20.2f)
            reflectiveQuadTo(11.4f, 20.08f)
            reflectiveQuadTo(10.88f, 19.73f)
            lineTo(4.25f, 13.1f)
            quadTo(3.78f, 12.63f, 3.78f, 11.99f)
            reflectiveQuadTo(4.25f, 10.88f)
            quadTo(4.73f, 10.4f, 5.38f, 10.4f)
            reflectiveQuadTo(6.5f, 10.88f)
            lineToRelative(3.93f, 3.92f)
            close()
          }
        }
        .build()
    return _arrow_downward!!
  }

private var _arrow_downward: ImageVector? = null
