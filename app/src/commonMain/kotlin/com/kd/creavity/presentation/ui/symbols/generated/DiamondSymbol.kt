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
public val diamond: ImageVector
  get() {
    if (_diamond != null) {
      return _diamond!!
    }
    _diamond =
      ImageVector.Builder(
          name = "diamond",
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
            moveTo(9.23f, 8.25f)
            horizontalLineToRelative(5.55f)
            lineTo(12.35f, 2.63f)
            horizontalLineToRelative(-0.7f)
            lineTo(9.23f, 8.25f)
            close()
            moveTo(11.25f, 20.7f)
            verticalLineTo(9.75f)
            horizontalLineTo(2.1f)
            lineTo(11.25f, 20.7f)
            close()
            moveToRelative(1.5f, 0f)
            lineTo(21.9f, 9.75f)
            horizontalLineTo(12.75f)
            verticalLineTo(20.7f)
            close()
            moveTo(16.5f, 8.25f)
            horizontalLineToRelative(5.55f)
            lineTo(20.1f, 4.35f)
            quadTo(19.7f, 3.55f, 18.94f, 3.09f)
            quadTo(18.18f, 2.63f, 17.28f, 2.63f)
            horizontalLineToRelative(-3.2f)
            lineTo(16.5f, 8.25f)
            close()
            moveToRelative(-14.55f, 0f)
            horizontalLineTo(7.5f)
            lineTo(9.93f, 2.63f)
            horizontalLineTo(6.73f)
            quadToRelative(-0.9f, 0f, -1.66f, 0.46f)
            reflectiveQuadTo(3.9f, 4.35f)
            lineTo(1.95f, 8.25f)
            close()
          }
        }
        .build()
    return _diamond!!
  }

private var _diamond: ImageVector? = null
