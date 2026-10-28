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
public val menu: ImageVector
  get() {
    if (_menu != null) {
      return _menu!!
    }
    _menu =
      ImageVector.Builder(
          name = "menu",
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
            moveTo(3.73f, 19.23f)
            quadToRelative(-0.65f, 0f, -1.11f, -0.46f)
            reflectiveQuadTo(2.15f, 17.65f)
            reflectiveQuadTo(2.61f, 16.54f)
            reflectiveQuadTo(3.73f, 16.08f)
            horizontalLineTo(20.28f)
            quadToRelative(0.65f, 0f, 1.11f, 0.46f)
            reflectiveQuadToRelative(0.46f, 1.11f)
            reflectiveQuadToRelative(-0.46f, 1.11f)
            quadToRelative(-0.46f, 0.46f, -1.11f, 0.46f)
            horizontalLineTo(3.73f)
            close()
            moveToRelative(0f, -5.65f)
            quadToRelative(-0.65f, 0f, -1.11f, -0.46f)
            reflectiveQuadTo(2.15f, 12f)
            reflectiveQuadTo(2.61f, 10.89f)
            reflectiveQuadTo(3.73f, 10.43f)
            horizontalLineTo(20.28f)
            quadToRelative(0.65f, 0f, 1.11f, 0.46f)
            reflectiveQuadTo(21.85f, 12f)
            reflectiveQuadToRelative(-0.46f, 1.11f)
            reflectiveQuadToRelative(-1.11f, 0.46f)
            horizontalLineTo(3.73f)
            close()
            moveToRelative(0f, -5.65f)
            quadTo(3.08f, 7.93f, 2.61f, 7.46f)
            reflectiveQuadTo(2.15f, 6.35f)
            reflectiveQuadTo(2.61f, 5.24f)
            quadTo(3.08f, 4.77f, 3.73f, 4.77f)
            horizontalLineTo(20.28f)
            quadToRelative(0.65f, 0f, 1.11f, 0.46f)
            reflectiveQuadToRelative(0.46f, 1.11f)
            reflectiveQuadTo(21.39f, 7.46f)
            quadTo(20.93f, 7.93f, 20.28f, 7.93f)
            horizontalLineTo(3.73f)
            close()
          }
        }
        .build()
    return _menu!!
  }

private var _menu: ImageVector? = null
