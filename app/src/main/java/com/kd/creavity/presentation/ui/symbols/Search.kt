package com.kd.creavity.presentation.ui.symbols

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
          defaultWidth = 48.dp,
          defaultHeight = 48.dp,
          viewportWidth = 48f,
          viewportHeight = 48f,
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
            moveTo(39.7f, 43.2f)
            lineTo(26.29f, 29.8f)
            quadToRelative(-1.49f, 1.15f, -3.41f, 1.8f)
            reflectiveQuadToRelative(-4.22f, 0.65f)
            quadToRelative(-5.74f, 0f, -9.67f, -3.94f)
            reflectiveQuadTo(5.05f, 18.74f)
            reflectiveQuadTo(8.99f, 9.17f)
            reflectiveQuadTo(18.61f, 5.25f)
            quadToRelative(5.59f, 0f, 9.51f, 3.93f)
            reflectiveQuadToRelative(3.93f, 9.57f)
            quadToRelative(0f, 2.3f, -0.65f, 4.17f)
            reflectiveQuadTo(29.5f, 26.5f)
            lineTo(43f, 39.9f)
            lineToRelative(-3.3f, 3.3f)
            close()
            moveTo(18.58f, 27.7f)
            quadToRelative(3.78f, 0f, 6.35f, -2.57f)
            reflectiveQuadTo(27.5f, 18.77f)
            reflectiveQuadTo(24.93f, 12.4f)
            reflectiveQuadTo(18.58f, 9.8f)
            quadToRelative(-3.83f, 0f, -6.4f, 2.6f)
            reflectiveQuadTo(9.6f, 18.77f)
            reflectiveQuadToRelative(2.57f, 6.35f)
            reflectiveQuadToRelative(6.4f, 2.57f)
            close()
          }
        }
        .build()
    return _search!!
  }

private var _search: ImageVector? = null

@Suppress("CheckReturnValue")
public val search_filled: ImageVector
  get() {
    if (_search_filled != null) {
      return _search_filled!!
    }
    _search_filled =
      ImageVector.Builder(
          name = "search",
          defaultWidth = 48.dp,
          defaultHeight = 48.dp,
          viewportWidth = 48f,
          viewportHeight = 48f,
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
            moveTo(39.7f, 43.2f)
            lineTo(26.29f, 29.8f)
            quadToRelative(-1.49f, 1.15f, -3.41f, 1.8f)
            reflectiveQuadToRelative(-4.22f, 0.65f)
            quadToRelative(-5.74f, 0f, -9.67f, -3.94f)
            reflectiveQuadTo(5.05f, 18.74f)
            reflectiveQuadTo(8.99f, 9.17f)
            reflectiveQuadTo(18.61f, 5.25f)
            quadToRelative(5.59f, 0f, 9.51f, 3.93f)
            reflectiveQuadToRelative(3.93f, 9.57f)
            quadToRelative(0f, 2.3f, -0.65f, 4.17f)
            reflectiveQuadTo(29.5f, 26.5f)
            lineTo(43f, 39.9f)
            lineToRelative(-3.3f, 3.3f)
            close()
            moveTo(18.58f, 27.7f)
            quadToRelative(3.78f, 0f, 6.35f, -2.57f)
            reflectiveQuadTo(27.5f, 18.77f)
            reflectiveQuadTo(24.93f, 12.4f)
            reflectiveQuadTo(18.58f, 9.8f)
            quadToRelative(-3.83f, 0f, -6.4f, 2.6f)
            reflectiveQuadTo(9.6f, 18.77f)
            reflectiveQuadToRelative(2.57f, 6.35f)
            reflectiveQuadToRelative(6.4f, 2.57f)
            close()
          }
        }
        .build()
    return _search_filled!!
  }

private var _search_filled: ImageVector? = null
