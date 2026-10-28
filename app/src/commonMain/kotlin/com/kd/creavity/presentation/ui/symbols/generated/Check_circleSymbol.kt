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
public val check_circle: ImageVector
  get() {
    if (_check_circle != null) {
      return _check_circle!!
    }
    _check_circle =
      ImageVector.Builder(
          name = "check_circle",
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
            moveTo(10.53f, 12.93f)
            lineToRelative(-1.5f, -1.5f)
            quadTo(8.6f, 11f, 7.98f, 11f)
            reflectiveQuadTo(6.93f, 11.43f)
            reflectiveQuadTo(6.51f, 12.48f)
            reflectiveQuadToRelative(0.44f, 1.05f)
            lineTo(9.4f, 15.98f)
            quadToRelative(0.48f, 0.47f, 1.13f, 0.47f)
            reflectiveQuadToRelative(1.12f, -0.47f)
            lineTo(16.93f, 10.7f)
            quadToRelative(0.43f, -0.43f, 0.43f, -1.04f)
            reflectiveQuadTo(16.93f, 8.63f)
            reflectiveQuadTo(15.88f, 8.2f)
            reflectiveQuadTo(14.83f, 8.63f)
            lineToRelative(-4.3f, 4.3f)
            close()
            moveTo(12f, 22.85f)
            quadTo(9.73f, 22.85f, 7.75f, 22f)
            reflectiveQuadTo(4.31f, 19.69f)
            reflectiveQuadTo(2f, 16.25f)
            reflectiveQuadTo(1.15f, 12f)
            reflectiveQuadTo(2f, 7.75f)
            reflectiveQuadTo(4.31f, 4.31f)
            reflectiveQuadTo(7.75f, 2f)
            reflectiveQuadTo(12f, 1.15f)
            quadToRelative(2.28f, 0f, 4.25f, 0.85f)
            reflectiveQuadToRelative(3.44f, 2.31f)
            reflectiveQuadTo(22f, 7.75f)
            quadTo(22.85f, 9.73f, 22.85f, 12f)
            quadToRelative(0f, 2.27f, -0.85f, 4.25f)
            reflectiveQuadToRelative(-2.31f, 3.44f)
            reflectiveQuadTo(16.25f, 22f)
            reflectiveQuadTo(12f, 22.85f)
            close()
          }
        }
        .build()
    return _check_circle!!
  }

private var _check_circle: ImageVector? = null
