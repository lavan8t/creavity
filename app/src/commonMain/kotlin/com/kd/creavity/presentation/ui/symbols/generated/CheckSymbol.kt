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
public val check: ImageVector
  get() {
    if (_check != null) {
      return _check!!
    }
    _check =
      ImageVector.Builder(
          name = "check",
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
            moveTo(9.55f, 14.3f)
            lineTo(17.58f, 6.27f)
            quadTo(18.05f, 5.8f, 18.7f, 5.8f)
            reflectiveQuadToRelative(1.13f, 0.47f)
            reflectiveQuadTo(20.3f, 7.4f)
            reflectiveQuadTo(19.83f, 8.52f)
            lineToRelative(-9.15f, 9.15f)
            quadTo(10.2f, 18.15f, 9.55f, 18.15f)
            reflectiveQuadTo(8.43f, 17.68f)
            lineTo(4.18f, 13.43f)
            quadTo(3.7f, 12.95f, 3.7f, 12.3f)
            reflectiveQuadTo(4.18f, 11.18f)
            reflectiveQuadTo(5.3f, 10.7f)
            reflectiveQuadToRelative(1.13f, 0.47f)
            lineTo(9.55f, 14.3f)
            close()
          }
        }
        .build()
    return _check!!
  }

private var _check: ImageVector? = null
