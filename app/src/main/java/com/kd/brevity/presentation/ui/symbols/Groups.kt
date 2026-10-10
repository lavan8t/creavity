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
public val groups: ImageVector
  get() {
    if (_groups != null) {
      return _groups!!
    }
    _groups =
      ImageVector.Builder(
          name = "groups",
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
            moveTo(-0.5f, 36.5f)
            verticalLineTo(33.3f)
            quadToRelative(0f, -2.1f, 2.17f, -3.45f)
            reflectiveQuadTo(7.52f, 28.5f)
            quadToRelative(0.33f, 0f, 0.68f, 0f)
            reflectiveQuadToRelative(0.65f, 0.03f)
            quadTo(8.4f, 29.55f, 8.12f, 30.56f)
            reflectiveQuadTo(7.83f, 32.74f)
            verticalLineTo(36.5f)
            horizontalLineTo(-0.5f)
            close()
            moveToRelative(12f, 0f)
            verticalLineTo(32.75f)
            quadToRelative(0f, -1.8f, 0.9f, -3.21f)
            reflectiveQuadTo(14.95f, 27.1f)
            quadToRelative(1.69f, -1f, 4f, -1.55f)
            reflectiveQuadTo(23.98f, 25f)
            quadToRelative(2.77f, 0f, 5.08f, 0.55f)
            reflectiveQuadToRelative(3.99f, 1.55f)
            quadToRelative(1.65f, 1.05f, 2.55f, 2.45f)
            reflectiveQuadToRelative(0.9f, 3.2f)
            verticalLineTo(36.5f)
            horizontalLineToRelative(-25f)
            close()
            moveToRelative(28.65f, 0f)
            verticalLineTo(32.72f)
            quadToRelative(0f, -1.13f, -0.25f, -2.19f)
            reflectiveQuadToRelative(-0.65f, -2f)
            quadToRelative(0.3f, -0.04f, 0.61f, -0.04f)
            reflectiveQuadToRelative(0.64f, 0f)
            quadToRelative(3.67f, 0f, 5.84f, 1.31f)
            reflectiveQuadToRelative(2.16f, 3.54f)
            verticalLineTo(36.5f)
            horizontalLineTo(40.15f)
            close()
            moveTo(15.95f, 32.65f)
            horizontalLineToRelative(16.1f)
            verticalLineToRelative(-0.3f)
            quadToRelative(-0.4f, -1.6f, -2.53f, -2.58f)
            reflectiveQuadTo(24f, 28.8f)
            reflectiveQuadToRelative(-5.55f, 1f)
            reflectiveQuadToRelative(-2.5f, 2.6f)
            verticalLineToRelative(0.25f)
            close()
            moveTo(7.49f, 27.1f)
            quadToRelative(-1.54f, 0f, -2.67f, -1.11f)
            reflectiveQuadTo(3.7f, 23.3f)
            quadToRelative(0f, -1.58f, 1.13f, -2.69f)
            reflectiveQuadTo(7.5f, 19.5f)
            quadToRelative(1.55f, 0f, 2.7f, 1.11f)
            reflectiveQuadToRelative(1.15f, 2.7f)
            quadToRelative(0f, 1.55f, -1.12f, 2.67f)
            reflectiveQuadTo(7.49f, 27.1f)
            close()
            moveToRelative(33f, 0f)
            quadToRelative(-1.54f, 0f, -2.67f, -1.11f)
            reflectiveQuadTo(36.7f, 23.3f)
            quadToRelative(0f, -1.58f, 1.13f, -2.69f)
            reflectiveQuadTo(40.5f, 19.5f)
            quadToRelative(1.55f, 0f, 2.7f, 1.11f)
            reflectiveQuadToRelative(1.15f, 2.7f)
            quadToRelative(0f, 1.55f, -1.12f, 2.67f)
            reflectiveQuadTo(40.49f, 27.1f)
            close()
            moveTo(24.05f, 23.5f)
            quadToRelative(-2.67f, 0f, -4.61f, -1.92f)
            reflectiveQuadTo(17.5f, 17f)
            quadToRelative(0f, -2.75f, 1.94f, -4.62f)
            reflectiveQuadTo(24.05f, 10.5f)
            quadToRelative(2.75f, 0f, 4.6f, 1.86f)
            reflectiveQuadToRelative(1.85f, 4.62f)
            quadToRelative(0f, 2.68f, -1.85f, 4.6f)
            reflectiveQuadToRelative(-4.6f, 1.92f)
            close()
            moveToRelative(0f, -3.85f)
            quadToRelative(1f, 0f, 1.83f, -0.78f)
            reflectiveQuadTo(26.7f, 16.95f)
            quadToRelative(0f, -1.05f, -0.84f, -1.85f)
            reflectiveQuadTo(24f, 14.3f)
            quadToRelative(-1.15f, 0f, -1.9f, 0.8f)
            reflectiveQuadToRelative(-0.75f, 1.89f)
            quadToRelative(0f, 1.11f, 0.75f, 1.89f)
            reflectiveQuadToRelative(1.95f, 0.77f)
            close()
            moveTo(24f, 32.65f)
            close()
            moveTo(24f, 17f)
            close()
          }
        }
        .build()
    return _groups!!
  }

