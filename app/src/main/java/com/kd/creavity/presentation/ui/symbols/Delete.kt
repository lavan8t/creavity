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
public val delete: ImageVector
  get() {
    if (_delete != null) {
      return _delete!!
    }
    _delete =
      ImageVector.Builder(
          name = "delete",
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
            moveTo(12.65f, 43.05f)
            quadToRelative(-1.91f, 0f, -3.26f, -1.33f)
            reflectiveQuadTo(8.05f, 38.5f)
            verticalLineTo(10.9f)
            horizontalLineTo(5.15f)
            verticalLineTo(6.35f)
            horizontalLineToRelative(11.4f)
            verticalLineTo(4f)
            horizontalLineTo(31.4f)
            verticalLineTo(6.35f)
            horizontalLineTo(42.8f)
            verticalLineTo(10.9f)
            horizontalLineTo(39.9f)
            verticalLineTo(38.5f)
            quadToRelative(0f, 1.86f, -1.36f, 3.2f)
            reflectiveQuadTo(35.3f, 43.05f)
            horizontalLineTo(12.65f)
            close()
            moveTo(35.3f, 10.9f)
            horizontalLineTo(12.65f)
            verticalLineTo(38.5f)
            horizontalLineTo(35.3f)
            verticalLineTo(10.9f)
            close()
            moveTo(17.85f, 34.6f)
            horizontalLineToRelative(3.7f)
            verticalLineTo(14.7f)
            horizontalLineToRelative(-3.7f)
            verticalLineTo(34.6f)
            close()
            moveToRelative(8.65f, 0f)
            horizontalLineToRelative(3.75f)
            verticalLineTo(14.7f)
            horizontalLineTo(26.5f)
            verticalLineTo(34.6f)
            close()
            moveTo(12.65f, 10.9f)
            verticalLineTo(38.5f)
            verticalLineTo(10.9f)
            close()
          }
        }
        .build()
    return _delete!!
  }

private var _delete: ImageVector? = null

@Suppress("CheckReturnValue")
public val delete_filled: ImageVector
  get() {
    if (_delete_filled != null) {
      return _delete_filled!!
    }
    _delete_filled =
      ImageVector.Builder(
          name = "delete",
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
            moveTo(12.65f, 43.05f)
            quadToRelative(-1.81f, 0f, -3.2f, -1.35f)
            reflectiveQuadTo(8.05f, 38.5f)
            verticalLineTo(10.9f)
            horizontalLineTo(5.15f)
            verticalLineTo(6.35f)
            horizontalLineToRelative(11.4f)
            verticalLineTo(4f)
            horizontalLineTo(31.4f)
            verticalLineTo(6.35f)
            horizontalLineTo(42.8f)
            verticalLineTo(10.9f)
            horizontalLineTo(39.9f)
            verticalLineTo(38.5f)
            quadToRelative(0f, 1.86f, -1.36f, 3.2f)
            reflectiveQuadTo(35.3f, 43.05f)
            horizontalLineTo(12.65f)
            close()
            moveToRelative(5.2f, -8.45f)
            horizontalLineToRelative(3.7f)
            verticalLineTo(14.7f)
            horizontalLineToRelative(-3.7f)
            verticalLineTo(34.6f)
            close()
            moveToRelative(8.65f, 0f)
            horizontalLineToRelative(3.75f)
            verticalLineTo(14.7f)
            horizontalLineTo(26.5f)
            verticalLineTo(34.6f)
            close()
          }
        }
        .build()
    return _delete_filled!!
  }

private var _delete_filled: ImageVector? = null
