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
public val schedule: ImageVector
  get() {
    if (_schedule != null) {
      return _schedule!!
    }
    _schedule =
      ImageVector.Builder(
          name = "schedule",
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
            moveTo(13.38f, 11.4f)
            verticalLineTo(8.5f)
            quadToRelative(0f, -0.58f, -0.4f, -0.98f)
            reflectiveQuadTo(12f, 7.13f)
            reflectiveQuadToRelative(-0.97f, 0.4f)
            reflectiveQuadTo(10.63f, 8.5f)
            verticalLineToRelative(3.4f)
            quadToRelative(0f, 0.33f, 0.11f, 0.6f)
            reflectiveQuadTo(11.08f, 13f)
            lineToRelative(2.9f, 2.9f)
            quadToRelative(0.4f, 0.4f, 0.95f, 0.4f)
            reflectiveQuadTo(15.9f, 15.9f)
            reflectiveQuadToRelative(0.42f, -0.98f)
            reflectiveQuadToRelative(-0.42f, -1f)
            lineTo(13.38f, 11.4f)
            close()
            moveTo(12f, 22.85f)
            quadTo(9.73f, 22.85f, 7.75f, 22f)
            reflectiveQuadTo(4.31f, 19.69f)
            reflectiveQuadTo(2f, 16.25f)
            reflectiveQuadTo(1.15f, 12f)
            reflectiveQuadTo(2f, 7.75f)
            reflectiveQuadTo(4.31f, 4.31f)
            reflectiveQuadTo(7.75f, 2f)
            reflectiveQuadTo(12f, 1.15f)
            quadToRelative(2.28f, 0f, 4.25f, 0.85f)
            reflectiveQuadToRelative(3.44f, 2.31f)
            reflectiveQuadTo(22f, 7.75f)
            quadTo(22.85f, 9.73f, 22.85f, 12f)
            quadToRelative(0f, 2.27f, -0.85f, 4.25f)
            reflectiveQuadToRelative(-2.31f, 3.44f)
            reflectiveQuadTo(16.25f, 22f)
            reflectiveQuadTo(12f, 22.85f)
            close()
          }
        }
        .build()
    return _schedule!!
  }

private var _schedule: ImageVector? = null
