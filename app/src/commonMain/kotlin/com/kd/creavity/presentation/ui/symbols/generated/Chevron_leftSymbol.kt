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
public val chevron_left: ImageVector
  get() {
    if (_chevron_left != null) {
      return _chevron_left!!
    }
    _chevron_left =
      ImageVector.Builder(
          name = "chevron_left",
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
            moveTo(11.6f, 12f)
            lineToRelative(3.5f, 3.5f)
            quadToRelative(0.45f, 0.45f, 0.45f, 1.1f)
            reflectiveQuadTo(15.1f, 17.7f)
            reflectiveQuadTo(14f, 18.15f)
            reflectiveQuadTo(12.9f, 17.7f)
            lineTo(8.33f, 13.13f)
            quadTo(8.1f, 12.9f, 7.98f, 12.6f)
            reflectiveQuadTo(7.85f, 12f)
            reflectiveQuadTo(7.98f, 11.4f)
            reflectiveQuadTo(8.33f, 10.88f)
            lineTo(12.9f, 6.3f)
            quadTo(13.35f, 5.85f, 14f, 5.85f)
            reflectiveQuadTo(15.1f, 6.3f)
            reflectiveQuadToRelative(0.45f, 1.1f)
            reflectiveQuadTo(15.1f, 8.5f)
            lineTo(11.6f, 12f)
            close()
          }
        }
        .build()
    return _chevron_left!!
  }

private var _chevron_left: ImageVector? = null
