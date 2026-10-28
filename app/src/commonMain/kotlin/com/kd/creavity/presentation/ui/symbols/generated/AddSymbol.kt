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
public val add: ImageVector
  get() {
    if (_add != null) {
      return _add!!
    }
    _add =
      ImageVector.Builder(
          name = "add",
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
            moveTo(10.43f, 13.58f)
            horizontalLineTo(5.73f)
            quadToRelative(-0.65f, 0f, -1.11f, -0.46f)
            reflectiveQuadTo(4.15f, 12f)
            reflectiveQuadTo(4.61f, 10.89f)
            quadTo(5.08f, 10.43f, 5.73f, 10.43f)
            horizontalLineToRelative(4.7f)
            verticalLineTo(5.72f)
            quadToRelative(0f, -0.65f, 0.46f, -1.11f)
            reflectiveQuadTo(12f, 4.15f)
            reflectiveQuadToRelative(1.11f, 0.46f)
            reflectiveQuadToRelative(0.46f, 1.11f)
            verticalLineToRelative(4.7f)
            horizontalLineToRelative(4.7f)
            quadToRelative(0.65f, 0f, 1.11f, 0.46f)
            reflectiveQuadTo(19.85f, 12f)
            reflectiveQuadToRelative(-0.46f, 1.11f)
            reflectiveQuadToRelative(-1.11f, 0.46f)
            horizontalLineToRelative(-4.7f)
            verticalLineToRelative(4.7f)
            quadToRelative(0f, 0.65f, -0.46f, 1.11f)
            reflectiveQuadTo(12f, 19.85f)
            reflectiveQuadTo(10.89f, 19.39f)
            quadTo(10.43f, 18.93f, 10.43f, 18.27f)
            verticalLineToRelative(-4.7f)
            close()
          }
        }
        .build()
    return _add!!
  }

private var _add: ImageVector? = null
