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
public val flare: ImageVector
  get() {
    if (_flare != null) {
      return _flare!!
    }
    _flare =
      ImageVector.Builder(
          name = "flare",
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
            moveTo(1.05f, 26f)
            verticalLineTo(22.05f)
            horizontalLineToRelative(12.6f)
            verticalLineTo(26f)
            horizontalLineTo(1.05f)
            close()
            moveToRelative(14.2f, -7.95f)
            lineTo(10.5f, 13.3f)
            lineToRelative(2.8f, -2.8f)
            lineToRelative(4.75f, 4.75f)
            lineToRelative(-2.8f, 2.8f)
            close()
            moveTo(22f, 13.65f)
            verticalLineTo(1.05f)
            horizontalLineToRelative(3.95f)
            verticalLineToRelative(12.6f)
            horizontalLineTo(22f)
            close()
            moveToRelative(10.75f, 4.4f)
            lineToRelative(-2.8f, -2.8f)
            lineTo(34.7f, 10.5f)
            lineToRelative(2.8f, 2.8f)
            lineToRelative(-4.75f, 4.75f)
            close()
            moveTo(34.35f, 26f)
            verticalLineTo(22.05f)
            horizontalLineToRelative(12.6f)
            verticalLineTo(26f)
            horizontalLineTo(34.35f)
            close()
            moveTo(20.08f, 27.92f)
            quadTo(18.5f, 26.35f, 18.5f, 24f)
            reflectiveQuadToRelative(1.58f, -3.93f)
            reflectiveQuadTo(24f, 18.5f)
            reflectiveQuadToRelative(3.93f, 1.57f)
            reflectiveQuadTo(29.5f, 24f)
            reflectiveQuadToRelative(-1.57f, 3.92f)
            reflectiveQuadTo(24f, 29.5f)
            reflectiveQuadTo(20.08f, 27.92f)
            close()
            moveToRelative(14.88f, 9.83f)
            lineToRelative(-5f, -5f)
            lineToRelative(2.8f, -2.8f)
            lineToRelative(5f, 5f)
            lineToRelative(-2.8f, 2.8f)
            close()
            moveToRelative(-21.9f, 0f)
            lineToRelative(-2.8f, -2.8f)
            lineToRelative(5f, -5f)
            lineToRelative(2.8f, 2.8f)
            lineToRelative(-5f, 5f)
            close()
            moveTo(22f, 46.95f)
            verticalLineTo(34.35f)
            horizontalLineToRelative(3.95f)
            verticalLineToRelative(12.6f)
            horizontalLineTo(22f)
            close()
          }
        }
        .build()
    return _flare!!
  }

private var _flare: ImageVector? = null

@Suppress("CheckReturnValue")
public val flare_filled: ImageVector
  get() {
    if (_flare_filled != null) {
      return _flare_filled!!
    }
    _flare_filled =
      ImageVector.Builder(
          name = "flare",
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
            moveTo(1.05f, 26f)
            verticalLineTo(22.05f)
            horizontalLineToRelative(12.6f)
            verticalLineTo(26f)
            horizontalLineTo(1.05f)
            close()
            moveToRelative(14.2f, -7.95f)
            lineTo(10.5f, 13.3f)
            lineToRelative(2.8f, -2.8f)
            lineToRelative(4.75f, 4.75f)
            lineToRelative(-2.8f, 2.8f)
            close()
            moveTo(22f, 13.65f)
            verticalLineTo(1.05f)
            horizontalLineToRelative(3.95f)
            verticalLineToRelative(12.6f)
            horizontalLineTo(22f)
            close()
            moveToRelative(10.75f, 4.4f)
            lineToRelative(-2.8f, -2.8f)
            lineTo(34.7f, 10.5f)
            lineToRelative(2.8f, 2.8f)
            lineToRelative(-4.75f, 4.75f)
            close()
            moveTo(34.35f, 26f)
            verticalLineTo(22.05f)
            horizontalLineToRelative(12.6f)
            verticalLineTo(26f)
            horizontalLineTo(34.35f)
            close()
            moveTo(20.08f, 27.92f)
            quadTo(18.5f, 26.35f, 18.5f, 24f)
            reflectiveQuadToRelative(1.58f, -3.93f)
            reflectiveQuadTo(24f, 18.5f)
            reflectiveQuadToRelative(3.93f, 1.57f)
            reflectiveQuadTo(29.5f, 24f)
            reflectiveQuadToRelative(-1.57f, 3.92f)
            reflectiveQuadTo(24f, 29.5f)
            reflectiveQuadTo(20.08f, 27.92f)
            close()
            moveToRelative(14.88f, 9.83f)
            lineToRelative(-5f, -5f)
            lineToRelative(2.8f, -2.8f)
            lineToRelative(5f, 5f)
            lineToRelative(-2.8f, 2.8f)
            close()
            moveToRelative(-21.9f, 0f)
            lineToRelative(-2.8f, -2.8f)
            lineToRelative(5f, -5f)
            lineToRelative(2.8f, 2.8f)
            lineToRelative(-5f, 5f)
            close()
            moveTo(22f, 46.95f)
            verticalLineTo(34.35f)
            horizontalLineToRelative(3.95f)
            verticalLineToRelative(12.6f)
            horizontalLineTo(22f)
            close()
          }
        }
        .build()
    return _flare_filled!!
  }

private var _flare_filled: ImageVector? = null
