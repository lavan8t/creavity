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
public val edit: ImageVector
  get() {
    if (_edit != null) {
      return _edit!!
    }
    _edit =
      ImageVector.Builder(
          name = "edit",
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
            moveTo(9.45f, 38.5f)
            horizontalLineTo(11.7f)
            lineTo(32.85f, 17.42f)
            lineTo(30.7f, 15.15f)
            lineTo(9.45f, 36.33f)
            verticalLineTo(38.5f)
            close()
            moveToRelative(-4.5f, 4.55f)
            verticalLineToRelative(-8.6f)
            lineTo(33.4f, 6f)
            quadTo(33.85f, 5.55f, 34.52f, 5.25f)
            reflectiveQuadToRelative(1.41f, -0.3f)
            quadToRelative(0.71f, 0f, 1.37f, 0.3f)
            reflectiveQuadToRelative(1.26f, 0.7f)
            lineTo(42.1f, 9.4f)
            quadToRelative(0.5f, 0.6f, 0.75f, 1.27f)
            reflectiveQuadToRelative(0.25f, 1.37f)
            quadToRelative(0f, 0.75f, -0.27f, 1.43f)
            reflectiveQuadToRelative(-0.71f, 1.11f)
            lineTo(13.6f, 43.05f)
            horizontalLineTo(4.95f)
            close()
            moveTo(38.2f, 12f)
            lineTo(36.1f, 9.85f)
            lineTo(38.2f, 12f)
            close()
            moveToRelative(-6.4f, 4.25f)
            lineToRelative(-1.1f, -1.1f)
            lineToRelative(2.15f, 2.25f)
            lineTo(31.8f, 16.25f)
            close()
          }
        }
        .build()
    return _edit!!
  }

private var _edit: ImageVector? = null

@Suppress("CheckReturnValue")
public val edit_filled: ImageVector
  get() {
    if (_edit_filled != null) {
      return _edit_filled!!
    }
    _edit_filled =
      ImageVector.Builder(
          name = "edit",
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
            moveTo(4.95f, 43.05f)
            verticalLineToRelative(-8.6f)
            lineTo(33.4f, 6f)
            quadTo(33.85f, 5.55f, 34.52f, 5.25f)
            reflectiveQuadToRelative(1.43f, -0.3f)
            quadToRelative(0.69f, 0f, 1.34f, 0.3f)
            reflectiveQuadToRelative(1.26f, 0.7f)
            lineTo(42.1f, 9.4f)
            quadToRelative(0.5f, 0.6f, 0.75f, 1.27f)
            reflectiveQuadToRelative(0.25f, 1.37f)
            quadToRelative(0f, 0.75f, -0.27f, 1.43f)
            reflectiveQuadTo(42.1f, 14.6f)
            lineTo(13.6f, 43.05f)
            horizontalLineTo(4.95f)
            close()
            moveTo(36.2f, 13.95f)
            lineTo(38.2f, 12f)
            lineTo(36.1f, 9.85f)
            lineToRelative(-2.05f, 2f)
            lineToRelative(2.15f, 2.1f)
            close()
          }
        }
        .build()
    return _edit_filled!!
  }

private var _edit_filled: ImageVector? = null
