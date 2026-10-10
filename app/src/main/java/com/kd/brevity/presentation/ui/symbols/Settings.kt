package com.kd.brevity.presentation.ui.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val settings: ImageVector
  get() {
    if (_settings != null) {
      return _settings!!
    }
    _settings =
      ImageVector.Builder(
          name = "settings",
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
            moveTo(18.45f, 45.05f)
            lineTo(17.4f, 38.4f)
            quadTo(16.7f, 38.2f, 15.86f, 37.71f)
            reflectiveQuadTo(14.45f, 36.75f)
            lineTo(8.25f, 39.6f)
            lineTo(2.6f, 29.6f)
            lineTo(8.25f, 25.49f)
            quadTo(8.2f, 25.25f, 8.2f, 24.79f)
            reflectiveQuadToRelative(0f, -0.81f)
            quadToRelative(0f, -0.3f, 0f, -0.7f)
            reflectiveQuadTo(8.25f, 22.5f)
            lineTo(2.6f, 18.3f)
            lineTo(8.25f, 8.4f)
            lineToRelative(6.3f, 2.85f)
            quadToRelative(0.49f, -0.37f, 1.35f, -0.86f)
            reflectiveQuadTo(17.4f, 9.7f)
            lineTo(18.45f, 2.9f)
            horizontalLineTo(29.6f)
            lineToRelative(1f, 6.75f)
            quadToRelative(0.75f, 0.2f, 1.58f, 0.7f)
            reflectiveQuadToRelative(1.38f, 0.9f)
            lineTo(39.85f, 8.4f)
            lineToRelative(5.6f, 9.9f)
            lineTo(39.7f, 22.35f)
            quadToRelative(0.05f, 0.38f, 0.13f, 0.85f)
            reflectiveQuadTo(39.9f, 24f)
            reflectiveQuadToRelative(-0.08f, 0.77f)
            reflectiveQuadTo(39.7f, 25.55f)
            lineToRelative(5.75f, 4.05f)
            lineToRelative(-5.7f, 10f)
            lineToRelative(-6.2f, -2.85f)
            quadTo(33f, 37.2f, 32.13f, 37.73f)
            reflectiveQuadTo(30.65f, 38.4f)
            lineTo(29.6f, 45.05f)
            horizontalLineTo(18.45f)
            close()
            moveToRelative(3.8f, -4.55f)
            horizontalLineToRelative(3.52f)
            lineToRelative(0.74f, -5.55f)
            quadToRelative(1.74f, -0.45f, 3.26f, -1.28f)
            reflectiveQuadTo(32.51f, 31.5f)
            lineToRelative(5.29f, 2.35f)
            lineTo(39.35f, 30.9f)
            lineTo(34.7f, 27.55f)
            quadToRelative(0.25f, -0.88f, 0.4f, -1.76f)
            reflectiveQuadTo(35.25f, 24f)
            reflectiveQuadTo(35.1f, 22.2f)
            reflectiveQuadTo(34.75f, 20.5f)
            lineTo(39.4f, 17.1f)
            lineTo(37.8f, 14.15f)
            lineTo(32.51f, 16.5f)
            quadTo(31.35f, 15.1f, 29.82f, 14.14f)
            reflectiveQuadTo(26.48f, 13f)
            lineTo(25.8f, 7.5f)
            horizontalLineTo(22.2f)
            lineTo(21.55f, 13f)
            quadToRelative(-1.75f, 0.35f, -3.32f, 1.25f)
            reflectiveQuadToRelative(-2.78f, 2.2f)
            lineToRelative(-5.19f, -2.3f)
            lineTo(8.65f, 17.1f)
            lineToRelative(4.6f, 3.3f)
            quadTo(13f, 21.25f, 12.88f, 22.15f)
            reflectiveQuadToRelative(-0.13f, 1.88f)
            reflectiveQuadToRelative(0.13f, 1.87f)
            reflectiveQuadToRelative(0.38f, 1.65f)
            lineTo(8.65f, 30.9f)
            lineToRelative(1.6f, 2.95f)
            lineToRelative(5.2f, -2.35f)
            quadToRelative(1.25f, 1.35f, 2.8f, 2.25f)
            reflectiveQuadTo(21.55f, 35f)
            lineToRelative(0.7f, 5.5f)
            close()
            moveToRelative(1.7f, -10.15f)
            quadToRelative(2.64f, 0f, 4.49f, -1.83f)
            reflectiveQuadTo(30.3f, 24.04f)
            reflectiveQuadTo(28.44f, 19.52f)
            reflectiveQuadTo(23.95f, 17.65f)
            quadToRelative(-2.7f, 0f, -4.53f, 1.87f)
            reflectiveQuadTo(17.6f, 24.03f)
            reflectiveQuadToRelative(1.82f, 4.48f)
            reflectiveQuadToRelative(4.53f, 1.84f)
            close()
            moveTo(24.05f, 24f)
            close()
          }
        }
        .build()
    return _settings!!
  }

