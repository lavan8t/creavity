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
public val edit: ImageVector
  get() {
    if (_edit != null) {
      return _edit!!
    }
    _edit =
      ImageVector.Builder(
          name = "edit",
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
            moveTo(3.73f, 21.85f)
            quadToRelative(-0.65f, 0f, -1.11f, -0.46f)
            quadTo(2.15f, 20.93f, 2.15f, 20.27f)
            verticalLineTo(17.3f)
            quadToRelative(0f, -0.63f, 0.24f, -1.2f)
            reflectiveQuadTo(3.08f, 15.08f)
            lineTo(15.3f, 2.85f)
            quadTo(15.65f, 2.5f, 16.09f, 2.32f)
            reflectiveQuadTo(17f, 2.15f)
            quadToRelative(0.45f, 0f, 0.9f, 0.18f)
            reflectiveQuadToRelative(0.82f, 0.52f)
            lineToRelative(2.45f, 2.4f)
            quadTo(21.53f, 5.6f, 21.7f, 6.06f)
            quadTo(21.88f, 6.52f, 21.88f, 7f)
            quadToRelative(0f, 0.47f, -0.17f, 0.93f)
            reflectiveQuadToRelative(-0.53f, 0.8f)
            lineTo(8.98f, 20.93f)
            quadTo(8.53f, 21.38f, 7.95f, 21.61f)
            reflectiveQuadToRelative(-1.2f, 0.24f)
            horizontalLineTo(3.73f)
            close()
            moveTo(17.05f, 8.38f)
            lineTo(18.45f, 7f)
            lineTo(17.03f, 5.55f)
            lineTo(15.6f, 6.95f)
            lineToRelative(1.45f, 1.43f)
            close()
          }
        }
        .build()
    return _edit!!
  }

private var _edit: ImageVector? = null
