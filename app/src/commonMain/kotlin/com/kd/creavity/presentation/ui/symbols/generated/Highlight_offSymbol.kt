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
public val highlight_off: ImageVector
  get() {
    if (_highlight_off != null) {
      return _highlight_off!!
    }
    _highlight_off =
      ImageVector.Builder(
          name = "highlight_off",
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
            moveTo(12f, 13.68f)
            lineToRelative(2.5f, 2.5f)
            quadToRelative(0.35f, 0.35f, 0.84f, 0.34f)
            reflectiveQuadToRelative(0.84f, -0.36f)
            quadToRelative(0.35f, -0.35f, 0.35f, -0.84f)
            reflectiveQuadTo(16.18f, 14.48f)
            lineTo(13.68f, 12f)
            lineToRelative(2.5f, -2.5f)
            quadTo(16.53f, 9.15f, 16.53f, 8.66f)
            quadToRelative(0f, -0.49f, -0.35f, -0.84f)
            quadTo(15.83f, 7.47f, 15.34f, 7.47f)
            reflectiveQuadTo(14.5f, 7.82f)
            lineTo(12f, 10.33f)
            lineTo(9.5f, 7.82f)
            quadTo(9.15f, 7.47f, 8.68f, 7.47f)
            reflectiveQuadTo(7.85f, 7.82f)
            quadTo(7.5f, 8.17f, 7.5f, 8.66f)
            reflectiveQuadTo(7.85f, 9.5f)
            lineTo(10.33f, 12f)
            lineToRelative(-2.5f, 2.5f)
            quadTo(7.48f, 14.85f, 7.49f, 15.33f)
            reflectiveQuadToRelative(0.36f, 0.82f)
            reflectiveQuadTo(8.69f, 16.5f)
            quadToRelative(0.49f, 0f, 0.84f, -0.35f)
            lineTo(12f, 13.68f)
            close()
            moveToRelative(0f, 9.18f)
            quadTo(9.73f, 22.85f, 7.75f, 22f)
            quadTo(5.78f, 21.15f, 4.31f, 19.69f)
            reflectiveQuadTo(2f, 16.25f)
            reflectiveQuadTo(1.15f, 12f)
            reflectiveQuadTo(2f, 7.75f)
            quadTo(2.85f, 5.77f, 4.31f, 4.31f)
            reflectiveQuadTo(7.75f, 2f)
            reflectiveQuadTo(12f, 1.15f)
            quadToRelative(2.28f, 0f, 4.25f, 0.85f)
            reflectiveQuadToRelative(3.44f, 2.31f)
            quadTo(21.15f, 5.77f, 22f, 7.75f)
            reflectiveQuadTo(22.85f, 12f)
            quadToRelative(0f, 2.27f, -0.85f, 4.25f)
            reflectiveQuadToRelative(-2.31f, 3.44f)
            quadTo(18.23f, 21.15f, 16.25f, 22f)
            quadTo(14.28f, 22.85f, 12f, 22.85f)
            close()
          }
        }
        .build()
    return _highlight_off!!
  }

private var _highlight_off: ImageVector? = null
