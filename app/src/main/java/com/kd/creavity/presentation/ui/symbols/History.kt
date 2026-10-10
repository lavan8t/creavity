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
public val history: ImageVector
  get() {
    if (_history != null) {
      return _history!!
    }
    _history =
      ImageVector.Builder(
          name = "history",
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
            moveTo(23.75f, 43.05f)
            quadToRelative(-8.03f, 0f, -13.53f, -5.6f)
            reflectiveQuadTo(4.85f, 23.95f)
            horizontalLineTo(9.4f)
            quadToRelative(0.06f, 6.06f, 4.2f, 10.31f)
            reflectiveQuadTo(23.75f, 38.5f)
            quadToRelative(6.07f, 0f, 10.38f, -4.3f)
            reflectiveQuadTo(38.45f, 23.78f)
            quadToRelative(0f, -6.03f, -4.35f, -10.16f)
            reflectiveQuadTo(23.75f, 9.5f)
            quadToRelative(-2.98f, 0f, -5.64f, 1.2f)
            reflectiveQuadToRelative(-4.66f, 3.25f)
            horizontalLineTo(17.6f)
            verticalLineToRelative(3.5f)
            horizontalLineTo(6.45f)
            verticalLineTo(6.4f)
            horizontalLineTo(9.8f)
            verticalLineToRelative(4.7f)
            quadTo(12.5f, 8.25f, 16.11f, 6.57f)
            reflectiveQuadTo(23.75f, 4.9f)
            quadToRelative(3.96f, 0f, 7.45f, 1.52f)
            reflectiveQuadToRelative(6.09f, 4.07f)
            reflectiveQuadToRelative(4.13f, 5.99f)
            reflectiveQuadToRelative(1.53f, 7.43f)
            reflectiveQuadToRelative(-1.53f, 7.47f)
            reflectiveQuadToRelative(-4.13f, 6.08f)
            reflectiveQuadToRelative(-6.09f, 4.1f)
            reflectiveQuadToRelative(-7.44f, 1.5f)
            close()
            moveTo(30.1f, 32.35f)
            lineTo(22.45f, 24.82f)
            verticalLineTo(13.95f)
            horizontalLineToRelative(3.4f)
            verticalLineToRelative(9.38f)
            lineToRelative(6.7f, 6.52f)
            lineToRelative(-2.45f, 2.5f)
            close()
          }
        }
        .build()
    return _history!!
  }

private var _history: ImageVector? = null

@Suppress("CheckReturnValue")
public val history_filled: ImageVector
  get() {
    if (_history_filled != null) {
      return _history_filled!!
    }
    _history_filled =
      ImageVector.Builder(
          name = "history",
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
            moveTo(23.75f, 43.05f)
            quadToRelative(-8.03f, 0f, -13.53f, -5.6f)
            reflectiveQuadTo(4.85f, 23.95f)
            horizontalLineTo(9.4f)
            quadToRelative(0.06f, 6.06f, 4.2f, 10.31f)
            reflectiveQuadTo(23.75f, 38.5f)
            quadToRelative(6.07f, 0f, 10.38f, -4.3f)
            reflectiveQuadTo(38.45f, 23.78f)
            quadToRelative(0f, -6.03f, -4.35f, -10.16f)
            reflectiveQuadTo(23.75f, 9.5f)
            quadToRelative(-2.98f, 0f, -5.64f, 1.2f)
            reflectiveQuadToRelative(-4.66f, 3.25f)
            horizontalLineTo(17.6f)
            verticalLineToRelative(3.5f)
            horizontalLineTo(6.45f)
            verticalLineTo(6.4f)
            horizontalLineTo(9.8f)
            verticalLineToRelative(4.7f)
            quadTo(12.5f, 8.25f, 16.11f, 6.57f)
            reflectiveQuadTo(23.75f, 4.9f)
            quadToRelative(3.96f, 0f, 7.45f, 1.52f)
            reflectiveQuadToRelative(6.09f, 4.07f)
            reflectiveQuadToRelative(4.13f, 5.99f)
            reflectiveQuadToRelative(1.53f, 7.43f)
            reflectiveQuadToRelative(-1.53f, 7.47f)
            reflectiveQuadToRelative(-4.13f, 6.08f)
            reflectiveQuadToRelative(-6.09f, 4.1f)
            reflectiveQuadToRelative(-7.44f, 1.5f)
            close()
            moveTo(30.1f, 32.35f)
            lineTo(22.45f, 24.82f)
            verticalLineTo(13.95f)
            horizontalLineToRelative(3.4f)
            verticalLineToRelative(9.38f)
            lineToRelative(6.7f, 6.52f)
            lineToRelative(-2.45f, 2.5f)
            close()
          }
        }
        .build()
    return _history_filled!!
  }

private var _history_filled: ImageVector? = null
