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
public val sticky_note_2: ImageVector
  get() {
    if (_sticky_note_2 != null) {
      return _sticky_note_2!!
    }
    _sticky_note_2 =
      ImageVector.Builder(
          name = "sticky_note_2",
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
            moveTo(9.5f, 38.5f)
            horizontalLineToRelative(19f)
            verticalLineToRelative(-10f)
            horizontalLineToRelative(10f)
            verticalLineTo(9.5f)
            horizontalLineTo(9.5f)
            verticalLineToRelative(29f)
            close()
            moveToRelative(0f, 4.55f)
            quadToRelative(-1.91f, 0f, -3.23f, -1.32f)
            reflectiveQuadTo(4.95f, 38.5f)
            verticalLineTo(9.5f)
            quadToRelative(0f, -1.93f, 1.32f, -3.26f)
            reflectiveQuadTo(9.5f, 4.9f)
            horizontalLineToRelative(29f)
            quadToRelative(1.93f, 0f, 3.26f, 1.34f)
            reflectiveQuadTo(43.1f, 9.5f)
            verticalLineTo(30.05f)
            lineToRelative(-13.05f, 13f)
            horizontalLineTo(9.5f)
            close()
            moveToRelative(6f, -15.85f)
            verticalLineTo(23.8f)
            horizontalLineToRelative(8.05f)
            verticalLineToRelative(3.4f)
            horizontalLineTo(15.5f)
            close()
            moveToRelative(0f, -8.4f)
            verticalLineTo(15.35f)
            horizontalLineToRelative(17f)
            verticalLineTo(18.8f)
            horizontalLineToRelative(-17f)
            close()
            moveToRelative(-6f, 19.7f)
            verticalLineToRelative(-10f)
            verticalLineTo(9.5f)
            verticalLineToRelative(29f)
            close()
          }
        }
        .build()
    return _sticky_note_2!!
  }

private var _sticky_note_2: ImageVector? = null

@Suppress("CheckReturnValue")
public val sticky_note_2_filled: ImageVector
  get() {
    if (_sticky_note_2_filled != null) {
      return _sticky_note_2_filled!!
    }
    _sticky_note_2_filled =
      ImageVector.Builder(
          name = "sticky_note_2",
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
            moveTo(9.5f, 43.05f)
            quadToRelative(-1.91f, 0f, -3.23f, -1.32f)
            reflectiveQuadTo(4.95f, 38.5f)
            verticalLineTo(9.5f)
            quadToRelative(0f, -1.93f, 1.32f, -3.26f)
            reflectiveQuadTo(9.5f, 4.9f)
            horizontalLineToRelative(29f)
            quadToRelative(1.93f, 0f, 3.26f, 1.34f)
            reflectiveQuadTo(43.1f, 9.5f)
            verticalLineTo(30.05f)
            lineToRelative(-13.05f, 13f)
            horizontalLineTo(9.5f)
            close()
            moveToRelative(19f, -4.55f)
            lineToRelative(10f, -10f)
            horizontalLineToRelative(-10f)
            verticalLineToRelative(10f)
            close()
            moveTo(15.5f, 27.2f)
            horizontalLineToRelative(8.05f)
            verticalLineTo(23.8f)
            horizontalLineTo(15.5f)
            verticalLineToRelative(3.4f)
            close()
            moveToRelative(0f, -8.4f)
            horizontalLineToRelative(17f)
            verticalLineTo(15.35f)
            horizontalLineToRelative(-17f)
            verticalLineTo(18.8f)
            close()
          }
        }
        .build()
    return _sticky_note_2_filled!!
  }

private var _sticky_note_2_filled: ImageVector? = null
