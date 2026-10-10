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
public val palette: ImageVector
  get() {
    if (_palette != null) {
      return _palette!!
    }
    _palette =
      ImageVector.Builder(
          name = "palette",
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
            moveTo(24.05f, 45.05f)
            quadToRelative(-4.35f, 0f, -8.15f, -1.65f)
            reflectiveQuadTo(9.18f, 38.85f)
            reflectiveQuadTo(4.63f, 32.13f)
            reflectiveQuadTo(3f, 24f)
            quadTo(3f, 19.55f, 4.67f, 15.74f)
            reflectiveQuadTo(9.27f, 9.09f)
            reflectiveQuadTo(16.09f, 4.57f)
            reflectiveQuadTo(24.42f, 2.9f)
            quadToRelative(4.13f, 0f, 7.87f, 1.4f)
            reflectiveQuadToRelative(6.58f, 3.9f)
            reflectiveQuadToRelative(4.54f, 5.96f)
            reflectiveQuadToRelative(1.7f, 7.39f)
            quadToRelative(0f, 5.6f, -3.1f, 9.23f)
            reflectiveQuadTo(33.25f, 34.4f)
            horizontalLineTo(30.2f)
            quadToRelative(-0.9f, 0f, -1.48f, 0.65f)
            reflectiveQuadToRelative(-0.57f, 1.5f)
            quadToRelative(0f, 1.25f, 0.52f, 1.78f)
            reflectiveQuadToRelative(0.53f, 1.63f)
            quadToRelative(0f, 2.2f, -1.46f, 3.65f)
            reflectiveQuadToRelative(-3.69f, 1.45f)
            close()
            moveTo(24f, 24f)
            close()
            moveToRelative(-9.42f, 0.52f)
            quadTo(15.3f, 23.8f, 15.3f, 22.8f)
            reflectiveQuadTo(14.58f, 21.08f)
            reflectiveQuadTo(12.85f, 20.35f)
            reflectiveQuadToRelative(-1.7f, 0.73f)
            reflectiveQuadToRelative(-0.7f, 1.72f)
            reflectiveQuadToRelative(0.7f, 1.73f)
            reflectiveQuadToRelative(1.7f, 0.73f)
            reflectiveQuadToRelative(1.72f, -0.73f)
            close()
            moveToRelative(6.09f, -8.13f)
            quadTo(21.4f, 15.68f, 21.4f, 14.64f)
            reflectiveQuadTo(20.67f, 12.92f)
            reflectiveQuadTo(18.92f, 12.25f)
            reflectiveQuadToRelative(-1.69f, 0.67f)
            reflectiveQuadToRelative(-0.68f, 1.72f)
            reflectiveQuadToRelative(0.68f, 1.75f)
            reflectiveQuadToRelative(1.69f, 0.71f)
            reflectiveQuadToRelative(1.75f, -0.71f)
            close()
            moveToRelative(10.2f, 0f)
            quadTo(31.6f, 15.68f, 31.6f, 14.64f)
            reflectiveQuadTo(30.87f, 12.92f)
            reflectiveQuadTo(29.12f, 12.25f)
            reflectiveQuadToRelative(-1.69f, 0.67f)
            reflectiveQuadToRelative(-0.68f, 1.72f)
            reflectiveQuadToRelative(0.68f, 1.75f)
            reflectiveQuadToRelative(1.69f, 0.71f)
            reflectiveQuadToRelative(1.75f, -0.71f)
            close()
            moveToRelative(6.21f, 8.13f)
            quadTo(37.8f, 23.8f, 37.8f, 22.8f)
            reflectiveQuadTo(37.08f, 21.08f)
            reflectiveQuadTo(35.33f, 20.35f)
            reflectiveQuadToRelative(-1.7f, 0.73f)
            reflectiveQuadTo(32.95f, 22.8f)
            reflectiveQuadToRelative(0.67f, 1.73f)
            reflectiveQuadToRelative(1.7f, 0.73f)
            reflectiveQuadToRelative(1.75f, -0.73f)
            close()
            moveTo(23.9f, 40.5f)
            quadToRelative(0.29f, 0f, 0.52f, -0.15f)
            reflectiveQuadToRelative(0.23f, -0.63f)
            quadToRelative(0f, -0.67f, -0.75f, -1.16f)
            reflectiveQuadTo(23.15f, 36.15f)
            quadToRelative(0f, -2.46f, 1.62f, -4.38f)
            reflectiveQuadToRelative(4.11f, -1.92f)
            horizontalLineToRelative(4.36f)
            quadToRelative(3.65f, 0f, 5.45f, -2.25f)
            reflectiveQuadToRelative(1.8f, -5.75f)
            quadToRelative(0f, -6.6f, -4.9f, -10.47f)
            reflectiveQuadTo(24.43f, 7.5f)
            quadToRelative(-7.08f, 0f, -11.98f, 4.82f)
            reflectiveQuadTo(7.55f, 24.02f)
            quadToRelative(0f, 6.88f, 4.8f, 11.68f)
            reflectiveQuadTo(23.9f, 40.5f)
            close()
          }
        }
        .build()
    return _palette!!
  }

