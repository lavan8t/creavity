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
public val person: ImageVector
  get() {
    if (_person != null) {
      return _person!!
    }
    _person =
      ImageVector.Builder(
          name = "person",
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
            moveTo(17.79f, 20.94f)
            quadTo(15.4f, 18.52f, 15.4f, 14.74f)
            reflectiveQuadTo(17.78f, 8.55f)
            reflectiveQuadToRelative(6.2f, -2.4f)
            reflectiveQuadToRelative(6.24f, 2.4f)
            reflectiveQuadToRelative(2.42f, 6.19f)
            reflectiveQuadToRelative(-2.42f, 6.2f)
            reflectiveQuadTo(24f, 23.35f)
            reflectiveQuadTo(17.79f, 20.94f)
            close()
            moveTo(6.9f, 41.5f)
            verticalLineTo(35.71f)
            quadToRelative(0f, -2.2f, 1.14f, -3.84f)
            reflectiveQuadTo(11f, 29.4f)
            quadToRelative(3.4f, -1.6f, 6.59f, -2.35f)
            reflectiveQuadTo(23.98f, 26.3f)
            quadToRelative(3.3f, 0f, 6.43f, 0.8f)
            reflectiveQuadToRelative(6.54f, 2.3f)
            quadToRelative(1.89f, 0.77f, 3.04f, 2.42f)
            reflectiveQuadToRelative(1.16f, 3.87f)
            verticalLineToRelative(5.8f)
            horizontalLineTo(6.9f)
            close()
            moveToRelative(4.55f, -4.55f)
            horizontalLineToRelative(25.1f)
            verticalLineTo(35.9f)
            quadToRelative(0f, -0.77f, -0.49f, -1.49f)
            reflectiveQuadTo(34.85f, 33.35f)
            quadToRelative(-3f, -1.45f, -5.5f, -1.95f)
            reflectiveQuadToRelative(-5.4f, -0.5f)
            quadToRelative(-2.8f, 0f, -5.35f, 0.53f)
            reflectiveQuadToRelative(-5.43f, 1.92f)
            quadToRelative(-0.78f, 0.33f, -1.25f, 1.06f)
            reflectiveQuadToRelative(-0.47f, 1.5f)
            verticalLineToRelative(1.05f)
            close()
            moveTo(26.88f, 17.62f)
            quadToRelative(1.18f, -1.18f, 1.18f, -2.9f)
            reflectiveQuadToRelative(-1.16f, -2.9f)
            reflectiveQuadTo(24.01f, 10.65f)
            reflectiveQuadToRelative(-2.89f, 1.18f)
            reflectiveQuadToRelative(-1.17f, 2.9f)
            reflectiveQuadToRelative(1.16f, 2.9f)
            reflectiveQuadToRelative(2.88f, 1.17f)
            reflectiveQuadToRelative(2.89f, -1.18f)
            close()
            moveTo(24f, 14.75f)
            close()
            moveToRelative(0f, 22.2f)
            close()
          }
        }
        .build()
    return _person!!
  }

private var _person: ImageVector? = null

@Suppress("CheckReturnValue")
public val person_filled: ImageVector
  get() {
    if (_person_filled != null) {
      return _person_filled!!
    }
    _person_filled =
      ImageVector.Builder(
          name = "person",
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
            moveTo(17.79f, 20.94f)
            quadTo(15.4f, 18.52f, 15.4f, 14.74f)
            reflectiveQuadTo(17.78f, 8.55f)
            reflectiveQuadToRelative(6.2f, -2.4f)
            reflectiveQuadToRelative(6.24f, 2.4f)
            reflectiveQuadToRelative(2.42f, 6.19f)
            reflectiveQuadToRelative(-2.42f, 6.2f)
            reflectiveQuadTo(24f, 23.35f)
            reflectiveQuadTo(17.79f, 20.94f)
            close()
            moveTo(6.9f, 41.5f)
            verticalLineTo(35.71f)
            quadToRelative(0f, -2.18f, 1.13f, -3.83f)
            reflectiveQuadTo(11f, 29.4f)
            quadToRelative(3.4f, -1.6f, 6.59f, -2.35f)
            reflectiveQuadTo(23.98f, 26.3f)
            quadToRelative(3.3f, 0f, 6.43f, 0.8f)
            reflectiveQuadToRelative(6.54f, 2.3f)
            quadToRelative(1.91f, 0.77f, 3.05f, 2.42f)
            reflectiveQuadToRelative(1.15f, 3.88f)
            verticalLineToRelative(5.8f)
            horizontalLineTo(6.9f)
            close()
          }
        }
        .build()
    return _person_filled!!
  }

private var _person_filled: ImageVector? = null
