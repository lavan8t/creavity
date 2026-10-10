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
public val menu: ImageVector
  get() {
    if (_menu != null) {
      return _menu!!
    }
    _menu =
      ImageVector.Builder(
          name = "menu",
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
            moveTo(4.95f, 37.75f)
            verticalLineToRelative(-4.6f)
            horizontalLineTo(43.1f)
            verticalLineToRelative(4.6f)
            horizontalLineTo(4.95f)
            close()
            moveToRelative(0f, -11.45f)
            verticalLineTo(21.75f)
            horizontalLineTo(43.1f)
            verticalLineTo(26.3f)
            horizontalLineTo(4.95f)
            close()
            moveToRelative(0f, -11.45f)
            verticalLineToRelative(-4.6f)
            horizontalLineTo(43.1f)
            verticalLineToRelative(4.6f)
            horizontalLineTo(4.95f)
            close()
          }
        }
        .build()
    return _menu!!
  }

private var _menu: ImageVector? = null

@Suppress("CheckReturnValue")
public val menu_filled: ImageVector
  get() {
    if (_menu_filled != null) {
      return _menu_filled!!
    }
    _menu_filled =
      ImageVector.Builder(
          name = "menu",
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
            moveTo(4.95f, 37.75f)
            verticalLineToRelative(-4.6f)
            horizontalLineTo(43.1f)
            verticalLineToRelative(4.6f)
            horizontalLineTo(4.95f)
            close()
            moveToRelative(0f, -11.45f)
            verticalLineTo(21.75f)
            horizontalLineTo(43.1f)
            verticalLineTo(26.3f)
            horizontalLineTo(4.95f)
            close()
            moveToRelative(0f, -11.45f)
            verticalLineToRelative(-4.6f)
            horizontalLineTo(43.1f)
            verticalLineToRelative(4.6f)
            horizontalLineTo(4.95f)
            close()
          }
        }
        .build()
    return _menu_filled!!
  }

private var _menu_filled: ImageVector? = null
