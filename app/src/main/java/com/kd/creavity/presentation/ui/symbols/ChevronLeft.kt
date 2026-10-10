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
public val chevron_left: ImageVector
  get() {
    if (_chevron_left != null) {
      return _chevron_left!!
    }
    _chevron_left =
      ImageVector.Builder(
          name = "chevron_left",
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
            moveTo(28.05f, 37.05f)
            lineTo(14.9f, 23.95f)
            lineTo(28.05f, 10.8f)
            lineToRelative(3.25f, 3.25f)
            lineToRelative(-9.9f, 9.9f)
            lineToRelative(9.9f, 9.85f)
            lineToRelative(-3.25f, 3.25f)
            close()
          }
        }
        .build()
    return _chevron_left!!
  }

private var _chevron_left: ImageVector? = null

@Suppress("CheckReturnValue")
public val chevron_left_filled: ImageVector
  get() {
    if (_chevron_left_filled != null) {
      return _chevron_left_filled!!
    }
    _chevron_left_filled =
      ImageVector.Builder(
          name = "chevron_left",
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
            moveTo(28.05f, 37.05f)
            lineTo(14.9f, 23.95f)
            lineTo(28.05f, 10.8f)
            lineToRelative(3.25f, 3.25f)
            lineToRelative(-9.9f, 9.9f)
            lineToRelative(9.9f, 9.85f)
            lineToRelative(-3.25f, 3.25f)
            close()
          }
        }
        .build()
    return _chevron_left_filled!!
  }

private var _chevron_left_filled: ImageVector? = null
