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
public val map: ImageVector
  get() {
    if (_map != null) {
      return _map!!
    }
    _map =
      ImageVector.Builder(
          name = "map",
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
            moveTo(30.6f, 43.1f)
            lineTo(17.5f, 38.5f)
            lineTo(9.1f, 41.9f)
            quadTo(7.6f, 42.6f, 6.28f, 41.73f)
            reflectiveQuadTo(4.95f, 39.15f)
            verticalLineTo(11.6f)
            quadToRelative(0f, -1.1f, 0.58f, -1.93f)
            reflectiveQuadTo(7.15f, 8.5f)
            lineTo(17.5f, 4.9f)
            lineTo(30.6f, 9.5f)
            lineTo(38.95f, 6.1f)
            quadTo(40.4f, 5.4f, 41.78f, 6.27f)
            reflectiveQuadTo(43.15f, 8.8f)
            verticalLineTo(36.75f)
            quadToRelative(0f, 1.05f, -0.65f, 1.75f)
            reflectiveQuadToRelative(-1.55f, 0.95f)
            lineTo(30.6f, 43.1f)
            close()
            moveTo(29f, 37.7f)
            verticalLineTo(13.55f)
            lineTo(19.05f, 10.2f)
            verticalLineTo(34.35f)
            lineTo(29f, 37.7f)
            close()
            moveToRelative(2.85f, 0f)
            lineToRelative(6.7f, -2.15f)
            verticalLineTo(11f)
            lineToRelative(-6.7f, 2.55f)
            verticalLineTo(37.7f)
            close()
            moveTo(9.5f, 36.95f)
            lineToRelative(6.7f, -2.6f)
            verticalLineTo(10.2f)
            lineTo(9.5f, 12.45f)
            verticalLineToRelative(24.5f)
            close()
            moveTo(31.85f, 13.55f)
            verticalLineTo(37.7f)
            verticalLineTo(13.55f)
            close()
            moveTo(16.2f, 10.2f)
            verticalLineTo(34.35f)
            verticalLineTo(10.2f)
            close()
          }
        }
        .build()
    return _map!!
  }

private var _map: ImageVector? = null

@Suppress("CheckReturnValue")
public val map_filled: ImageVector
  get() {
    if (_map_filled != null) {
      return _map_filled!!
    }
    _map_filled =
      ImageVector.Builder(
          name = "map",
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
            moveTo(30.6f, 43.1f)
            lineTo(17.5f, 38.5f)
            lineTo(9.1f, 41.9f)
            quadTo(7.6f, 42.6f, 6.28f, 41.73f)
            reflectiveQuadTo(4.95f, 39.15f)
            verticalLineTo(11.6f)
            quadToRelative(0f, -1.1f, 0.58f, -1.93f)
            reflectiveQuadTo(7.15f, 8.5f)
            lineTo(17.5f, 4.9f)
            lineTo(30.6f, 9.5f)
            lineTo(38.95f, 6.1f)
            quadTo(40.4f, 5.4f, 41.78f, 6.27f)
            reflectiveQuadTo(43.15f, 8.8f)
            verticalLineTo(36.75f)
            quadToRelative(0f, 1.05f, -0.65f, 1.75f)
            reflectiveQuadToRelative(-1.55f, 0.95f)
            lineTo(30.6f, 43.1f)
            close()
            moveTo(29f, 37.7f)
            verticalLineTo(13.55f)
            lineTo(19.05f, 10.2f)
            verticalLineTo(34.35f)
            lineTo(29f, 37.7f)
            close()
          }
        }
        .build()
    return _map_filled!!
  }

private var _map_filled: ImageVector? = null
