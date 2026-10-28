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
public val more_vert: ImageVector
  get() {
    if (_more_vert != null) {
      return _more_vert!!
    }
    _more_vert =
      ImageVector.Builder(
          name = "more_vert",
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
            moveTo(12.01f, 21.15f)
            quadToRelative(-0.99f, 0f, -1.69f, -0.7f)
            reflectiveQuadTo(9.63f, 18.77f)
            quadToRelative(0f, -0.99f, 0.7f, -1.69f)
            reflectiveQuadTo(12f, 16.38f)
            quadToRelative(1f, 0f, 1.69f, 0.7f)
            reflectiveQuadToRelative(0.69f, 1.69f)
            reflectiveQuadToRelative(-0.69f, 1.69f)
            reflectiveQuadToRelative(-1.67f, 0.7f)
            close()
            moveToRelative(0f, -6.77f)
            quadToRelative(-0.99f, 0f, -1.69f, -0.7f)
            reflectiveQuadTo(9.63f, 12f)
            quadToRelative(0f, -1f, 0.7f, -1.69f)
            reflectiveQuadTo(12f, 9.63f)
            quadToRelative(1f, 0f, 1.69f, 0.69f)
            reflectiveQuadToRelative(0.69f, 1.67f)
            quadToRelative(0f, 0.99f, -0.69f, 1.69f)
            quadToRelative(-0.69f, 0.7f, -1.67f, 0.7f)
            close()
            moveToRelative(0f, -6.75f)
            quadToRelative(-0.99f, 0f, -1.69f, -0.71f)
            reflectiveQuadTo(9.63f, 5.22f)
            reflectiveQuadToRelative(0.7f, -1.68f)
            reflectiveQuadTo(12f, 2.85f)
            quadToRelative(1f, 0f, 1.69f, 0.69f)
            reflectiveQuadToRelative(0.69f, 1.68f)
            reflectiveQuadToRelative(-0.69f, 1.7f)
            quadTo(13f, 7.63f, 12.01f, 7.63f)
            close()
          }
        }
        .build()
    return _more_vert!!
  }

private var _more_vert: ImageVector? = null