private var _groups: ImageVector? = null

@Suppress("CheckReturnValue")
public val groups_filled: ImageVector
  get() {
    if (_groups_filled != null) {
      return _groups_filled!!
    }
    _groups_filled =
      ImageVector.Builder(
          name = "groups",
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
            moveTo(-0.5f, 36.5f)
            verticalLineTo(33.33f)
            quadToRelative(0f, -2.13f, 2.17f, -3.48f)
            reflectiveQuadTo(7.52f, 28.5f)
            quadToRelative(0.33f, 0f, 0.68f, 0f)
            reflectiveQuadToRelative(0.65f, 0.03f)
            quadTo(8.4f, 29.55f, 8.12f, 30.56f)
            reflectiveQuadTo(7.83f, 32.74f)
            verticalLineTo(36.5f)
            horizontalLineTo(-0.5f)
            close()
            moveToRelative(12f, 0f)
            verticalLineTo(32.75f)
            quadToRelative(0f, -1.8f, 0.9f, -3.21f)
            reflectiveQuadTo(14.95f, 27.1f)
            quadToRelative(1.69f, -1f, 4f, -1.55f)
            reflectiveQuadTo(23.98f, 25f)
            quadToRelative(2.77f, 0f, 5.08f, 0.55f)
            reflectiveQuadToRelative(3.99f, 1.55f)
            quadToRelative(1.65f, 1.05f, 2.55f, 2.45f)
            reflectiveQuadToRelative(0.9f, 3.2f)
            verticalLineTo(36.5f)
            horizontalLineToRelative(-25f)
            close()
            moveToRelative(28.65f, 0f)
            verticalLineTo(32.72f)
            quadToRelative(0f, -1.15f, -0.25f, -2.2f)
            reflectiveQuadTo(39.25f, 28.54f)
            quadToRelative(0.3f, -0.04f, 0.62f, -0.04f)
            reflectiveQuadToRelative(0.63f, 0f)
            quadToRelative(3.7f, 0f, 5.85f, 1.32f)
            reflectiveQuadToRelative(2.15f, 3.53f)
            verticalLineTo(36.5f)
            horizontalLineTo(40.15f)
            close()
            moveTo(7.49f, 27.1f)
            quadToRelative(-1.54f, 0f, -2.67f, -1.11f)
            reflectiveQuadTo(3.7f, 23.32f)
            quadTo(3.7f, 21.7f, 4.83f, 20.6f)
            reflectiveQuadTo(7.5f, 19.5f)
            quadToRelative(1.55f, 0f, 2.7f, 1.1f)
            reflectiveQuadToRelative(1.15f, 2.73f)
            quadToRelative(0f, 1.53f, -1.13f, 2.65f)
            reflectiveQuadTo(7.49f, 27.1f)
            close()
            moveToRelative(33f, 0f)
            quadToRelative(-1.54f, 0f, -2.67f, -1.11f)
            reflectiveQuadTo(36.7f, 23.32f)
            quadToRelative(0f, -1.62f, 1.13f, -2.72f)
            reflectiveQuadTo(40.5f, 19.5f)
            quadToRelative(1.55f, 0f, 2.7f, 1.1f)
            reflectiveQuadToRelative(1.15f, 2.73f)
            quadToRelative(0f, 1.53f, -1.13f, 2.65f)
            reflectiveQuadTo(40.49f, 27.1f)
            close()
            moveTo(24.05f, 23.5f)
            quadToRelative(-2.67f, 0f, -4.61f, -1.92f)
            reflectiveQuadTo(17.5f, 17f)
            quadToRelative(0f, -2.75f, 1.94f, -4.62f)
            reflectiveQuadTo(24.05f, 10.5f)
            quadToRelative(2.75f, 0f, 4.6f, 1.86f)
            reflectiveQuadToRelative(1.85f, 4.62f)
            quadToRelative(0f, 2.68f, -1.85f, 4.6f)
            reflectiveQuadToRelative(-4.6f, 1.92f)
            close()
          }
        }
        .build()
    return _groups_filled!!
  }

private var _groups_filled: ImageVector? = null
