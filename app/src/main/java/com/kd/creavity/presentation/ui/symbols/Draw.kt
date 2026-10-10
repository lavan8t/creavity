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
public val draw: ImageVector
  get() {
    if (_draw != null) {
      return _draw!!
    }
    _draw =
      ImageVector.Builder(
          name = "draw",
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
            moveTo(6.95f, 43.05f)
            verticalLineTo(36.8f)
            lineTo(37.6f, 6.2f)
            quadTo(38.13f, 5.6f, 38.78f, 5.25f)
            reflectiveQuadTo(40.15f, 4.9f)
            quadToRelative(0.66f, 0f, 1.38f, 0.35f)
            reflectiveQuadTo(42.85f, 6.2f)
            lineTo(43.9f, 7.15f)
            quadToRelative(0.55f, 0.45f, 0.88f, 1.2f)
            reflectiveQuadToRelative(0.32f, 1.4f)
            quadToRelative(0f, 0.69f, -0.29f, 1.39f)
            reflectiveQuadToRelative(-0.93f, 1.28f)
            lineTo(13.25f, 43.05f)
            horizontalLineTo(6.95f)
            close()
            moveToRelative(3.45f, -3.4f)
            horizontalLineToRelative(1.5f)
            lineTo(35.05f, 16.5f)
            lineToRelative(-0.8f, -0.75f)
            lineToRelative(-0.7f, -0.8f)
            lineTo(10.4f, 38.15f)
            verticalLineToRelative(1.5f)
            close()
            moveTo(41.7f, 9.8f)
            lineTo(40.2f, 8.3f)
            lineToRelative(1.5f, 1.5f)
            close()
            moveToRelative(-7.45f, 5.95f)
            lineToRelative(-0.7f, -0.8f)
            lineToRelative(1.5f, 1.55f)
            lineToRelative(-0.8f, -0.75f)
            close()
            moveToRelative(-7.3f, 27.3f)
            quadToRelative(4.05f, 0f, 7.13f, -1.84f)
            reflectiveQuadToRelative(3.07f, -5.38f)
            quadToRelative(0f, -2.03f, -1.48f, -3.73f)
            reflectiveQuadTo(31.1f, 29.4f)
            lineToRelative(-2.7f, 2.7f)
            quadToRelative(2.55f, 0.65f, 3.95f, 1.7f)
            reflectiveQuadToRelative(1.4f, 2.01f)
            quadToRelative(0f, 1.64f, -1.98f, 2.74f)
            reflectiveQuadToRelative(-4.82f, 1.1f)
            quadToRelative(-0.69f, 0f, -1.19f, 0.48f)
            reflectiveQuadTo(25.25f, 41.3f)
            quadToRelative(0f, 0.73f, 0.51f, 1.24f)
            reflectiveQuadToRelative(1.19f, 0.51f)
            close()
            moveTo(12.3f, 27.1f)
            lineToRelative(2.9f, -2.85f)
            quadTo(11.6f, 23.45f, 9.98f, 22.7f)
            reflectiveQuadTo(8.35f, 21.08f)
            quadToRelative(0f, -0.62f, 1f, -1.4f)
            reflectiveQuadTo(13.8f, 17.8f)
            quadToRelative(4.25f, -1.3f, 5.75f, -2.82f)
            reflectiveQuadToRelative(1.5f, -3.83f)
            quadToRelative(0f, -2.82f, -2.12f, -4.53f)
            reflectiveQuadTo(13.3f, 4.9f)
            quadTo(11f, 4.9f, 9.28f, 5.67f)
            reflectiveQuadTo(6.65f, 7.54f)
            quadTo(6.15f, 8.1f, 6.2f, 8.77f)
            reflectiveQuadTo(6.91f, 9.9f)
            quadToRelative(0.46f, 0.4f, 1.17f, 0.38f)
            reflectiveQuadTo(9.25f, 9.7f)
            quadTo(10f, 8.95f, 10.9f, 8.65f)
            reflectiveQuadToRelative(2.39f, -0.3f)
            quadToRelative(2.26f, 0f, 3.31f, 0.84f)
            reflectiveQuadToRelative(1.05f, 1.97f)
            quadToRelative(0f, 1.04f, -0.93f, 1.79f)
            reflectiveQuadToRelative(-4.23f, 1.7f)
            quadToRelative(-5f, 1.7f, -6.28f, 3.2f)
            reflectiveQuadTo(4.95f, 21.1f)
            quadToRelative(0f, 2.12f, 1.65f, 3.66f)
            reflectiveQuadToRelative(5.7f, 2.34f)
            close()
          }
        }
        .build()
    return _draw!!
  }

