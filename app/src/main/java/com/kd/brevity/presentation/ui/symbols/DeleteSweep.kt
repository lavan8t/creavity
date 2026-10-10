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
public val delete_sweep: ImageVector
  get() {
    if (_delete_sweep != null) {
      return _delete_sweep!!
    }
    _delete_sweep =
      ImageVector.Builder(
          name = "delete_sweep",
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
            moveTo(30.8f, 38.05f)
            verticalLineToRelative(-3.4f)
            horizontalLineToRelative(7.7f)
            verticalLineToRelative(3.4f)
            horizontalLineTo(30.8f)
            close()
            moveToRelative(0f, -19.5f)
            verticalLineToRelative(-3.4f)
            horizontalLineTo(45.25f)
            verticalLineToRelative(3.4f)
            horizontalLineTo(30.8f)
            close()
            moveToRelative(0f, 9.8f)
            verticalLineTo(24.9f)
            horizontalLineTo(43f)
            verticalLineToRelative(3.45f)
            horizontalLineTo(30.8f)
            close()
            moveTo(5.05f, 14.25f)
            horizontalLineTo(2.8f)
            verticalLineTo(9.65f)
            horizontalLineToRelative(9.05f)
            verticalLineTo(7.45f)
            horizontalLineTo(19.7f)
            verticalLineToRelative(2.2f)
            horizontalLineToRelative(9.05f)
            verticalLineToRelative(4.6f)
            horizontalLineTo(26.5f)
            verticalLineTo(34.5f)
            quadToRelative(0f, 1.86f, -1.36f, 3.2f)
            reflectiveQuadTo(21.9f, 39.05f)
            horizontalLineTo(9.6f)
            quadToRelative(-1.86f, 0f, -3.2f, -1.35f)
            reflectiveQuadTo(5.05f, 34.5f)
            verticalLineTo(14.25f)
            close()
            moveToRelative(4.55f, 0f)
            verticalLineTo(34.5f)
            horizontalLineTo(21.9f)
            verticalLineTo(14.25f)
            horizontalLineTo(9.6f)
            close()
            moveToRelative(0f, 0f)
            verticalLineTo(34.5f)
            verticalLineTo(14.25f)
            close()
          }
        }
        .build()
    return _delete_sweep!!
  }

private var _delete_sweep: ImageVector? = null

@Suppress("CheckReturnValue")
public val delete_sweep_filled: ImageVector
  get() {
    if (_delete_sweep_filled != null) {
      return _delete_sweep_filled!!
    }
    _delete_sweep_filled =
      ImageVector.Builder(
          name = "delete_sweep",
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
            moveTo(30.8f, 38.05f)
            verticalLineToRelative(-3.4f)
            horizontalLineToRelative(7.7f)
            verticalLineToRelative(3.4f)
            horizontalLineTo(30.8f)
            close()
            moveToRelative(0f, -19.5f)
            verticalLineToRelative(-3.4f)
            horizontalLineTo(45.25f)
            verticalLineToRelative(3.4f)
            horizontalLineTo(30.8f)
            close()
            moveToRelative(0f, 9.8f)
            verticalLineTo(24.9f)
            horizontalLineTo(43f)
            verticalLineToRelative(3.45f)
            horizontalLineTo(30.8f)
            close()
            moveTo(5.05f, 14.25f)
            horizontalLineTo(2.8f)
            verticalLineTo(9.65f)
            horizontalLineToRelative(9.05f)
            verticalLineTo(7.45f)
            horizontalLineTo(19.7f)
            verticalLineToRelative(2.2f)
            horizontalLineToRelative(9.05f)
            verticalLineToRelative(4.6f)
            horizontalLineTo(26.5f)
            verticalLineTo(34.5f)
            quadToRelative(0f, 1.86f, -1.36f, 3.2f)
            reflectiveQuadTo(21.9f, 39.05f)
            horizontalLineTo(9.6f)
            quadToRelative(-1.86f, 0f, -3.2f, -1.35f)
            reflectiveQuadTo(5.05f, 34.5f)
            verticalLineTo(14.25f)
            close()
          }
        }
        .build()
    return _delete_sweep_filled!!
  }

private var _delete_sweep_filled: ImageVector? = null
