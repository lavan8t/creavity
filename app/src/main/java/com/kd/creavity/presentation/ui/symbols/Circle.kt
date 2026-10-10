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
public val circle: ImageVector
  get() {
    if (_circle != null) {
      return _circle!!
    }
    _circle =
      ImageVector.Builder(
          name = "circle",
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
            moveTo(23.98f, 45.05f)
            quadToRelative(-4.32f, 0f, -8.18f, -1.63f)
            reflectiveQuadTo(9.07f, 38.93f)
            reflectiveQuadTo(4.58f, 32.2f)
            reflectiveQuadTo(2.95f, 24.01f)
            quadToRelative(0f, -4.37f, 1.63f, -8.21f)
            reflectiveQuadTo(9.09f, 9.07f)
            reflectiveQuadTo(15.81f, 4.55f)
            reflectiveQuadTo(23.97f, 2.9f)
            quadToRelative(4.38f, 0f, 8.24f, 1.64f)
            reflectiveQuadToRelative(6.73f, 4.51f)
            reflectiveQuadToRelative(4.51f, 6.73f)
            reflectiveQuadToRelative(1.65f, 8.25f)
            quadToRelative(0f, 4.34f, -1.65f, 8.17f)
            reflectiveQuadToRelative(-4.52f, 6.71f)
            reflectiveQuadToRelative(-6.72f, 4.51f)
            reflectiveQuadToRelative(-8.22f, 1.63f)
            close()
            moveToRelative(0f, -4.55f)
            quadToRelative(6.84f, 0f, 11.67f, -4.82f)
            reflectiveQuadTo(40.5f, 24.01f)
            quadToRelative(0f, -6.84f, -4.83f, -11.67f)
            reflectiveQuadTo(23.99f, 7.5f)
            quadToRelative(-6.81f, 0f, -11.65f, 4.83f)
            reflectiveQuadTo(7.5f, 24.01f)
            quadToRelative(0f, 6.81f, 4.82f, 11.65f)
            reflectiveQuadTo(23.99f, 40.5f)
            close()
            moveTo(24f, 24f)
            close()
          }
        }
        .build()
    return _circle!!
  }

private var _circle: ImageVector? = null

@Suppress("CheckReturnValue")
public val circle_filled: ImageVector
  get() {
    if (_circle_filled != null) {
      return _circle_filled!!
    }
    _circle_filled =
      ImageVector.Builder(
          name = "circle",
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
            moveTo(23.98f, 45.05f)
            quadToRelative(-4.32f, 0f, -8.18f, -1.63f)
            reflectiveQuadTo(9.07f, 38.93f)
            reflectiveQuadTo(4.58f, 32.2f)
            reflectiveQuadTo(2.95f, 24.01f)
            quadToRelative(0f, -4.37f, 1.63f, -8.21f)
            reflectiveQuadTo(9.09f, 9.07f)
            reflectiveQuadTo(15.81f, 4.55f)
            reflectiveQuadTo(23.97f, 2.9f)
            quadToRelative(4.38f, 0f, 8.24f, 1.64f)
            reflectiveQuadToRelative(6.73f, 4.51f)
            reflectiveQuadToRelative(4.51f, 6.73f)
            reflectiveQuadToRelative(1.65f, 8.25f)
            quadToRelative(0f, 4.34f, -1.65f, 8.17f)
            reflectiveQuadToRelative(-4.52f, 6.71f)
            reflectiveQuadToRelative(-6.72f, 4.51f)
            reflectiveQuadToRelative(-8.22f, 1.63f)
            close()
          }
        }
        .build()
    return _circle_filled!!
  }

private var _circle_filled: ImageVector? = null