private var _palette: ImageVector? = null

@Suppress("CheckReturnValue")
public val palette_filled: ImageVector
  get() {
    if (_palette_filled != null) {
      return _palette_filled!!
    }
    _palette_filled =
      ImageVector.Builder(
          name = "palette",
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
            moveTo(24.05f, 45.05f)
            quadToRelative(-4.35f, 0f, -8.15f, -1.65f)
            reflectiveQuadTo(9.18f, 38.85f)
            reflectiveQuadTo(4.63f, 32.13f)
            reflectiveQuadTo(3f, 24f)
            quadTo(3f, 19.55f, 4.68f, 15.7f)
            reflectiveQuadTo(9.27f, 9.01f)
            reflectiveQuadTo(16.09f, 4.54f)
            reflectiveQuadTo(24.43f, 2.9f)
            quadToRelative(4.12f, 0f, 7.86f, 1.4f)
            reflectiveQuadToRelative(6.57f, 3.9f)
            reflectiveQuadToRelative(4.54f, 5.96f)
            reflectiveQuadToRelative(1.7f, 7.39f)
            quadToRelative(0f, 5.6f, -3.1f, 9.23f)
            reflectiveQuadTo(33.25f, 34.4f)
            horizontalLineTo(30.2f)
            quadToRelative(-0.9f, 0f, -1.48f, 0.65f)
            reflectiveQuadToRelative(-0.57f, 1.5f)
            quadToRelative(0f, 0.9f, 0.52f, 1.38f)
            reflectiveQuadTo(29.2f, 39.5f)
            quadToRelative(0f, 1.7f, -1.64f, 3.63f)
            reflectiveQuadToRelative(-3.51f, 1.92f)
            close()
            moveTo(14.58f, 24.53f)
            quadTo(15.3f, 23.8f, 15.3f, 22.8f)
            reflectiveQuadTo(14.58f, 21.08f)
            reflectiveQuadTo(12.85f, 20.35f)
            reflectiveQuadToRelative(-1.7f, 0.73f)
            reflectiveQuadToRelative(-0.7f, 1.72f)
            reflectiveQuadToRelative(0.7f, 1.73f)
            reflectiveQuadToRelative(1.7f, 0.73f)
            reflectiveQuadToRelative(1.72f, -0.73f)
            close()
            moveToRelative(6.09f, -8.13f)
            quadTo(21.4f, 15.68f, 21.4f, 14.64f)
            reflectiveQuadTo(20.67f, 12.92f)
            reflectiveQuadTo(18.92f, 12.25f)
            reflectiveQuadToRelative(-1.69f, 0.67f)
            reflectiveQuadToRelative(-0.68f, 1.72f)
            reflectiveQuadToRelative(0.68f, 1.75f)
            reflectiveQuadToRelative(1.69f, 0.71f)
            reflectiveQuadToRelative(1.75f, -0.71f)
            close()
            moveToRelative(10.2f, 0f)
            quadTo(31.6f, 15.68f, 31.6f, 14.64f)
            reflectiveQuadTo(30.87f, 12.92f)
            reflectiveQuadTo(29.12f, 12.25f)
            reflectiveQuadToRelative(-1.69f, 0.67f)
            reflectiveQuadToRelative(-0.68f, 1.72f)
            reflectiveQuadToRelative(0.68f, 1.75f)
            reflectiveQuadToRelative(1.69f, 0.71f)
            reflectiveQuadToRelative(1.75f, -0.71f)
            close()
            moveToRelative(6.21f, 8.13f)
            quadTo(37.8f, 23.8f, 37.8f, 22.8f)
            reflectiveQuadTo(37.08f, 21.08f)
            reflectiveQuadTo(35.33f, 20.35f)
            reflectiveQuadToRelative(-1.7f, 0.73f)
            reflectiveQuadTo(32.95f, 22.8f)
            reflectiveQuadToRelative(0.67f, 1.73f)
            reflectiveQuadToRelative(1.7f, 0.73f)
            reflectiveQuadToRelative(1.75f, -0.73f)
            close()
          }
        }
        .build()
    return _palette_filled!!
  }

private var _palette_filled: ImageVector? = null
