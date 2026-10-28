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
public val home: ImageVector
  get() {
    if (_home != null) {
      return _home!!
    }
    _home =
      ImageVector.Builder(
          name = "home",
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
            moveTo(3.15f, 18.7f)
            verticalLineTo(10.15f)
            quadTo(3.15f, 9.4f, 3.48f, 8.74f)
            reflectiveQuadTo(4.4f, 7.63f)
            lineTo(10.1f, 3.35f)
            quadTo(10.95f, 2.7f, 12f, 2.7f)
            reflectiveQuadToRelative(1.9f, 0.65f)
            lineToRelative(5.7f, 4.28f)
            quadToRelative(0.6f, 0.45f, 0.93f, 1.11f)
            reflectiveQuadToRelative(0.32f, 1.41f)
            verticalLineTo(18.7f)
            quadToRelative(0f, 1.32f, -0.91f, 2.24f)
            quadToRelative(-0.91f, 0.91f, -2.24f, 0.91f)
            horizontalLineTo(15.78f)
            quadToRelative(-0.65f, 0f, -1.11f, -0.46f)
            quadTo(14.2f, 20.93f, 14.2f, 20.27f)
            verticalLineToRelative(-4.9f)
            quadToRelative(0f, -0.65f, -0.46f, -1.11f)
            reflectiveQuadTo(12.63f, 13.8f)
            horizontalLineTo(11.38f)
            quadToRelative(-0.65f, 0f, -1.11f, 0.46f)
            reflectiveQuadTo(9.8f, 15.38f)
            verticalLineToRelative(4.9f)
            quadToRelative(0f, 0.65f, -0.46f, 1.11f)
            reflectiveQuadTo(8.23f, 21.85f)
            horizontalLineTo(6.3f)
            quadToRelative(-1.33f, 0f, -2.24f, -0.91f)
            reflectiveQuadTo(3.15f, 18.7f)
            close()
          }
        }
        .build()
    return _home!!
  }

private var _home: ImageVector? = null
