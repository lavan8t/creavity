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
public val brush: ImageVector
  get() {
    if (_brush != null) {
      return _brush!!
    }
    _brush =
      ImageVector.Builder(
          name = "brush",
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
            moveTo(10.45f, 43.25f)
            quadToRelative(-1.84f, 0f, -3.89f, -0.88f)
            reflectiveQuadTo(2.8f, 39.3f)
            quadTo(5f, 38.65f, 5.75f, 37.63f)
            reflectiveQuadTo(6.6f, 34.62f)
            quadTo(6.75f, 32.28f, 8.43f, 30.64f)
            reflectiveQuadTo(12.7f, 29f)
            quadToRelative(2.6f, 0f, 4.38f, 1.78f)
            reflectiveQuadToRelative(1.77f, 4.32f)
            quadToRelative(0f, 3.55f, -2.44f, 5.85f)
            reflectiveQuadToRelative(-5.97f, 2.3f)
            close()
            moveToRelative(-0f, -3.8f)
            quadToRelative(1.71f, 0f, 3.11f, -1.27f)
            reflectiveQuadToRelative(1.4f, -3.03f)
            quadToRelative(0f, -1f, -0.63f, -1.63f)
            reflectiveQuadTo(12.7f, 32.9f)
            reflectiveQuadToRelative(-1.63f, 0.63f)
            reflectiveQuadToRelative(-0.63f, 1.63f)
            quadToRelative(0f, 1.91f, -0.4f, 2.86f)
            reflectiveQuadTo(8.5f, 39.16f)
            quadToRelative(0.3f, -0.01f, 1f, 0.14f)
            reflectiveQuadToRelative(0.95f, 0.15f)
            close()
            moveTo(22.95f, 30.7f)
            lineToRelative(-5.6f, -5.85f)
            lineTo(37.8f, 4.4f)
            quadTo(38.46f, 3.75f, 39.3f, 3.67f)
            reflectiveQuadTo(40.9f, 4.4f)
            lineToRelative(2.6f, 2.55f)
            quadToRelative(0.75f, 0.75f, 0.7f, 1.63f)
            reflectiveQuadToRelative(-0.7f, 1.58f)
            lineTo(22.95f, 30.7f)
            close()
            moveToRelative(-10.2f, 4.45f)
            close()
          }
        }
        .build()
    return _brush!!
  }

private var _brush: ImageVector? = null

@Suppress("CheckReturnValue")
public val brush_filled: ImageVector
  get() {
    if (_brush_filled != null) {
      return _brush_filled!!
    }
    _brush_filled =
      ImageVector.Builder(
          name = "brush",
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
            moveTo(10.45f, 43.25f)
            quadToRelative(-1.85f, 0f, -3.9f, -0.88f)
            reflectiveQuadTo(2.8f, 39.3f)
            quadTo(5f, 38.65f, 5.75f, 37.63f)
            reflectiveQuadTo(6.6f, 34.62f)
            quadToRelative(0.15f, -2.38f, 1.84f, -4f)
            reflectiveQuadTo(12.71f, 29f)
            quadToRelative(2.6f, 0f, 4.37f, 1.77f)
            reflectiveQuadToRelative(1.77f, 4.36f)
            quadToRelative(0f, 3.52f, -2.44f, 5.82f)
            reflectiveQuadToRelative(-5.96f, 2.3f)
            close()
            moveTo(22.95f, 30.7f)
            lineToRelative(-5.6f, -5.85f)
            lineTo(37.8f, 4.4f)
            quadTo(38.46f, 3.75f, 39.3f, 3.67f)
            reflectiveQuadTo(40.9f, 4.4f)
            lineToRelative(2.6f, 2.55f)
            quadToRelative(0.75f, 0.75f, 0.7f, 1.63f)
            reflectiveQuadToRelative(-0.7f, 1.58f)
            lineTo(22.95f, 30.7f)
            close()
          }
        }
        .build()
    return _brush_filled!!
  }

private var _brush_filled: ImageVector? = null
