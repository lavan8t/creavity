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
public val redo: ImageVector
  get() {
    if (_redo != null) {
      return _redo!!
    }
    _redo =
      ImageVector.Builder(
          name = "redo",
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
            moveTo(18.9f, 39.05f)
            quadToRelative(-5f, 0f, -8.48f, -3.47f)
            reflectiveQuadTo(6.95f, 27.1f)
            reflectiveQuadToRelative(3.48f, -8.52f)
            reflectiveQuadTo(18.9f, 15.05f)
            horizontalLineTo(32.4f)
            lineToRelative(-5f, -5f)
            lineTo(30.6f, 6.9f)
            lineTo(41.1f, 17.35f)
            lineTo(30.6f, 27.8f)
            lineTo(27.4f, 24.6f)
            lineToRelative(5f, -5f)
            horizontalLineTo(18.85f)
            quadToRelative(-3.1f, 0f, -5.23f, 2.2f)
            reflectiveQuadTo(11.5f, 27.08f)
            reflectiveQuadToRelative(2.13f, 5.25f)
            reflectiveQuadToRelative(5.23f, 2.17f)
            horizontalLineTo(34.6f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(18.9f)
            close()
          }
        }
        .build()
    return _redo!!
  }

private var _redo: ImageVector? = null

@Suppress("CheckReturnValue")
public val redo_filled: ImageVector
  get() {
    if (_redo_filled != null) {
      return _redo_filled!!
    }
    _redo_filled =
      ImageVector.Builder(
          name = "redo",
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
            moveTo(18.9f, 39.05f)
            quadToRelative(-5f, 0f, -8.48f, -3.47f)
            reflectiveQuadTo(6.95f, 27.1f)
            reflectiveQuadToRelative(3.48f, -8.52f)
            reflectiveQuadTo(18.9f, 15.05f)
            horizontalLineTo(32.4f)
            lineToRelative(-5f, -5f)
            lineTo(30.6f, 6.9f)
            lineTo(41.1f, 17.35f)
            lineTo(30.6f, 27.8f)
            lineTo(27.4f, 24.6f)
            lineToRelative(5f, -5f)
            horizontalLineTo(18.85f)
            quadToRelative(-3.1f, 0f, -5.23f, 2.2f)
            reflectiveQuadTo(11.5f, 27.08f)
            reflectiveQuadToRelative(2.13f, 5.25f)
            reflectiveQuadToRelative(5.23f, 2.17f)
            horizontalLineTo(34.6f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(18.9f)
            close()
          }
        }
        .build()
    return _redo_filled!!
  }

private var _redo_filled: ImageVector? = null
