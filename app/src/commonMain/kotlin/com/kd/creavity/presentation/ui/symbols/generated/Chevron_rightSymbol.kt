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
public val chevron_right: ImageVector
  get() {
    if (_chevron_right != null) {
      return _chevron_right!!
    }
    _chevron_right =
      ImageVector.Builder(
          name = "chevron_right",
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
            moveTo(11.8f, 12f)
            lineTo(8.3f, 8.5f)
            quadTo(7.85f, 8.05f, 7.85f, 7.4f)
            reflectiveQuadTo(8.3f, 6.3f)
            reflectiveQuadTo(9.4f, 5.85f)
            reflectiveQuadTo(10.5f, 6.3f)
            lineToRelative(4.58f, 4.57f)
            quadToRelative(0.22f, 0.22f, 0.35f, 0.52f)
            reflectiveQuadTo(15.55f, 12f)
            reflectiveQuadToRelative(-0.13f, 0.6f)
            reflectiveQuadToRelative(-0.35f, 0.53f)
            lineTo(10.5f, 17.7f)
            quadToRelative(-0.45f, 0.45f, -1.1f, 0.45f)
            reflectiveQuadTo(8.3f, 17.7f)
            reflectiveQuadTo(7.85f, 16.6f)
            reflectiveQuadTo(8.3f, 15.5f)
            lineTo(11.8f, 12f)
            close()
          }
        }
        .build()
    return _chevron_right!!
  }

private var _chevron_right: ImageVector? = null
