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
public val home: ImageVector
  get() {
    if (_home != null) {
      return _home!!
    }
    _home =
      ImageVector.Builder(
          name = "home",
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
            moveTo(11.45f, 38.55f)
            horizontalLineTo(17.8f)
            verticalLineTo(25.7f)
            horizontalLineTo(30.25f)
            verticalLineTo(38.55f)
            horizontalLineToRelative(6.3f)
            verticalLineTo(19.7f)
            lineTo(24f, 10.3f)
            lineTo(11.45f, 19.68f)
            verticalLineTo(38.55f)
            close()
            moveTo(6.9f, 43.1f)
            verticalLineTo(17.4f)
            lineTo(24f, 4.55f)
            lineTo(41.15f, 17.4f)
            verticalLineTo(43.1f)
            horizontalLineTo(26.25f)
            verticalLineTo(29.7f)
            horizontalLineTo(21.8f)
            verticalLineTo(43.1f)
            horizontalLineTo(6.9f)
            close()
            moveTo(24f, 24.4f)
            close()
          }
        }
        .build()
    return _home!!
  }

private var _home: ImageVector? = null

@Suppress("CheckReturnValue")
public val home_filled: ImageVector
  get() {
    if (_home_filled != null) {
      return _home_filled!!
    }
    _home_filled =
      ImageVector.Builder(
          name = "home",
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
            moveTo(6.9f, 43.1f)
            verticalLineTo(17.42f)
            lineTo(24f, 4.55f)
            lineTo(41.15f, 17.4f)
            verticalLineTo(43.1f)
            horizontalLineTo(28.25f)
            verticalLineTo(27.7f)
            horizontalLineToRelative(-8.5f)
            verticalLineTo(43.1f)
            horizontalLineTo(6.9f)
            close()
          }
        }
        .build()
    return _home_filled!!
  }

private var _home_filled: ImageVector? = null
