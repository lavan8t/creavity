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
public val note_add: ImageVector
  get() {
    if (_note_add != null) {
      return _note_add!!
    }
    _note_add =
      ImageVector.Builder(
          name = "note_add",
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
            moveTo(22.6f, 36.5f)
            horizontalLineToRelative(2.85f)
            verticalLineTo(30.05f)
            horizontalLineToRelative(6.5f)
            verticalLineTo(27.2f)
            horizontalLineToRelative(-6.5f)
            verticalLineTo(20.7f)
            horizontalLineTo(22.6f)
            verticalLineToRelative(6.5f)
            horizontalLineTo(16.1f)
            verticalLineToRelative(2.85f)
            horizontalLineToRelative(6.5f)
            verticalLineTo(36.5f)
            close()
            moveTo(11.45f, 45.05f)
            quadToRelative(-1.79f, 0f, -3.17f, -1.35f)
            reflectiveQuadTo(6.9f, 40.5f)
            verticalLineTo(7.5f)
            quadTo(6.9f, 5.62f, 8.28f, 4.26f)
            reflectiveQuadTo(11.45f, 2.9f)
            horizontalLineToRelative(18.2f)
            lineToRelative(11.5f, 11.4f)
            verticalLineTo(40.5f)
            quadToRelative(0f, 1.86f, -1.4f, 3.2f)
            reflectiveQuadToRelative(-3.2f, 1.35f)
            horizontalLineTo(11.45f)
            close()
            moveTo(27.25f, 16.6f)
            verticalLineTo(7.5f)
            horizontalLineTo(11.45f)
            verticalLineToRelative(33f)
            horizontalLineToRelative(25.1f)
            verticalLineTo(16.6f)
            horizontalLineToRelative(-9.3f)
            close()
            moveTo(11.45f, 7.5f)
            verticalLineToRelative(9.1f)
            verticalLineTo(7.5f)
            verticalLineToRelative(9.1f)
            verticalLineTo(40.5f)
            verticalLineTo(7.5f)
            close()
          }
        }
        .build()
    return _note_add!!
  }

private var _note_add: ImageVector? = null

@Suppress("CheckReturnValue")
public val note_add_filled: ImageVector
  get() {
    if (_note_add_filled != null) {
      return _note_add_filled!!
    }
    _note_add_filled =
      ImageVector.Builder(
          name = "note_add",
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
            moveTo(22.6f, 36.5f)
            horizontalLineToRelative(2.85f)
            verticalLineTo(30.05f)
            horizontalLineToRelative(6.5f)
            verticalLineTo(27.2f)
            horizontalLineToRelative(-6.5f)
            verticalLineTo(20.7f)
            horizontalLineTo(22.6f)
            verticalLineToRelative(6.5f)
            horizontalLineTo(16.1f)
            verticalLineToRelative(2.85f)
            horizontalLineToRelative(6.5f)
            verticalLineTo(36.5f)
            close()
            moveTo(11.45f, 45.05f)
            quadToRelative(-1.79f, 0f, -3.17f, -1.35f)
            reflectiveQuadTo(6.9f, 40.5f)
            verticalLineTo(7.5f)
            quadTo(6.9f, 5.62f, 8.28f, 4.26f)
            reflectiveQuadTo(11.45f, 2.9f)
            horizontalLineToRelative(18.2f)
            lineToRelative(11.5f, 11.4f)
            verticalLineTo(40.5f)
            quadToRelative(0f, 1.86f, -1.4f, 3.2f)
            reflectiveQuadToRelative(-3.2f, 1.35f)
            horizontalLineTo(11.45f)
            close()
            moveTo(27.25f, 16.6f)
            horizontalLineToRelative(9.3f)
            lineTo(27.25f, 7.5f)
            verticalLineToRelative(9.1f)
            close()
          }
        }
        .build()
    return _note_add_filled!!
  }

private var _note_add_filled: ImageVector? = null
