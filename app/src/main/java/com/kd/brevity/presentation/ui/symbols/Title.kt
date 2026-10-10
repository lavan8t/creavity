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
public val title: ImageVector
  get() {
    if (_title != null) {
      return _title!!
    }
    _title =
      ImageVector.Builder(
          name = "title",
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
            moveTo(20.75f, 41.05f)
            verticalLineTo(13.45f)
            horizontalLineTo(8.95f)
            verticalLineTo(6.9f)
            horizontalLineTo(39.1f)
            verticalLineToRelative(6.55f)
            horizontalLineTo(27.35f)
            verticalLineToRelative(27.6f)
            horizontalLineToRelative(-6.6f)
            close()
          }
        }
        .build()
    return _title!!
  }

private var _title: ImageVector? = null

@Suppress("CheckReturnValue")
public val title_filled: ImageVector
  get() {
    if (_title_filled != null) {
      return _title_filled!!
    }
    _title_filled =
      ImageVector.Builder(
          name = "title",
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
            moveTo(20.75f, 41.05f)
            verticalLineTo(13.45f)
            horizontalLineTo(8.95f)
            verticalLineTo(6.9f)
            horizontalLineTo(39.1f)
            verticalLineToRelative(6.55f)
            horizontalLineTo(27.35f)
            verticalLineToRelative(27.6f)
            horizontalLineToRelative(-6.6f)
            close()
          }
        }
        .build()
    return _title_filled!!
  }

private var _title_filled: ImageVector? = null
