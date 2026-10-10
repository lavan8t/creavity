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
public val chevron_right: ImageVector
  get() {
    if (_chevron_right != null) {
      return _chevron_right!!
    }
    _chevron_right =
      ImageVector.Builder(
          name = "chevron_right",
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
            moveTo(25.4f, 23.95f)
            lineToRelative(-9.9f, -9.9f)
            lineTo(18.75f, 10.8f)
            lineTo(31.9f, 23.95f)
            lineTo(18.75f, 37.05f)
            lineTo(15.5f, 33.8f)
            lineToRelative(9.9f, -9.85f)
            close()
          }
        }
        .build()
    return _chevron_right!!
  }

private var _chevron_right: ImageVector? = null

@Suppress("CheckReturnValue")
public val chevron_right_filled: ImageVector
  get() {
    if (_chevron_right_filled != null) {
      return _chevron_right_filled!!
    }
    _chevron_right_filled =
      ImageVector.Builder(
          name = "chevron_right",
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
            moveTo(25.4f, 23.95f)
            lineToRelative(-9.9f, -9.9f)
            lineTo(18.75f, 10.8f)
            lineTo(31.9f, 23.95f)
            lineTo(18.75f, 37.05f)
            lineTo(15.5f, 33.8f)
            lineToRelative(9.9f, -9.85f)
            close()
          }
        }
        .build()
    return _chevron_right_filled!!
  }

private var _chevron_right_filled: ImageVector? = null
