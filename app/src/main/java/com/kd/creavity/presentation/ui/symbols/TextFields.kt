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
public val text_fields: ImageVector
  get() {
    if (_text_fields != null) {
      return _text_fields!!
    }
    _text_fields =
      ImageVector.Builder(
          name = "text_fields",
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
            moveTo(13.4f, 41.1f)
            verticalLineTo(13f)
            horizontalLineTo(2.9f)
            verticalLineTo(6.9f)
            horizontalLineTo(30f)
            verticalLineTo(13f)
            horizontalLineTo(19.5f)
            verticalLineTo(41.1f)
            horizontalLineTo(13.4f)
            close()
            moveToRelative(19.1f, 0f)
            verticalLineToRelative(-17f)
            horizontalLineTo(26f)
            verticalLineTo(18f)
            horizontalLineTo(45.1f)
            verticalLineToRelative(6.1f)
            horizontalLineTo(38.6f)
            verticalLineToRelative(17f)
            horizontalLineTo(32.5f)
            close()
          }
        }
        .build()
    return _text_fields!!
  }

private var _text_fields: ImageVector? = null

@Suppress("CheckReturnValue")
public val text_fields_filled: ImageVector
  get() {
    if (_text_fields_filled != null) {
      return _text_fields_filled!!
    }
    _text_fields_filled =
      ImageVector.Builder(
          name = "text_fields",
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
            moveTo(13.4f, 41.1f)
            verticalLineTo(13f)
            horizontalLineTo(2.9f)
            verticalLineTo(6.9f)
            horizontalLineTo(30f)
            verticalLineTo(13f)
            horizontalLineTo(19.5f)
            verticalLineTo(41.1f)
            horizontalLineTo(13.4f)
            close()
            moveToRelative(19.1f, 0f)
            verticalLineToRelative(-17f)
            horizontalLineTo(26f)
            verticalLineTo(18f)
            horizontalLineTo(45.1f)
            verticalLineToRelative(6.1f)
            horizontalLineTo(38.6f)
            verticalLineToRelative(17f)
            horizontalLineTo(32.5f)
            close()
          }
        }
        .build()
    return _text_fields_filled!!
  }

private var _text_fields_filled: ImageVector? = null
