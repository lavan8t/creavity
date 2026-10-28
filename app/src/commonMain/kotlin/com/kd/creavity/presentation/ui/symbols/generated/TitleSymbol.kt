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
public val title: ImageVector
  get() {
    if (_title != null) {
      return _title!!
    }
    _title =
      ImageVector.Builder(
          name = "title",
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
            moveTo(9.93f, 7.27f)
            horizontalLineTo(6.2f)
            quadToRelative(-0.88f, 0f, -1.46f, -0.6f)
            quadTo(4.15f, 6.07f, 4.15f, 5.2f)
            reflectiveQuadTo(4.74f, 3.74f)
            quadTo(5.33f, 3.15f, 6.2f, 3.15f)
            horizontalLineTo(17.8f)
            quadToRelative(0.88f, 0f, 1.46f, 0.59f)
            reflectiveQuadTo(19.85f, 5.2f)
            reflectiveQuadTo(19.26f, 6.68f)
            quadToRelative(-0.59f, 0.6f, -1.46f, 0.6f)
            horizontalLineTo(14.1f)
            verticalLineToRelative(11.5f)
            quadToRelative(0f, 0.88f, -0.6f, 1.48f)
            reflectiveQuadToRelative(-1.47f, 0.6f)
            reflectiveQuadTo(10.54f, 20.24f)
            reflectiveQuadTo(9.93f, 18.75f)
            verticalLineTo(7.27f)
            close()
          }
        }
        .build()
    return _title!!
  }

private var _title: ImageVector? = null
