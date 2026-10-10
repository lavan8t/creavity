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
public val folder: ImageVector
  get() {
    if (_folder != null) {
      return _folder!!
    }
    _folder =
      ImageVector.Builder(
          name = "folder",
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
            moveTo(7.5f, 41.05f)
            quadToRelative(-1.86f, 0f, -3.2f, -1.35f)
            reflectiveQuadTo(2.95f, 36.5f)
            verticalLineToRelative(-25f)
            quadTo(2.95f, 9.64f, 4.3f, 8.27f)
            reflectiveQuadTo(7.5f, 6.9f)
            horizontalLineTo(20.65f)
            lineToRelative(3.4f, 3.45f)
            horizontalLineTo(40.5f)
            quadToRelative(1.86f, 0f, 3.23f, 1.35f)
            reflectiveQuadToRelative(1.37f, 3.2f)
            verticalLineTo(36.5f)
            quadToRelative(0f, 1.84f, -1.37f, 3.2f)
            reflectiveQuadTo(40.5f, 41.05f)
            horizontalLineTo(7.5f)
            close()
            moveToRelative(0f, -4.55f)
            horizontalLineToRelative(33f)
            verticalLineTo(14.9f)
            horizontalLineTo(22.2f)
            lineTo(18.8f, 11.5f)
            horizontalLineTo(7.5f)
            verticalLineToRelative(25f)
            close()
            moveToRelative(0f, 0f)
            verticalLineToRelative(-25f)
            verticalLineToRelative(3.4f)
            verticalLineTo(36.5f)
            close()
          }
        }
        .build()
    return _folder!!
  }

private var _folder: ImageVector? = null

@Suppress("CheckReturnValue")
public val folder_filled: ImageVector
  get() {
    if (_folder_filled != null) {
      return _folder_filled!!
    }
    _folder_filled =
      ImageVector.Builder(
          name = "folder",
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
            moveTo(7.5f, 41.05f)
            quadToRelative(-1.86f, 0f, -3.2f, -1.35f)
            reflectiveQuadTo(2.95f, 36.5f)
            verticalLineToRelative(-25f)
            quadTo(2.95f, 9.64f, 4.3f, 8.27f)
            reflectiveQuadTo(7.5f, 6.9f)
            horizontalLineTo(20.65f)
            lineToRelative(3.4f, 3.45f)
            horizontalLineTo(40.5f)
            quadToRelative(1.86f, 0f, 3.23f, 1.35f)
            reflectiveQuadToRelative(1.37f, 3.2f)
            verticalLineTo(36.5f)
            quadToRelative(0f, 1.84f, -1.37f, 3.2f)
            reflectiveQuadTo(40.5f, 41.05f)
            horizontalLineTo(7.5f)
            close()
          }
        }
        .build()
    return _folder_filled!!
  }

private var _folder_filled: ImageVector? = null
