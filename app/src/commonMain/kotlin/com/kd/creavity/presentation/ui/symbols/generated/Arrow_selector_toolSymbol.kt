package com.kd.creavity.presentation.ui.symbols.generated

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val arrow_selector_tool: ImageVector
  get() {
    if (_arrow_selector_tool != null) {
      return _arrow_selector_tool!!
    }
    _arrow_selector_tool =
      ImageVector.Builder(
          name = "arrow_selector_tool",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
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
            moveTo(15.98f, 23.18f)
            quadToRelative(-0.73f, 0.35f, -1.48f, 0.06f)
            lineTo(13.4f, 22.23f)
            lineTo(10.2f, 15.4f)
            lineToRelative(-2.3f, 3.23f)
            quadToRelative(-0.68f, 0.95f, -1.76f, 0.6f)
            lineTo(5.05f, 17.73f)
            verticalLineTo(3.23f)
            quadToRelative(0.89f, -1.43f, 1.66f, 0.18f)
            lineToRelative(11.38f, 8.93f)
            quadToRelative(0.51f, 1.76f, -0.09f, 2.45f)
            lineTo(18.0f, 13.73f)
            horizontalLineTo(13.73f)
            lineToRelative(3.2f, 6.85f)
            quadToRelative(0.35f, 0.73f, 0.06f, 1.49f)
            close()
          }
        }
        .build()
    return _arrow_selector_tool!!
  }

private var _arrow_selector_tool: ImageVector? = null
