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
public val lock: ImageVector
  get() {
    if (_lock != null) {
      return _lock!!
    }
    _lock =
      ImageVector.Builder(
          name = "lock",
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
            moveTo(6.3f, 22.78f)
            quadTo(5f, 22.78f, 4.08f, 21.85f)
            reflectiveQuadTo(3.15f, 19.63f)
            verticalLineToRelative(-9.4f)
            quadTo(3.15f, 8.93f, 4.08f, 8f)
            quadTo(5f, 7.07f, 6.3f, 7.07f)
            horizontalLineTo(6.63f)
            verticalLineTo(5.8f)
            quadToRelative(0f, -2.28f, 1.56f, -3.86f)
            reflectiveQuadTo(12f, 0.35f)
            reflectiveQuadToRelative(3.81f, 1.59f)
            quadToRelative(1.56f, 1.59f, 1.56f, 3.86f)
            verticalLineTo(7.07f)
            horizontalLineTo(17.7f)
            quadTo(19f, 7.07f, 19.92f, 8f)
            reflectiveQuadToRelative(0.93f, 2.22f)
            verticalLineToRelative(9.4f)
            quadToRelative(0f, 1.3f, -0.93f, 2.22f)
            reflectiveQuadTo(17.7f, 22.78f)
            horizontalLineTo(6.3f)
            close()
            moveToRelative(7.11f, -6.44f)
            quadTo(14f, 15.75f, 14f, 14.93f)
            reflectiveQuadTo(13.41f, 13.51f)
            reflectiveQuadTo(12f, 12.93f)
            reflectiveQuadToRelative(-1.41f, 0.59f)
            reflectiveQuadTo(10f, 14.93f)
            reflectiveQuadToRelative(0.59f, 1.41f)
            quadToRelative(0.59f, 0.59f, 1.41f, 0.59f)
            reflectiveQuadToRelative(1.41f, -0.59f)
            close()
            moveTo(9.78f, 7.07f)
            horizontalLineToRelative(4.45f)
            verticalLineTo(5.8f)
            quadToRelative(0f, -0.96f, -0.64f, -1.63f)
            reflectiveQuadTo(12f, 3.5f)
            reflectiveQuadTo(10.41f, 4.17f)
            reflectiveQuadTo(9.78f, 5.8f)
            verticalLineTo(7.07f)
            close()
          }
        }
        .build()
    return _lock!!
  }

private var _lock: ImageVector? = null
