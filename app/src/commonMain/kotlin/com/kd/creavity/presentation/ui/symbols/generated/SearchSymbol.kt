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
public val search: ImageVector
  get() {
    if (_search != null) {
      return _search!!
    }
    _search =
      ImageVector.Builder(
          name = "search",
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
            moveTo(9.3f, 16.58f)
            quadToRelative(-2.95f, 0f, -5.01f, -2.06f)
            reflectiveQuadTo(2.23f, 9.5f)
            reflectiveQuadTo(4.29f, 4.49f)
            reflectiveQuadTo(9.3f, 2.42f)
            reflectiveQuadToRelative(5.01f, 2.06f)
            reflectiveQuadTo(16.38f, 9.5f)
            quadToRelative(0f, 1.15f, -0.3f, 2.09f)
            quadToRelative(-0.3f, 0.94f, -0.8f, 1.64f)
            lineToRelative(5.4f, 5.42f)
            quadToRelative(0.45f, 0.48f, 0.45f, 1.11f)
            reflectiveQuadToRelative(-0.47f, 1.09f)
            quadTo(20.2f, 21.3f, 19.55f, 21.3f)
            reflectiveQuadToRelative(-1.1f, -0.45f)
            lineTo(13.08f, 15.48f)
            quadToRelative(-0.72f, 0.5f, -1.69f, 0.8f)
            quadToRelative(-0.96f, 0.3f, -2.09f, 0.3f)
            close()
            moveToRelative(0f, -3.15f)
            quadToRelative(1.65f, 0f, 2.79f, -1.14f)
            reflectiveQuadTo(13.23f, 9.5f)
            reflectiveQuadTo(12.09f, 6.71f)
            reflectiveQuadTo(9.3f, 5.57f)
            reflectiveQuadTo(6.51f, 6.71f)
            reflectiveQuadTo(5.38f, 9.5f)
            reflectiveQuadToRelative(1.14f, 2.79f)
            reflectiveQuadTo(9.3f, 13.43f)
            close()
          }
        }
        .build()
    return _search!!
  }

private var _search: ImageVector? = null
