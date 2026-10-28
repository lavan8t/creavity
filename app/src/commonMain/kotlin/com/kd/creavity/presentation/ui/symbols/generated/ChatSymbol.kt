package com.kd.creavity.presentation.ui.symbols.generated

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val chat: ImageVector
  get() {
    if (_chat != null) {
      return _chat!!
    }
    _chat =
      ImageVector.Builder(
          name = "chat",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
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
            moveTo(5.15f, 18.85f)
            lineToRelative(-1.3f, 1.3f)
            quadToRelative(-0.75f, 0.75f, -1.73f, 0.35f)
            reflectiveQuadTo(1.15f, 19.05f)
            verticalLineTo(4.3f)
            quadTo(1.15f, 2.98f, 2.06f, 2.06f)
            reflectiveQuadTo(4.3f, 1.15f)
            horizontalLineTo(19.7f)
            quadTo(21.03f, 1.15f, 21.94f, 2.06f)
            reflectiveQuadTo(22.85f, 4.3f)
            verticalLineTo(15.7f)
            quadTo(22.85f, 17.03f, 21.94f, 17.94f)
            reflectiveQuadTo(19.7f, 18.85f)
            horizontalLineTo(5.15f)
            close()
            moveTo(7.0f, 14.0f)
            horizontalLineToRelative(6.0f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            reflectiveQuadTo(14.0f, 13.0f)
            reflectiveQuadTo(13.71f, 12.29f)
            reflectiveQuadTo(13.0f, 12.0f)
            horizontalLineTo(7.0f)
            quadToRelative(-0.43f, 0f, -0.71f, 0.29f)
            reflectiveQuadTo(6.0f, 13.0f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(7.0f, 14.0f)
            close()
            moveTo(7.0f, 11.0f)
            horizontalLineToRelative(10.0f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            reflectiveQuadTo(18.0f, 10.0f)
            reflectiveQuadTo(17.71f, 9.29f)
            reflectiveQuadTo(17.0f, 9.0f)
            horizontalLineTo(7.0f)
            quadToRelative(-0.43f, 0f, -0.71f, 0.29f)
            reflectiveQuadTo(6.0f, 10.0f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(7.0f, 11.0f)
            close()
            moveTo(7.0f, 8.0f)
            horizontalLineToRelative(10.0f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            reflectiveQuadTo(18.0f, 7.0f)
            reflectiveQuadTo(17.71f, 6.29f)
            reflectiveQuadTo(17.0f, 6.0f)
            horizontalLineTo(7.0f)
            quadToRelative(-0.43f, 0f, -0.71f, 0.29f)
            reflectiveQuadTo(6.0f, 7.0f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(7.0f, 8.0f)
            close()
          }
        }
        .build()
    return _chat!!
  }

private var _chat: ImageVector? = null
