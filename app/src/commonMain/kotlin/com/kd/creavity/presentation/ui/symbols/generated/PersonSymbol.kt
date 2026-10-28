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
public val person: ImageVector
  get() {
    if (_person != null) {
      return _person!!
    }
    _person =
      ImageVector.Builder(
          name = "person",
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
            moveTo(8.56f, 10.16f)
            quadTo(7.15f, 8.75f, 7.15f, 6.72f)
            reflectiveQuadTo(8.56f, 3.3f)
            reflectiveQuadTo(12f, 1.9f)
            quadToRelative(2.03f, 0f, 3.44f, 1.4f)
            reflectiveQuadToRelative(1.41f, 3.43f)
            reflectiveQuadToRelative(-1.41f, 3.44f)
            quadTo(14.03f, 11.58f, 12f, 11.58f)
            reflectiveQuadTo(8.56f, 10.16f)
            close()
            moveTo(3.15f, 18.13f)
            verticalLineTo(17.58f)
            quadToRelative(0f, -1.07f, 0.56f, -1.99f)
            reflectiveQuadTo(5.23f, 14.2f)
            quadTo(6.85f, 13.4f, 8.55f, 12.99f)
            reflectiveQuadTo(12f, 12.58f)
            quadToRelative(1.8f, 0f, 3.5f, 0.4f)
            reflectiveQuadToRelative(3.28f, 1.2f)
            quadToRelative(0.95f, 0.48f, 1.51f, 1.38f)
            reflectiveQuadToRelative(0.56f, 2.03f)
            verticalLineToRelative(0.55f)
            quadToRelative(0f, 1.32f, -0.91f, 2.24f)
            quadToRelative(-0.91f, 0.91f, -2.24f, 0.91f)
            horizontalLineTo(6.3f)
            quadToRelative(-1.33f, 0f, -2.24f, -0.91f)
            reflectiveQuadTo(3.15f, 18.13f)
            close()
          }
        }
        .build()
    return _person!!
  }

private var _person: ImageVector? = null