private var _settings: ImageVector? = null

@Suppress("CheckReturnValue")
public val settings_filled: ImageVector
  get() {
    if (_settings_filled != null) {
      return _settings_filled!!
    }
    _settings_filled =
      ImageVector.Builder(
          name = "settings",
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
            moveTo(18.45f, 45.05f)
            lineTo(17.4f, 38.4f)
            quadTo(16.7f, 38.2f, 15.85f, 37.7f)
            reflectiveQuadToRelative(-1.4f, -0.95f)
            lineTo(8.25f, 39.6f)
            lineTo(2.6f, 29.6f)
            lineTo(8.25f, 25.5f)
            quadTo(8.2f, 25.25f, 8.2f, 24.78f)
            reflectiveQuadTo(8.2f, 24f)
            reflectiveQuadToRelative(0f, -0.73f)
            reflectiveQuadTo(8.25f, 22.5f)
            lineTo(2.6f, 18.3f)
            lineTo(8.25f, 8.4f)
            lineToRelative(6.3f, 2.85f)
            quadTo(15.05f, 10.9f, 15.9f, 10.4f)
            reflectiveQuadTo(17.4f, 9.7f)
            lineTo(18.45f, 2.9f)
            horizontalLineTo(29.6f)
            lineToRelative(1f, 6.75f)
            quadToRelative(0.75f, 0.2f, 1.58f, 0.7f)
            reflectiveQuadToRelative(1.38f, 0.9f)
            lineTo(39.85f, 8.4f)
            lineToRelative(5.6f, 9.9f)
            lineTo(39.7f, 22.35f)
            quadToRelative(0.05f, 0.4f, 0.13f, 0.85f)
            reflectiveQuadTo(39.9f, 24f)
            reflectiveQuadToRelative(-0.08f, 0.77f)
            reflectiveQuadTo(39.7f, 25.55f)
            lineToRelative(5.75f, 4.05f)
            lineToRelative(-5.7f, 10f)
            lineToRelative(-6.2f, -2.85f)
            quadTo(33f, 37.2f, 32.13f, 37.73f)
            reflectiveQuadTo(30.65f, 38.4f)
            lineTo(29.6f, 45.05f)
            horizontalLineTo(18.45f)
            close()
            moveToRelative(5.5f, -14.7f)
            quadToRelative(2.65f, 0f, 4.5f, -1.85f)
            reflectiveQuadTo(30.3f, 24.03f)
            reflectiveQuadToRelative(-1.85f, -4.5f)
            reflectiveQuadToRelative(-4.5f, -1.88f)
            quadToRelative(-2.7f, 0f, -4.53f, 1.88f)
            reflectiveQuadToRelative(-1.82f, 4.5f)
            reflectiveQuadToRelative(1.82f, 4.48f)
            reflectiveQuadToRelative(4.53f, 1.85f)
            close()
          }
        }
        .build()
    return _settings_filled!!
  }

private var _settings_filled: ImageVector? = null
