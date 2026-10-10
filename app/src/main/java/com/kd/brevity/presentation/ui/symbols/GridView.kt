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
public val grid_view: ImageVector
  get() {
    if (_grid_view != null) {
      return _grid_view!!
    }
    _grid_view =
      ImageVector.Builder(
          name = "grid_view",
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
            moveTo(4.95f, 22.5f)
            verticalLineTo(4.9f)
            horizontalLineTo(22.5f)
            verticalLineTo(22.5f)
            horizontalLineTo(4.95f)
            close()
            moveToRelative(0f, 20.55f)
            verticalLineTo(25.5f)
            horizontalLineTo(22.5f)
            verticalLineTo(43.05f)
            horizontalLineTo(4.95f)
            close()
            moveTo(25.5f, 22.5f)
            verticalLineTo(4.9f)
            horizontalLineTo(43.1f)
            verticalLineTo(22.5f)
            horizontalLineTo(25.5f)
            close()
            moveToRelative(0f, 20.55f)
            verticalLineTo(25.5f)
            horizontalLineTo(43.1f)
            verticalLineTo(43.05f)
            horizontalLineTo(25.5f)
            close()
            moveTo(9.5f, 17.9f)
            horizontalLineToRelative(8.45f)
            verticalLineTo(9.5f)
            horizontalLineTo(9.5f)
            verticalLineToRelative(8.4f)
            close()
            moveToRelative(20.6f, 0f)
            horizontalLineToRelative(8.4f)
            verticalLineTo(9.5f)
            horizontalLineTo(30.1f)
            verticalLineToRelative(8.4f)
            close()
            moveToRelative(0f, 20.6f)
            horizontalLineToRelative(8.4f)
            verticalLineTo(30.05f)
            horizontalLineTo(30.1f)
            verticalLineTo(38.5f)
            close()
            moveToRelative(-20.6f, 0f)
            horizontalLineToRelative(8.45f)
            verticalLineTo(30.05f)
            horizontalLineTo(9.5f)
            verticalLineTo(38.5f)
            close()
            moveTo(30.1f, 17.9f)
            close()
            moveToRelative(0f, 12.15f)
            close()
            moveToRelative(-12.15f, 0f)
            close()
            moveToRelative(0f, -12.15f)
            close()
          }
        }
        .build()
    return _grid_view!!
  }

private var _grid_view: ImageVector? = null

@Suppress("CheckReturnValue")
public val grid_view_filled: ImageVector
  get() {
    if (_grid_view_filled != null) {
      return _grid_view_filled!!
    }
    _grid_view_filled =
      ImageVector.Builder(
          name = "grid_view",
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
            moveTo(4.95f, 22.5f)
            verticalLineTo(4.9f)
            horizontalLineTo(22.5f)
            verticalLineTo(22.5f)
            horizontalLineTo(4.95f)
            close()
            moveToRelative(0f, 20.55f)
            verticalLineTo(25.5f)
            horizontalLineTo(22.5f)
            verticalLineTo(43.05f)
            horizontalLineTo(4.95f)
            close()
            moveTo(25.5f, 22.5f)
            verticalLineTo(4.9f)
            horizontalLineTo(43.1f)
            verticalLineTo(22.5f)
            horizontalLineTo(25.5f)
            close()
            moveToRelative(0f, 20.55f)
            verticalLineTo(25.5f)
            horizontalLineTo(43.1f)
            verticalLineTo(43.05f)
            horizontalLineTo(25.5f)
            close()
          }
        }
        .build()
    return _grid_view_filled!!
  }

private var _grid_view_filled: ImageVector? = null
