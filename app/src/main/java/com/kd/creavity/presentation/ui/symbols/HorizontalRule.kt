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
public val horizontal_rule: ImageVector
  get() {
    if (_horizontal_rule != null) {
      return _horizontal_rule!!
    }
    _horizontal_rule =
      ImageVector.Builder(
          name = "horizontal_rule",
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
            moveTo(6.95f, 26.25f)
            verticalLineTo(21.7f)
            horizontalLineTo(41.1f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(6.95f)
            close()
          }
        }
        .build()
    return _horizontal_rule!!
  }

private var _horizontal_rule: ImageVector? = null

@Suppress("CheckReturnValue")
public val horizontal_rule_filled: ImageVector
  get() {
    if (_horizontal_rule_filled != null) {
      return _horizontal_rule_filled!!
    }
    _horizontal_rule_filled =
      ImageVector.Builder(
          name = "horizontal_rule",
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
            moveTo(6.95f, 26.25f)
            verticalLineTo(21.7f)
            horizontalLineTo(41.1f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(6.95f)
            close()
          }
        }
        .build()
    return _horizontal_rule_filled!!
  }

private var _horizontal_rule_filled: ImageVector? = null
