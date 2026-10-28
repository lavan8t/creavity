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
public val image: ImageVector
  get() {
    if (_image != null) {
      return _image!!
    }
    _image =
      ImageVector.Builder(
          name = "image",
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
            moveTo(5.3f, 21.85f)
            quadToRelative(-1.33f, 0f, -2.24f, -0.91f)
            reflectiveQuadTo(2.15f, 18.7f)
            verticalLineTo(5.3f)
            quadToRelative(0f, -1.33f, 0.91f, -2.24f)
            reflectiveQuadTo(5.3f, 2.15f)
            horizontalLineTo(18.7f)
            quadToRelative(1.33f, 0f, 2.24f, 0.91f)
            quadToRelative(0.91f, 0.91f, 0.91f, 2.24f)
            verticalLineTo(18.7f)
            quadToRelative(0f, 1.32f, -0.91f, 2.24f)
            quadToRelative(-0.91f, 0.91f, -2.24f, 0.91f)
            horizontalLineTo(5.3f)
            close()
            moveTo(7.1f, 17.3f)
            horizontalLineToRelative(9.8f)
            quadToRelative(0.5f, 0f, 0.7f, -0.43f)
            reflectiveQuadTo(17.53f, 16.05f)
            lineTo(14.88f, 12.38f)
            quadTo(14.65f, 12.05f, 14.25f, 12.04f)
            quadToRelative(-0.4f, -0.01f, -0.65f, 0.31f)
            lineTo(11.25f, 15.5f)
            lineTo(9.65f, 13.35f)
            quadTo(9.4f, 13.02f, 9f, 13.04f)
            reflectiveQuadTo(8.38f, 13.38f)
            lineToRelative(-1.9f, 2.68f)
            quadTo(6.2f, 16.45f, 6.4f, 16.88f)
            reflectiveQuadTo(7.1f, 17.3f)
            close()
          }
        }
        .build()
    return _image!!
  }

private var _image: ImageVector? = null
