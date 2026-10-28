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
public val change_history: ImageVector
  get() {
    if (_change_history != null) {
      return _change_history!!
    }
    _change_history =
      ImageVector.Builder(
          name = "change_history",
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
            moveTo(3.33f, 20.85f)
            quadToRelative(-0.92f, 0f, -1.39f, -0.8f)
            quadToRelative(-0.46f, -0.8f, 0.04f, -1.6f)
            lineToRelative(8.7f, -13.9f)
            quadTo(11.15f, 3.82f, 12f, 3.82f)
            reflectiveQuadToRelative(1.33f, 0.72f)
            lineToRelative(8.7f, 13.9f)
            quadToRelative(0.5f, 0.8f, 0.04f, 1.6f)
            reflectiveQuadToRelative(-1.39f, 0.8f)
            horizontalLineTo(3.33f)
            close()
          }
        }
        .build()
    return _change_history!!
  }

private var _change_history: ImageVector? = null
