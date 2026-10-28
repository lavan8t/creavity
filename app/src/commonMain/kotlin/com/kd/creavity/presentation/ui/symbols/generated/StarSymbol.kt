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
public val star: ImageVector
  get() {
    if (_star != null) {
      return _star!!
    }
    _star =
      ImageVector.Builder(
          name = "star",
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
            moveTo(12f, 18.23f)
            lineTo(7.78f, 20.78f)
            quadTo(7.33f, 21.05f, 6.85f, 21.01f)
            reflectiveQuadTo(6.03f, 20.7f)
            quadTo(5.68f, 20.43f, 5.49f, 20f)
            reflectiveQuadTo(5.43f, 19.08f)
            lineToRelative(1.13f, -4.8f)
            lineTo(2.8f, 11.02f)
            quadTo(2.4f, 10.68f, 2.3f, 10.23f)
            quadTo(2.2f, 9.77f, 2.33f, 9.35f)
            quadTo(2.45f, 8.92f, 2.81f, 8.63f)
            quadTo(3.18f, 8.32f, 3.7f, 8.27f)
            lineTo(8.63f, 7.85f)
            lineTo(10.55f, 3.3f)
            quadToRelative(0.2f, -0.5f, 0.6f, -0.72f)
            reflectiveQuadTo(12f, 2.35f)
            reflectiveQuadToRelative(0.85f, 0.23f)
            reflectiveQuadToRelative(0.6f, 0.72f)
            lineToRelative(1.92f, 4.55f)
            lineTo(20.3f, 8.27f)
            quadToRelative(0.53f, 0.05f, 0.89f, 0.35f)
            quadToRelative(0.36f, 0.3f, 0.49f, 0.72f)
            quadToRelative(0.13f, 0.42f, 0.03f, 0.88f)
            reflectiveQuadToRelative(-0.5f, 0.8f)
            lineToRelative(-3.75f, 3.25f)
            lineToRelative(1.13f, 4.8f)
            quadTo(18.7f, 19.58f, 18.51f, 20f)
            reflectiveQuadToRelative(-0.54f, 0.7f)
            reflectiveQuadToRelative(-0.82f, 0.31f)
            reflectiveQuadTo(16.23f, 20.78f)
            lineTo(12f, 18.23f)
            close()
          }
        }
        .build()
    return _star!!
  }

private var _star: ImageVector? = null
