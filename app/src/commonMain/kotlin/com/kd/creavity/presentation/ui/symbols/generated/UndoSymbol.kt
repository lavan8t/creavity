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
public val undo: ImageVector
  get() {
    if (_undo != null) {
      return _undo!!
    }
    _undo =
      ImageVector.Builder(
          name = "undo",
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
            moveTo(8.88f, 19.85f)
            quadToRelative(-0.65f, 0f, -1.11f, -0.46f)
            quadTo(7.3f, 18.93f, 7.3f, 18.27f)
            quadToRelative(0f, -0.65f, 0.46f, -1.11f)
            reflectiveQuadTo(8.88f, 16.7f)
            horizontalLineToRelative(5.75f)
            quadToRelative(1.28f, 0f, 2.18f, -0.89f)
            reflectiveQuadToRelative(0.9f, -2.16f)
            quadToRelative(0f, -1.27f, -0.9f, -2.17f)
            reflectiveQuadToRelative(-2.18f, -0.9f)
            horizontalLineTo(9.15f)
            lineToRelative(0.97f, 0.97f)
            quadToRelative(0.45f, 0.45f, 0.45f, 1.1f)
            reflectiveQuadToRelative(-0.45f, 1.1f)
            quadTo(9.68f, 14.2f, 9.03f, 14.2f)
            reflectiveQuadTo(7.93f, 13.75f)
            lineTo(4.28f, 10.13f)
            quadTo(4.05f, 9.9f, 3.93f, 9.6f)
            reflectiveQuadTo(3.8f, 9f)
            reflectiveQuadTo(3.93f, 8.4f)
            reflectiveQuadTo(4.28f, 7.88f)
            lineTo(7.93f, 4.25f)
            quadTo(8.38f, 3.8f, 9.03f, 3.8f)
            reflectiveQuadToRelative(1.1f, 0.45f)
            reflectiveQuadToRelative(0.45f, 1.1f)
            reflectiveQuadToRelative(-0.45f, 1.1f)
            lineTo(9.15f, 7.43f)
            horizontalLineToRelative(5.48f)
            quadToRelative(2.57f, 0f, 4.4f, 1.81f)
            reflectiveQuadToRelative(1.82f, 4.39f)
            reflectiveQuadToRelative(-1.82f, 4.4f)
            reflectiveQuadToRelative(-4.4f, 1.83f)
            horizontalLineTo(8.88f)
            close()
          }
        }
        .build()
    return _undo!!
  }

private var _undo: ImageVector? = null
