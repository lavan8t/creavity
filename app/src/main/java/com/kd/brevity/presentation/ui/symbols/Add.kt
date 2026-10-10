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
public val add: ImageVector
  get() {
    if (_add != null) {
      return _add!!
    }
    _add =
      ImageVector.Builder(
          name = "add",
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
            moveTo(21.75f, 26.3f)
            horizontalLineTo(8.95f)
            verticalLineTo(21.75f)
            horizontalLineToRelative(12.8f)
            verticalLineTo(8.85f)
            horizontalLineTo(26.3f)
            verticalLineToRelative(12.9f)
            horizontalLineTo(39.1f)
            verticalLineTo(26.3f)
            horizontalLineTo(26.3f)
            verticalLineTo(39.05f)
            horizontalLineTo(21.75f)
            verticalLineTo(26.3f)
            close()
          }
        }
        .build()
    return _add!!
  }

private var _add: ImageVector? = null

@Suppress("CheckReturnValue")
public val add_filled: ImageVector
  get() {
    if (_add_filled != null) {
      return _add_filled!!
    }
    _add_filled =
      ImageVector.Builder(
          name = "add",
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
            moveTo(21.75f, 26.3f)
            horizontalLineTo(8.95f)
            verticalLineTo(21.75f)
            horizontalLineToRelative(12.8f)
            verticalLineTo(8.85f)
            horizontalLineTo(26.3f)
            verticalLineToRelative(12.9f)
            horizontalLineTo(39.1f)
            verticalLineTo(26.3f)
            horizontalLineTo(26.3f)
            verticalLineTo(39.05f)
            horizontalLineTo(21.75f)
            verticalLineTo(26.3f)
            close()
          }
        }
        .build()
    return _add_filled!!
  }

private var _add_filled: ImageVector? = null
