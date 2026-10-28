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
public val grid_view: ImageVector
  get() {
    if (_grid_view != null) {
      return _grid_view!!
    }
    _grid_view =
      ImageVector.Builder(
          name = "grid_view",
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
            moveTo(5.3f, 11f)
            quadTo(4f, 11f, 3.08f, 10.07f)
            reflectiveQuadTo(2.15f, 7.85f)
            verticalLineTo(5.3f)
            quadTo(2.15f, 4f, 3.08f, 3.08f)
            reflectiveQuadTo(5.3f, 2.15f)
            horizontalLineTo(7.85f)
            quadToRelative(1.3f, 0f, 2.22f, 0.93f)
            reflectiveQuadTo(11f, 5.3f)
            verticalLineTo(7.85f)
            quadToRelative(0f, 1.3f, -0.93f, 2.22f)
            reflectiveQuadTo(7.85f, 11f)
            horizontalLineTo(5.3f)
            close()
            moveToRelative(0f, 10.85f)
            quadTo(4f, 21.85f, 3.08f, 20.92f)
            reflectiveQuadTo(2.15f, 18.7f)
            verticalLineTo(16.15f)
            quadToRelative(0f, -1.3f, 0.93f, -2.22f)
            reflectiveQuadTo(5.3f, 13f)
            horizontalLineTo(7.85f)
            quadToRelative(1.3f, 0f, 2.22f, 0.93f)
            reflectiveQuadTo(11f, 16.15f)
            verticalLineTo(18.7f)
            quadToRelative(0f, 1.3f, -0.93f, 2.22f)
            reflectiveQuadTo(7.85f, 21.85f)
            horizontalLineTo(5.3f)
            close()
            moveTo(16.15f, 11f)
            quadToRelative(-1.3f, 0f, -2.22f, -0.93f)
            reflectiveQuadTo(13f, 7.85f)
            verticalLineTo(5.3f)
            quadTo(13f, 4f, 13.93f, 3.08f)
            reflectiveQuadTo(16.15f, 2.15f)
            horizontalLineTo(18.7f)
            quadToRelative(1.3f, 0f, 2.22f, 0.93f)
            reflectiveQuadTo(21.85f, 5.3f)
            verticalLineTo(7.85f)
            quadToRelative(0f, 1.3f, -0.93f, 2.22f)
            reflectiveQuadTo(18.7f, 11f)
            horizontalLineTo(16.15f)
            close()
            moveToRelative(0f, 10.85f)
            quadToRelative(-1.3f, 0f, -2.22f, -0.93f)
            reflectiveQuadTo(13f, 18.7f)
            verticalLineTo(16.15f)
            quadToRelative(0f, -1.3f, 0.93f, -2.22f)
            reflectiveQuadTo(16.15f, 13f)
            horizontalLineTo(18.7f)
            quadToRelative(1.3f, 0f, 2.22f, 0.93f)
            reflectiveQuadToRelative(0.93f, 2.22f)
            verticalLineTo(18.7f)
            quadToRelative(0f, 1.3f, -0.93f, 2.22f)
            reflectiveQuadTo(18.7f, 21.85f)
            horizontalLineTo(16.15f)
            close()
          }
        }
        .build()
    return _grid_view!!
  }

private var _grid_view: ImageVector? = null
