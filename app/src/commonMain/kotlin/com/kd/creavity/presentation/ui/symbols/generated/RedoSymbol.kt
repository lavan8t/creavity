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
public val redo: ImageVector
  get() {
    if (_redo != null) {
      return _redo!!
    }
    _redo =
      ImageVector.Builder(
          name = "redo",
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
            moveTo(14.85f, 10.58f)
            horizontalLineTo(9.38f)
            quadToRelative(-1.28f, 0f, -2.17f, 0.89f)
            reflectiveQuadTo(6.3f, 13.63f)
            reflectiveQuadTo(7.2f, 15.8f)
            reflectiveQuadToRelative(2.17f, 0.9f)
            horizontalLineToRelative(5.75f)
            quadToRelative(0.65f, 0f, 1.11f, 0.46f)
            reflectiveQuadToRelative(0.46f, 1.11f)
            quadToRelative(0f, 0.65f, -0.46f, 1.11f)
            reflectiveQuadToRelative(-1.11f, 0.46f)
            horizontalLineTo(9.38f)
            quadToRelative(-2.57f, 0f, -4.4f, -1.81f)
            reflectiveQuadTo(3.15f, 13.65f)
            quadToRelative(0f, -2.57f, 1.83f, -4.4f)
            reflectiveQuadTo(9.38f, 7.43f)
            horizontalLineToRelative(5.48f)
            lineTo(13.88f, 6.45f)
            quadTo(13.43f, 6f, 13.43f, 5.35f)
            reflectiveQuadToRelative(0.45f, -1.1f)
            reflectiveQuadTo(14.98f, 3.8f)
            reflectiveQuadToRelative(1.1f, 0.45f)
            lineToRelative(3.65f, 3.63f)
            quadTo(19.95f, 8.1f, 20.08f, 8.4f)
            reflectiveQuadTo(20.2f, 9f)
            reflectiveQuadTo(20.08f, 9.6f)
            reflectiveQuadToRelative(-0.35f, 0.53f)
            lineToRelative(-3.65f, 3.63f)
            quadToRelative(-0.45f, 0.45f, -1.1f, 0.45f)
            reflectiveQuadToRelative(-1.1f, -0.45f)
            reflectiveQuadToRelative(-0.45f, -1.1f)
            reflectiveQuadToRelative(0.45f, -1.1f)
            lineToRelative(0.98f, -0.97f)
            close()
          }
        }
        .build()
    return _redo!!
  }

private var _redo: ImageVector? = null
