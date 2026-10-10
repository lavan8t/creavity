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
public val devices: ImageVector
  get() {
    if (_devices != null) {
      return _devices!!
    }
    _devices =
      ImageVector.Builder(
          name = "devices",
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
            moveTo(24.45f, 20.2f)
            close()
            moveTo(2.95f, 41.05f)
            verticalLineTo(36.5f)
            horizontalLineToRelative(21.5f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(2.95f)
            close()
            moveTo(9.5f, 33.5f)
            quadToRelative(-1.91f, 0f, -3.26f, -1.34f)
            reflectiveQuadTo(4.9f, 28.9f)
            verticalLineTo(11.5f)
            quadTo(4.9f, 9.62f, 6.24f, 8.26f)
            reflectiveQuadTo(9.5f, 6.9f)
            horizontalLineTo(38.55f)
            quadToRelative(1.86f, 0f, 3.2f, 1.33f)
            reflectiveQuadToRelative(1.35f, 3.22f)
            horizontalLineTo(9.45f)
            verticalLineTo(28.9f)
            horizontalLineToRelative(15f)
            verticalLineToRelative(4.6f)
            horizontalLineTo(9.5f)
            close()
            moveToRelative(31.75f, 3.75f)
            verticalLineToRelative(-19f)
            horizontalLineToRelative(-10f)
            verticalLineToRelative(19f)
            horizontalLineToRelative(10f)
            close()
            moveToRelative(-10.4f, 3.8f)
            quadToRelative(-1.45f, 0f, -2.43f, -0.97f)
            reflectiveQuadTo(27.45f, 37.65f)
            verticalLineTo(17.9f)
            quadToRelative(0f, -1.47f, 0.97f, -2.46f)
            reflectiveQuadToRelative(2.43f, -0.99f)
            horizontalLineTo(41.7f)
            quadToRelative(1.39f, 0f, 2.39f, 0.99f)
            reflectiveQuadTo(45.1f, 17.9f)
            verticalLineTo(37.65f)
            quadToRelative(0f, 1.45f, -1.01f, 2.43f)
            reflectiveQuadTo(41.7f, 41.05f)
            horizontalLineTo(30.85f)
            close()
            moveTo(36.3f, 23.9f)
            quadToRelative(0.74f, 0f, 1.3f, -0.55f)
            reflectiveQuadTo(38.15f, 22.1f)
            quadToRelative(0f, -0.74f, -0.55f, -1.3f)
            reflectiveQuadTo(36.26f, 20.25f)
            quadToRelative(-0.66f, 0f, -1.21f, 0.55f)
            reflectiveQuadTo(34.5f, 22.14f)
            quadToRelative(0f, 0.66f, 0.55f, 1.21f)
            reflectiveQuadTo(36.3f, 23.9f)
            close()
            moveToRelative(0f, 3.9f)
            close()
          }
        }
        .build()
    return _devices!!
  }

private var _devices: ImageVector? = null

@Suppress("CheckReturnValue")
public val devices_filled: ImageVector
  get() {
    if (_devices_filled != null) {
      return _devices_filled!!
    }
    _devices_filled =
      ImageVector.Builder(
          name = "devices",
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
            moveTo(9.5f, 33.5f)
            quadToRelative(-1.91f, 0f, -3.26f, -1.34f)
            reflectiveQuadTo(4.9f, 28.9f)
            verticalLineTo(11.5f)
            quadTo(4.9f, 9.62f, 6.24f, 8.26f)
            reflectiveQuadTo(9.5f, 6.9f)
            horizontalLineTo(38.55f)
            quadToRelative(1.86f, 0f, 3.2f, 1.33f)
            reflectiveQuadToRelative(1.35f, 3.22f)
            horizontalLineTo(30.81f)
            quadToRelative(-2.71f, 0f, -4.54f, 1.94f)
            reflectiveQuadToRelative(-1.83f, 4.56f)
            verticalLineTo(33.5f)
            horizontalLineTo(9.5f)
            close()
            moveToRelative(21.3f, 7.55f)
            quadToRelative(-1.51f, 0f, -2.43f, -0.96f)
            reflectiveQuadTo(27.45f, 37.7f)
            verticalLineTo(17.95f)
            quadToRelative(0f, -1.4f, 0.92f, -2.45f)
            reflectiveQuadTo(30.8f, 14.45f)
            horizontalLineTo(41.56f)
            quadToRelative(1.39f, 0f, 2.47f, 1.04f)
            reflectiveQuadToRelative(1.07f, 2.46f)
            verticalLineTo(37.7f)
            quadToRelative(0f, 1.46f, -1.07f, 2.4f)
            reflectiveQuadToRelative(-2.47f, 0.95f)
            horizontalLineTo(30.8f)
            close()
            moveToRelative(-27.85f, 0f)
            verticalLineTo(36.5f)
            horizontalLineToRelative(21.5f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(2.95f)
            close()
            moveTo(36.2f, 23.85f)
            quadToRelative(0.74f, 0f, 1.24f, -0.56f)
            reflectiveQuadToRelative(0.51f, -1.27f)
            quadToRelative(0f, -0.75f, -0.53f, -1.28f)
            reflectiveQuadTo(36.18f, 20.2f)
            quadToRelative(-0.63f, 0f, -1.2f, 0.53f)
            reflectiveQuadTo(34.4f, 22.02f)
            quadToRelative(0f, 0.71f, 0.58f, 1.27f)
            reflectiveQuadToRelative(1.23f, 0.56f)
            close()
          }
        }
        .build()
    return _devices_filled!!
  }

private var _devices_filled: ImageVector? = null
