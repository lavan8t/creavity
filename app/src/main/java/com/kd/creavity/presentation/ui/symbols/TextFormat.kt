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
public val text_format: ImageVector
  get() {
    if (_text_format != null) {
      return _text_format!!
    }
    _text_format =
      ImageVector.Builder(
          name = "text_format",
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
            moveTo(9.25f, 38.45f)
            verticalLineTo(33.9f)
            horizontalLineToRelative(29.5f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(9.25f)
            close()
            moveTo(13.6f, 29.4f)
            lineTo(22.25f, 7.75f)
            horizontalLineToRelative(3.8f)
            lineTo(34.7f, 29.4f)
            horizontalLineTo(30.3f)
            lineTo(28.35f, 24.15f)
            horizontalLineToRelative(-8.7f)
            lineToRelative(-1.9f, 5.25f)
            horizontalLineTo(13.6f)
            close()
            moveToRelative(6.85f, -8.2f)
            horizontalLineToRelative(7.2f)
            lineToRelative(-3.5f, -9.4f)
            horizontalLineToRelative(-0.2f)
            lineToRelative(-3.5f, 9.4f)
            close()
          }
        }
        .build()
    return _text_format!!
  }

private var _text_format: ImageVector? = null

@Suppress("CheckReturnValue")
public val text_format_filled: ImageVector
  get() {
    if (_text_format_filled != null) {
      return _text_format_filled!!
    }
    _text_format_filled =
      ImageVector.Builder(
          name = "text_format",
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
            moveTo(9.25f, 38.45f)
            verticalLineTo(33.9f)
            horizontalLineToRelative(29.5f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(9.25f)
            close()
            moveTo(13.6f, 29.4f)
            lineTo(22.25f, 7.75f)
            horizontalLineToRelative(3.8f)
            lineTo(34.7f, 29.4f)
            horizontalLineTo(30.3f)
            lineTo(28.35f, 24.15f)
            horizontalLineToRelative(-8.7f)
            lineToRelative(-1.9f, 5.25f)
            horizontalLineTo(13.6f)
            close()
            moveToRelative(6.85f, -8.2f)
            horizontalLineToRelative(7.2f)
            lineToRelative(-3.5f, -9.4f)
            horizontalLineToRelative(-0.2f)
            lineToRelative(-3.5f, 9.4f)
            close()
          }
        }
        .build()
    return _text_format_filled!!
  }

private var _text_format_filled: ImageVector? = null
