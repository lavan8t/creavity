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
public val star: ImageVector
  get() {
    if (_star != null) {
      return _star!!
    }
    _star =
      ImageVector.Builder(
          name = "star",
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
            moveTo(16.75f, 34.9f)
            lineTo(24f, 30.54f)
            lineToRelative(7.3f, 4.41f)
            lineToRelative(-2f, -8.2f)
            lineTo(35.7f, 21.2f)
            lineTo(27.3f, 20.5f)
            lineTo(24f, 12.75f)
            lineToRelative(-3.25f, 7.7f)
            lineToRelative(-8.4f, 0.7f)
            lineToRelative(6.42f, 5.5f)
            lineTo(16.75f, 34.9f)
            close()
            moveTo(9.9f, 44.4f)
            lineToRelative(3.75f, -16f)
            lineTo(1.15f, 17.55f)
            lineTo(17.63f, 16.16f)
            lineTo(24f, 0.95f)
            lineToRelative(6.42f, 15.21f)
            lineToRelative(16.43f, 1.39f)
            lineTo(34.35f, 28.4f)
            lineToRelative(3.8f, 16f)
            lineTo(24f, 35.85f)
            lineTo(9.9f, 44.4f)
            close()
            moveTo(24f, 24.3f)
            close()
          }
        }
        .build()
    return _star!!
  }

private var _star: ImageVector? = null

@Suppress("CheckReturnValue")
public val star_filled: ImageVector
  get() {
    if (_star_filled != null) {
      return _star_filled!!
    }
    _star_filled =
      ImageVector.Builder(
          name = "star",
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
            moveTo(9.9f, 44.4f)
            lineToRelative(3.75f, -16f)
            lineTo(1.15f, 17.55f)
            lineToRelative(16.5f, -1.4f)
            lineTo(24f, 0.95f)
            lineToRelative(6.4f, 15.2f)
            lineToRelative(16.45f, 1.4f)
            lineTo(34.35f, 28.4f)
            lineToRelative(3.8f, 16f)
            lineTo(24f, 35.85f)
            lineTo(9.9f, 44.4f)
            close()
          }
        }
        .build()
    return _star_filled!!
  }

private var _star_filled: ImageVector? = null
