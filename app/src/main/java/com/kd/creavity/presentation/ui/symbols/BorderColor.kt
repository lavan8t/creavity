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
public val border_color: ImageVector
  get() {
    if (_border_color != null) {
      return _border_color!!
    }
    _border_color =
      ImageVector.Builder(
          name = "border_color",
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
            moveTo(4.05f, 47.75f)
            verticalLineToRelative(-6.9f)
            horizontalLineToRelative(39.9f)
            verticalLineToRelative(6.9f)
            horizontalLineTo(4.05f)
            close()
            moveTo(11.55f, 31.4f)
            horizontalLineToRelative(1.5f)
            lineTo(31.6f, 12.85f)
            lineTo(30.93f, 12.06f)
            lineTo(30.2f, 11.35f)
            lineTo(11.55f, 29.9f)
            verticalLineToRelative(1.5f)
            close()
            moveToRelative(-3.4f, 3.45f)
            verticalLineToRelative(-6.3f)
            lineTo(32.45f, 4.15f)
            quadTo(32.95f, 3.7f, 33.61f, 3.42f)
            reflectiveQuadToRelative(1.3f, -0.27f)
            reflectiveQuadTo(36.2f, 3.44f)
            quadToRelative(0.65f, 0.29f, 1.2f, 0.76f)
            lineToRelative(1.4f, 1.25f)
            quadToRelative(0.6f, 0.5f, 0.85f, 1.2f)
            reflectiveQuadTo(39.9f, 7.98f)
            quadToRelative(0f, 0.66f, -0.27f, 1.34f)
            reflectiveQuadToRelative(-0.78f, 1.14f)
            lineTo(14.4f, 34.85f)
            horizontalLineTo(8.15f)
            close()
            moveTo(36.3f, 7.95f)
            lineTo(34.95f, 6.6f)
            lineTo(36.3f, 7.95f)
            close()
            moveToRelative(-4.7f, 4.9f)
            lineTo(30.93f, 12.06f)
            lineTo(30.2f, 11.35f)
            lineToRelative(1.4f, 1.5f)
            close()
          }
        }
        .build()
    return _border_color!!
  }

private var _border_color: ImageVector? = null

@Suppress("CheckReturnValue")
public val border_color_filled: ImageVector
  get() {
    if (_border_color_filled != null) {
      return _border_color_filled!!
    }
    _border_color_filled =
      ImageVector.Builder(
          name = "border_color",
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
            moveTo(4.05f, 47.75f)
            verticalLineToRelative(-6.9f)
            horizontalLineToRelative(39.9f)
            verticalLineToRelative(6.9f)
            horizontalLineTo(4.05f)
            close()
            moveTo(8.2f, 34.85f)
            verticalLineToRelative(-6.3f)
            lineTo(32.45f, 4.15f)
            quadToRelative(0.5f, -0.5f, 1.13f, -0.75f)
            reflectiveQuadTo(34.9f, 3.15f)
            reflectiveQuadToRelative(1.3f, 0.23f)
            reflectiveQuadTo(37.35f, 4.1f)
            lineTo(38.8f, 5.45f)
            quadToRelative(0.55f, 0.5f, 0.82f, 1.18f)
            reflectiveQuadTo(39.9f, 7.95f)
            quadToRelative(0f, 0.75f, -0.3f, 1.35f)
            reflectiveQuadToRelative(-0.75f, 1.1f)
            lineTo(14.4f, 34.85f)
            horizontalLineTo(8.2f)
            close()
            moveTo(34.1f, 10.2f)
            lineTo(36.4f, 7.95f)
            lineTo(35.05f, 6.55f)
            lineToRelative(-2.3f, 2.3f)
            lineTo(34.1f, 10.2f)
            close()
          }
        }
        .build()
    return _border_color_filled!!
  }

private var _border_color_filled: ImageVector? = null
