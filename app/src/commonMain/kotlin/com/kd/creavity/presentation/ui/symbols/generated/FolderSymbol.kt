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
public val folder: ImageVector
  get() {
    if (_folder != null) {
      return _folder!!
    }
    _folder =
      ImageVector.Builder(
          name = "folder",
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
            moveTo(4.3f, 20.85f)
            quadToRelative(-1.32f, 0f, -2.24f, -0.91f)
            reflectiveQuadTo(1.15f, 17.7f)
            verticalLineTo(6.3f)
            quadToRelative(0f, -1.33f, 0.91f, -2.24f)
            quadTo(2.98f, 3.15f, 4.3f, 3.15f)
            horizontalLineTo(8.33f)
            quadToRelative(0.63f, 0f, 1.2f, 0.24f)
            reflectiveQuadToRelative(1.03f, 0.69f)
            lineTo(12f, 5.52f)
            horizontalLineToRelative(7.7f)
            quadToRelative(1.32f, 0f, 2.24f, 0.91f)
            reflectiveQuadToRelative(0.91f, 2.24f)
            verticalLineTo(17.7f)
            quadToRelative(0f, 1.32f, -0.91f, 2.24f)
            reflectiveQuadTo(19.7f, 20.85f)
            horizontalLineTo(4.3f)
            close()
          }
        }
        .build()
    return _folder!!
  }

private var _folder: ImageVector? = null
