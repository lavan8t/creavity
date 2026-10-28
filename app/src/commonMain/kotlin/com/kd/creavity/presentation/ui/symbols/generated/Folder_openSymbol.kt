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
public val folder_open: ImageVector
  get() {
    if (_folder_open != null) {
      return _folder_open!!
    }
    _folder_open =
      ImageVector.Builder(
          name = "folder_open",
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
            moveTo(4.3f, 20.85f)
            quadToRelative(-1.32f, 0f, -2.24f, -0.91f)
            reflectiveQuadTo(1.15f, 17.7f)
            verticalLineTo(6.3f)
            quadToRelative(0f, -1.35f, 0.91f, -2.25f)
            quadTo(2.98f, 3.15f, 4.3f, 3.15f)
            horizontalLineTo(8.33f)
            quadToRelative(0.63f, 0f, 1.2f, 0.24f)
            reflectiveQuadToRelative(1.03f, 0.69f)
            lineTo(12f, 5.52f)
            horizontalLineToRelative(9.28f)
            quadToRelative(0.65f, 0f, 1.11f, 0.46f)
            reflectiveQuadTo(22.85f, 7.1f)
            reflectiveQuadTo(22.39f, 8.21f)
            reflectiveQuadTo(21.28f, 8.67f)
            horizontalLineTo(9.03f)
            quadToRelative(-1.88f, 0f, -3.3f, 1.18f)
            quadTo(4.3f, 11.02f, 4.3f, 12.83f)
            verticalLineToRelative(5f)
            lineTo(5.9f, 12.85f)
            quadTo(6.23f, 11.88f, 7.04f, 11.27f)
            quadToRelative(0.81f, -0.6f, 1.86f, -0.6f)
            horizontalLineTo(19.85f)
            quadToRelative(1.6f, 0f, 2.55f, 1.3f)
            quadToRelative(0.95f, 1.3f, 0.45f, 2.82f)
            lineToRelative(-1f, 3.1f)
            quadToRelative(-0.5f, 1.53f, -1.48f, 2.24f)
            reflectiveQuadToRelative(-2.55f, 0.71f)
            horizontalLineTo(4.3f)
            close()
          }
        }
        .build()
    return _folder_open!!
  }

private var _folder_open: ImageVector? = null
