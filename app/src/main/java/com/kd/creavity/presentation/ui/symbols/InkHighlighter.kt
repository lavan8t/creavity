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
public val ink_highlighter: ImageVector
  get() {
    if (_ink_highlighter != null) {
      return _ink_highlighter!!
    }
    _ink_highlighter =
      ImageVector.Builder(
          name = "ink_highlighter",
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
            moveTo(26.1f, 28.7f)
            lineTo(20.6f, 23.15f)
            lineToRelative(-9.65f, 9.6f)
            lineToRelative(5.6f, 5.6f)
            lineTo(26.1f, 28.7f)
            close()
            moveTo(23.8f, 19.85f)
            lineToRelative(5.6f, 5.55f)
            lineTo(40.3f, 14.6f)
            lineTo(34.75f, 9f)
            lineTo(23.8f, 19.85f)
            close()
            moveToRelative(-4.85f, -1.6f)
            lineTo(31f, 30.35f)
            lineTo(20.3f, 41.05f)
            quadToRelative(-1.4f, 1.4f, -3.85f, 1.4f)
            reflectiveQuadTo(12.7f, 41.05f)
            lineTo(12.2f, 40.6f)
            lineTo(9.6f, 43.15f)
            horizontalLineTo(2.2f)
            lineTo(8.45f, 36.9f)
            lineTo(8.3f, 36.65f)
            quadTo(6.7f, 35.15f, 6.7f, 32.8f)
            reflectiveQuadTo(8.35f, 28.85f)
            lineToRelative(10.6f, -10.6f)
            close()
            moveToRelative(0f, 0f)
            lineTo(31.6f, 5.6f)
            quadTo(33f, 4.15f, 34.85f, 4.15f)
            reflectiveQuadTo(38.2f, 5.6f)
            lineToRelative(5.35f, 5.45f)
            quadToRelative(1.4f, 1.4f, 1.38f, 3.4f)
            reflectiveQuadTo(43.5f, 17.9f)
            lineTo(31f, 30.35f)
            lineTo(18.95f, 18.25f)
            close()
          }
        }
        .build()
    return _ink_highlighter!!
  }

private var _ink_highlighter: ImageVector? = null

@Suppress("CheckReturnValue")
public val ink_highlighter_filled: ImageVector
  get() {
    if (_ink_highlighter_filled != null) {
      return _ink_highlighter_filled!!
    }
    _ink_highlighter_filled =
      ImageVector.Builder(
          name = "ink_highlighter",
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
            moveTo(18.9f, 18.3f)
            lineToRelative(12f, 12.1f)
            lineTo(20.3f, 41.05f)
            quadToRelative(-1.4f, 1.4f, -3.85f, 1.4f)
            reflectiveQuadTo(12.7f, 41.05f)
            lineTo(12.2f, 40.6f)
            lineTo(9.6f, 43.15f)
            horizontalLineTo(2.2f)
            lineTo(8.45f, 36.9f)
            lineTo(8.35f, 36.7f)
            quadTo(6.75f, 35.2f, 6.73f, 32.83f)
            reflectiveQuadTo(8.35f, 28.85f)
            lineTo(18.9f, 18.3f)
            close()
            moveToRelative(2.15f, -2.2f)
            lineTo(31.6f, 5.6f)
            quadTo(33f, 4.15f, 34.85f, 4.15f)
            reflectiveQuadTo(38.2f, 5.6f)
            lineToRelative(5.35f, 5.45f)
            quadToRelative(1.4f, 1.4f, 1.38f, 3.4f)
            reflectiveQuadTo(43.5f, 17.9f)
            lineTo(33.1f, 28.2f)
            lineTo(21.05f, 16.1f)
            close()
          }
        }
        .build()
    return _ink_highlighter_filled!!
  }

private var _ink_highlighter_filled: ImageVector? = null