private var _draw: ImageVector? = null

@Suppress("CheckReturnValue")
public val draw_filled: ImageVector
  get() {
    if (_draw_filled != null) {
      return _draw_filled!!
    }
    _draw_filled =
      ImageVector.Builder(
          name = "draw",
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
            moveTo(6.95f, 43.05f)
            verticalLineTo(36.8f)
            lineTo(37.6f, 6.2f)
            quadTo(38.15f, 5.6f, 38.8f, 5.25f)
            reflectiveQuadTo(40.15f, 4.9f)
            reflectiveQuadToRelative(1.4f, 0.35f)
            reflectiveQuadToRelative(1.3f, 0.95f)
            lineTo(43.9f, 7.15f)
            quadToRelative(0.55f, 0.45f, 0.88f, 1.2f)
            reflectiveQuadToRelative(0.32f, 1.4f)
            quadToRelative(0f, 0.7f, -0.3f, 1.4f)
            reflectiveQuadTo(43.9f, 12.4f)
            lineTo(13.25f, 43.05f)
            horizontalLineTo(6.95f)
            close()
            moveTo(37.15f, 14.3f)
            lineTo(41.7f, 9.8f)
            lineTo(40.2f, 8.3f)
            lineToRelative(-4.45f, 4.55f)
            lineToRelative(1.4f, 1.45f)
            close()
            moveTo(26.95f, 43.05f)
            quadToRelative(4.05f, 0f, 7.13f, -1.85f)
            reflectiveQuadToRelative(3.07f, -5.4f)
            quadToRelative(0f, -2f, -1.47f, -3.7f)
            reflectiveQuadTo(31.1f, 29.4f)
            lineToRelative(-2.7f, 2.7f)
            quadToRelative(2.55f, 0.65f, 3.95f, 1.7f)
            reflectiveQuadToRelative(1.4f, 2f)
            quadToRelative(0f, 1.65f, -1.97f, 2.75f)
            reflectiveQuadToRelative(-4.82f, 1.1f)
            quadToRelative(-0.7f, 0f, -1.2f, 0.48f)
            reflectiveQuadToRelative(-0.5f, 1.17f)
            quadToRelative(0f, 0.75f, 0.5f, 1.25f)
            reflectiveQuadToRelative(1.2f, 0.5f)
            close()
            moveTo(12.3f, 27.1f)
            lineToRelative(2.9f, -2.85f)
            quadTo(11.6f, 23.45f, 9.98f, 22.7f)
            reflectiveQuadTo(8.35f, 21.1f)
            quadToRelative(0f, -0.65f, 1f, -1.43f)
            reflectiveQuadTo(13.8f, 17.8f)
            quadToRelative(4.25f, -1.3f, 5.75f, -2.82f)
            reflectiveQuadToRelative(1.5f, -3.83f)
            quadToRelative(0f, -2.8f, -2.13f, -4.52f)
            reflectiveQuadTo(13.3f, 4.9f)
            quadTo(11f, 4.9f, 9.28f, 5.67f)
            reflectiveQuadTo(6.65f, 7.55f)
            quadTo(6.15f, 8.1f, 6.2f, 8.77f)
            reflectiveQuadTo(6.9f, 9.9f)
            quadToRelative(0.45f, 0.4f, 1.18f, 0.38f)
            reflectiveQuadTo(9.25f, 9.7f)
            quadTo(10f, 8.95f, 10.9f, 8.65f)
            reflectiveQuadToRelative(2.4f, -0.3f)
            quadToRelative(2.25f, 0f, 3.3f, 0.85f)
            reflectiveQuadToRelative(1.05f, 1.95f)
            quadToRelative(0f, 1.05f, -0.93f, 1.8f)
            reflectiveQuadToRelative(-4.23f, 1.7f)
            quadToRelative(-5f, 1.7f, -6.28f, 3.2f)
            reflectiveQuadTo(4.95f, 21.1f)
            quadToRelative(0f, 2.1f, 1.65f, 3.65f)
            reflectiveQuadToRelative(5.7f, 2.35f)
            close()
          }
        }
        .build()
    return _draw_filled!!
  }

private var _draw_filled: ImageVector? = null
