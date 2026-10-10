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
public val tune: ImageVector
  get() {
    if (_tune != null) {
      return _tune!!
    }
    _tune =
      ImageVector.Builder(
          name = "tune",
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
            moveTo(21.05f, 43f)
            verticalLineTo(30.75f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(4.2f)
            horizontalLineToRelative(17.7f)
            verticalLineToRelative(4f)
            horizontalLineTo(25.05f)
            verticalLineTo(43f)
            horizontalLineToRelative(-4f)
            close()
            moveTo(5.25f, 38.95f)
            verticalLineToRelative(-4f)
            horizontalLineToRelative(13.3f)
            verticalLineToRelative(4f)
            horizontalLineTo(5.25f)
            close()
            moveToRelative(9.3f, -8.9f)
            verticalLineToRelative(-4.1f)
            horizontalLineTo(5.25f)
            verticalLineTo(22f)
            horizontalLineToRelative(9.3f)
            verticalLineTo(17.85f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(12.2f)
            horizontalLineToRelative(-4f)
            close()
            moveToRelative(6.5f, -4.1f)
            verticalLineTo(22f)
            horizontalLineToRelative(21.7f)
            verticalLineToRelative(3.95f)
            horizontalLineTo(21.05f)
            close()
            moveToRelative(8.4f, -8.75f)
            verticalLineTo(4.95f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(4.1f)
            horizontalLineToRelative(9.3f)
            verticalLineToRelative(4f)
            horizontalLineToRelative(-9.3f)
            verticalLineTo(17.2f)
            horizontalLineToRelative(-4f)
            close()
            moveTo(5.25f, 13.05f)
            verticalLineToRelative(-4f)
            horizontalLineToRelative(21.7f)
            verticalLineToRelative(4f)
            horizontalLineTo(5.25f)
            close()
          }
        }
        .build()
    return _tune!!
  }

private var _tune: ImageVector? = null

@Suppress("CheckReturnValue")
public val tune_filled: ImageVector
  get() {
    if (_tune_filled != null) {
      return _tune_filled!!
    }
    _tune_filled =
      ImageVector.Builder(
          name = "tune",
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
            moveTo(21.05f, 43f)
            verticalLineTo(30.75f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(4.2f)
            horizontalLineToRelative(17.7f)
            verticalLineToRelative(4f)
            horizontalLineTo(25.05f)
            verticalLineTo(43f)
            horizontalLineToRelative(-4f)
            close()
            moveTo(5.25f, 38.95f)
            verticalLineToRelative(-4f)
            horizontalLineToRelative(13.3f)
            verticalLineToRelative(4f)
            horizontalLineTo(5.25f)
            close()
            moveToRelative(9.3f, -8.9f)
            verticalLineToRelative(-4.1f)
            horizontalLineTo(5.25f)
            verticalLineTo(22f)
            horizontalLineToRelative(9.3f)
            verticalLineTo(17.85f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(12.2f)
            horizontalLineToRelative(-4f)
            close()
            moveToRelative(6.5f, -4.1f)
            verticalLineTo(22f)
            horizontalLineToRelative(21.7f)
            verticalLineToRelative(3.95f)
            horizontalLineTo(21.05f)
            close()
            moveToRelative(8.4f, -8.75f)
            verticalLineTo(4.95f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(4.1f)
            horizontalLineToRelative(9.3f)
            verticalLineToRelative(4f)
            horizontalLineToRelative(-9.3f)
            verticalLineTo(17.2f)
            horizontalLineToRelative(-4f)
            close()
            moveTo(5.25f, 13.05f)
            verticalLineToRelative(-4f)
            horizontalLineToRelative(21.7f)
            verticalLineToRelative(4f)
            horizontalLineTo(5.25f)
            close()
          }
        }
        .build()
    return _tune_filled!!
  }

private var _tune_filled: ImageVector? = null
