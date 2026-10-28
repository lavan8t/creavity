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
public val arrow_right_alt: ImageVector
  get() {
    if (_arrow_right_alt != null) {
      return _arrow_right_alt!!
    }
    _arrow_right_alt =
      ImageVector.Builder(
          name = "arrow_right_alt",
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
            moveTo(14.85f, 13.58f)
            horizontalLineTo(4.73f)
            quadToRelative(-0.65f, 0f, -1.11f, -0.46f)
            reflectiveQuadTo(3.15f, 12f)
            reflectiveQuadTo(3.61f, 10.89f)
            reflectiveQuadTo(4.73f, 10.43f)
            horizontalLineTo(14.85f)
            lineTo(12.93f, 8.5f)
            quadTo(12.48f, 8.05f, 12.48f, 7.4f)
            reflectiveQuadTo(12.93f, 6.3f)
            reflectiveQuadToRelative(1.1f, -0.45f)
            reflectiveQuadToRelative(1.1f, 0.45f)
            lineToRelative(4.6f, 4.57f)
            quadToRelative(0.23f, 0.22f, 0.35f, 0.52f)
            reflectiveQuadTo(20.2f, 12f)
            reflectiveQuadToRelative(-0.13f, 0.6f)
            reflectiveQuadToRelative(-0.35f, 0.53f)
            lineToRelative(-4.6f, 4.57f)
            quadToRelative(-0.45f, 0.45f, -1.1f, 0.45f)
            reflectiveQuadTo(12.93f, 17.7f)
            reflectiveQuadTo(12.48f, 16.6f)
            reflectiveQuadToRelative(0.45f, -1.1f)
            lineToRelative(1.93f, -1.93f)
            close()
          }
        }
        .build()
    return _arrow_right_alt!!
  }

private var _arrow_right_alt: ImageVector? = null
