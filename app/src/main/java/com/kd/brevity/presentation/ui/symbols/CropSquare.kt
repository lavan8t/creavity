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
public val crop_square: ImageVector
  get() {
    if (_crop_square != null) {
      return _crop_square!!
    }
    _crop_square =
      ImageVector.Builder(
          name = "crop_square",
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
            quadToRelative(-1.86f, 0f, -3.2f, -1.35f)
            reflectiveQuadTo(4.95f, 38.5f)
            verticalLineTo(9.5f)
            quadTo(4.95f, 7.62f, 6.3f, 6.26f)
            reflectiveQuadTo(9.5f, 4.9f)
            horizontalLineToRelative(29f)
            quadToRelative(1.88f, 0f, 3.24f, 1.36f)
            reflectiveQuadTo(43.1f, 9.5f)
            verticalLineToRelative(29f)
            quadToRelative(0f, 1.86f, -1.36f, 3.2f)
            reflectiveQuadTo(38.5f, 43.05f)
            horizontalLineTo(9.5f)
            close()
            moveToRelative(0f, -4.55f)
            horizontalLineToRelative(29f)
            verticalLineTo(9.5f)
            horizontalLineTo(9.5f)
            verticalLineToRelative(29f)
            close()
            moveToRelative(0f, 0f)
            verticalLineTo(9.5f)
            verticalLineToRelative(29f)
            close()
          }
        }
        .build()
    return _crop_square!!
  }

private var _crop_square: ImageVector? = null

@Suppress("CheckReturnValue")
public val crop_square_filled: ImageVector
  get() {
    if (_crop_square_filled != null) {
      return _crop_square_filled!!
    }
    _crop_square_filled =
      ImageVector.Builder(
          name = "crop_square",
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
            quadToRelative(-1.86f, 0f, -3.2f, -1.35f)
            reflectiveQuadTo(4.95f, 38.5f)
            verticalLineTo(9.5f)
            quadTo(4.95f, 7.62f, 6.3f, 6.26f)
            reflectiveQuadTo(9.5f, 4.9f)
            horizontalLineToRelative(29f)
            quadToRelative(1.88f, 0f, 3.24f, 1.36f)
            reflectiveQuadTo(43.1f, 9.5f)
            verticalLineToRelative(29f)
            quadToRelative(0f, 1.86f, -1.36f, 3.2f)
            reflectiveQuadTo(38.5f, 43.05f)
            horizontalLineTo(9.5f)
            close()
          }
        }
        .build()
    return _crop_square_filled!!
  }

private var _crop_square_filled: ImageVector? = null
