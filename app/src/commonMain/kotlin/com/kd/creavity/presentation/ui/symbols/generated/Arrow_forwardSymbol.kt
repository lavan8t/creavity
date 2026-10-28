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
public val arrow_forward: ImageVector
  get() {
    if (_arrow_forward != null) {
      return _arrow_forward!!
    }
    _arrow_forward =
      ImageVector.Builder(
          name = "arrow_forward",
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
            moveTo(14.8f, 13.58f)
            horizontalLineTo(4.73f)
            quadToRelative(-0.65f, 0f, -1.11f, -0.46f)
            reflectiveQuadTo(3.15f, 12f)
            reflectiveQuadTo(3.61f, 10.89f)
            reflectiveQuadTo(4.73f, 10.43f)
            horizontalLineTo(14.8f)
            lineTo(10.88f, 6.5f)
            quadTo(10.4f, 6.02f, 10.4f, 5.38f)
            quadToRelative(0f, -0.65f, 0.47f, -1.13f)
            quadTo(11.35f, 3.8f, 12f, 3.8f)
            reflectiveQuadToRelative(1.13f, 0.48f)
            lineToRelative(6.6f, 6.6f)
            quadToRelative(0.23f, 0.22f, 0.35f, 0.52f)
            reflectiveQuadTo(20.2f, 12f)
            reflectiveQuadToRelative(-0.13f, 0.6f)
            reflectiveQuadToRelative(-0.35f, 0.53f)
            lineTo(13.1f, 19.75f)
            quadToRelative(-0.48f, 0.48f, -1.11f, 0.48f)
            reflectiveQuadTo(10.88f, 19.75f)
            reflectiveQuadTo(10.4f, 18.63f)
            reflectiveQuadTo(10.88f, 17.5f)
            lineTo(14.8f, 13.58f)
            close()
          }
        }
        .build()
    return _arrow_forward!!
  }

private var _arrow_forward: ImageVector? = null
