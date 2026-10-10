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
public val view_list: ImageVector
  get() {
    if (_view_list != null) {
      return _view_list!!
    }
    _view_list =
      ImageVector.Builder(
          name = "view_list",
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
            moveTo(18.65f, 36.55f)
            horizontalLineToRelative(21.9f)
            verticalLineToRelative(-6f)
            horizontalLineTo(18.65f)
            verticalLineToRelative(6f)
            close()
            moveTo(7.45f, 17.4f)
            horizontalLineTo(14.8f)
            verticalLineTo(11.45f)
            horizontalLineTo(7.45f)
            verticalLineTo(17.4f)
            close()
            moveToRelative(0f, 9.35f)
            horizontalLineTo(14.8f)
            verticalLineToRelative(-5.5f)
            horizontalLineTo(7.45f)
            verticalLineToRelative(5.5f)
            close()
            moveToRelative(0f, 9.8f)
            horizontalLineTo(14.8f)
            verticalLineToRelative(-6f)
            horizontalLineTo(7.45f)
            verticalLineToRelative(6f)
            close()
            moveToRelative(11.2f, -9.8f)
            horizontalLineToRelative(21.9f)
            verticalLineToRelative(-5.5f)
            horizontalLineTo(18.65f)
            verticalLineToRelative(5.5f)
            close()
            moveToRelative(0f, -9.35f)
            horizontalLineToRelative(21.9f)
            verticalLineTo(11.45f)
            horizontalLineTo(18.65f)
            verticalLineTo(17.4f)
            close()
            moveTo(7.45f, 41.1f)
            quadToRelative(-1.86f, 0f, -3.2f, -1.35f)
            reflectiveQuadTo(2.9f, 36.55f)
            verticalLineTo(11.45f)
            quadTo(2.9f, 9.57f, 4.25f, 8.21f)
            reflectiveQuadTo(7.45f, 6.85f)
            horizontalLineToRelative(33.1f)
            quadToRelative(1.88f, 0f, 3.24f, 1.36f)
            reflectiveQuadToRelative(1.36f, 3.24f)
            verticalLineToRelative(25.1f)
            quadToRelative(0f, 1.86f, -1.36f, 3.2f)
            reflectiveQuadTo(40.55f, 41.1f)
            horizontalLineTo(7.45f)
            close()
          }
        }
        .build()
    return _view_list!!
  }

private var _view_list: ImageVector? = null

@Suppress("CheckReturnValue")
public val view_list_filled: ImageVector
  get() {
    if (_view_list_filled != null) {
      return _view_list_filled!!
    }
    _view_list_filled =
      ImageVector.Builder(
          name = "view_list",
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
            moveTo(18.15f, 41.1f)
            horizontalLineToRelative(22.4f)
            quadToRelative(1.91f, 0f, 3.26f, -1.33f)
            reflectiveQuadToRelative(1.34f, -3.22f)
            verticalLineToRelative(-4.9f)
            horizontalLineToRelative(-27f)
            verticalLineTo(41.1f)
            close()
            moveTo(2.9f, 16.35f)
            horizontalLineTo(15.3f)
            verticalLineTo(6.85f)
            horizontalLineTo(7.45f)
            quadToRelative(-1.89f, 0f, -3.22f, 1.34f)
            reflectiveQuadTo(2.9f, 11.45f)
            verticalLineToRelative(4.9f)
            close()
            moveToRelative(0f, 12.35f)
            horizontalLineTo(15.3f)
            verticalLineTo(19.35f)
            horizontalLineTo(2.9f)
            verticalLineTo(28.7f)
            close()
            moveTo(7.45f, 41.1f)
            horizontalLineTo(15.3f)
            verticalLineTo(31.65f)
            horizontalLineTo(2.9f)
            verticalLineToRelative(4.9f)
            quadToRelative(0f, 1.89f, 1.33f, 3.22f)
            reflectiveQuadTo(7.45f, 41.1f)
            close()
            moveTo(18.15f, 28.7f)
            horizontalLineToRelative(27f)
            verticalLineTo(19.35f)
            horizontalLineToRelative(-27f)
            verticalLineTo(28.7f)
            close()
            moveToRelative(0f, -12.35f)
            horizontalLineToRelative(27f)
            verticalLineToRelative(-4.9f)
            quadToRelative(0f, -1.91f, -1.34f, -3.26f)
            reflectiveQuadTo(40.55f, 6.85f)
            horizontalLineTo(18.15f)
            verticalLineToRelative(9.5f)
            close()
          }
        }
        .build()
    return _view_list_filled!!
  }

private var _view_list_filled: ImageVector? = null
