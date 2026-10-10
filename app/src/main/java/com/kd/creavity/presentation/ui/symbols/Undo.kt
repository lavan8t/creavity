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
public val undo: ImageVector
  get() {
    if (_undo != null) {
      return _undo!!
    }
    _undo =
      ImageVector.Builder(
          name = "undo",
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
            moveTo(13.45f, 39.05f)
            verticalLineTo(34.5f)
            horizontalLineToRelative(15.7f)
            quadToRelative(3.1f, 0f, 5.22f, -2.17f)
            reflectiveQuadTo(36.5f, 27.1f)
            reflectiveQuadTo(34.38f, 21.83f)
            reflectiveQuadTo(29.15f, 19.6f)
            horizontalLineTo(15.65f)
            lineToRelative(4.95f, 5f)
            lineToRelative(-3.2f, 3.2f)
            lineTo(6.95f, 17.35f)
            lineTo(17.4f, 6.9f)
            lineToRelative(3.2f, 3.15f)
            lineToRelative(-4.95f, 5f)
            horizontalLineTo(29.1f)
            quadToRelative(5f, 0f, 8.5f, 3.5f)
            reflectiveQuadToRelative(3.5f, 8.5f)
            reflectiveQuadToRelative(-3.5f, 8.5f)
            reflectiveQuadToRelative(-8.5f, 3.5f)
            horizontalLineTo(13.45f)
            close()
          }
        }
        .build()
    return _undo!!
  }

private var _undo: ImageVector? = null

@Suppress("CheckReturnValue")
public val undo_filled: ImageVector
  get() {
    if (_undo_filled != null) {
      return _undo_filled!!
    }
    _undo_filled =
      ImageVector.Builder(
          name = "undo",
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
            moveTo(13.45f, 39.05f)
            verticalLineTo(34.5f)
            horizontalLineToRelative(15.7f)
            quadToRelative(3.1f, 0f, 5.22f, -2.17f)
            reflectiveQuadTo(36.5f, 27.1f)
            reflectiveQuadTo(34.38f, 21.83f)
            reflectiveQuadTo(29.15f, 19.6f)
            horizontalLineTo(15.65f)
            lineToRelative(4.95f, 5f)
            lineToRelative(-3.2f, 3.2f)
            lineTo(6.95f, 17.35f)
            lineTo(17.4f, 6.9f)
            lineToRelative(3.2f, 3.15f)
            lineToRelative(-4.95f, 5f)
            horizontalLineTo(29.1f)
            quadToRelative(5f, 0f, 8.5f, 3.5f)
            reflectiveQuadToRelative(3.5f, 8.5f)
            reflectiveQuadToRelative(-3.5f, 8.5f)
            reflectiveQuadToRelative(-8.5f, 3.5f)
            horizontalLineTo(13.45f)
            close()
          }
        }
        .build()
    return _undo_filled!!
  }

private var _undo_filled: ImageVector? = null
