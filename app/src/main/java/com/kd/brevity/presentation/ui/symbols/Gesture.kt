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
public val gesture: ImageVector
  get() {
    if (_gesture != null) {
      return _gesture!!
    }
    _gesture =
      ImageVector.Builder(
          name = "gesture",
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
            moveTo(28.9f, 43.05f)
            quadToRelative(-2.85f, 0f, -4.75f, -1.97f)
            reflectiveQuadTo(22.25f, 35.5f)
            quadToRelative(0f, -3.75f, 3.35f, -6.83f)
            reflectiveQuadTo(33.5f, 25.2f)
            quadTo(33.3f, 22.8f, 32.38f, 21.6f)
            reflectiveQuadTo(29.55f, 20.4f)
            quadToRelative(-2.1f, 0f, -3.93f, 1.4f)
            reflectiveQuadToRelative(-4.27f, 5.1f)
            quadToRelative(-2.7f, 4f, -5f, 5.73f)
            reflectiveQuadTo(11.5f, 34.35f)
            quadToRelative(-3f, 0f, -4.77f, -1.98f)
            reflectiveQuadTo(4.95f, 27.05f)
            quadToRelative(0f, -1.55f, 1.13f, -3.95f)
            reflectiveQuadTo(9.85f, 16.7f)
            quadTo(12f, 13.8f, 12.6f, 12.73f)
            reflectiveQuadTo(13.2f, 10.7f)
            quadToRelative(0f, -0.45f, -0.4f, -0.8f)
            reflectiveQuadTo(11.7f, 9.55f)
            quadToRelative(-0.8f, 0f, -1.7f, 0.58f)
            reflectiveQuadTo(8.05f, 11.9f)
            lineTo(4.95f, 8.7f)
            quadTo(6.35f, 6.85f, 8.1f, 5.88f)
            reflectiveQuadTo(11.8f, 4.9f)
            quadToRelative(2.7f, 0f, 4.25f, 1.65f)
            reflectiveQuadTo(17.6f, 10.4f)
            quadToRelative(0f, 2.05f, -0.85f, 3.8f)
            reflectiveQuadTo(13.2f, 19.9f)
            quadToRelative(-2.4f, 3.5f, -3.08f, 4.75f)
            reflectiveQuadTo(9.45f, 27.3f)
            quadToRelative(0f, 0.85f, 0.7f, 1.58f)
            reflectiveQuadTo(11.9f, 29.6f)
            quadToRelative(0.6f, 0f, 1.4f, -0.4f)
            reflectiveQuadTo(14.85f, 28f)
            quadToRelative(0.6f, -0.6f, 1.35f, -1.53f)
            reflectiveQuadTo(17.9f, 24.2f)
            quadToRelative(3.5f, -4.75f, 6.13f, -6.55f)
            reflectiveQuadToRelative(5.88f, -1.8f)
            quadToRelative(3.4f, 0f, 5.57f, 2.45f)
            reflectiveQuadTo(38f, 25f)
            horizontalLineToRelative(5.1f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(38f)
            quadToRelative(-0.45f, 6.1f, -2.98f, 9.8f)
            reflectiveQuadToRelative(-6.12f, 3.7f)
            close()
            moveTo(29f, 38.4f)
            quadToRelative(1.85f, 0f, 3.05f, -2.3f)
            reflectiveQuadTo(33.6f, 29.75f)
            quadToRelative(-2.95f, 0.35f, -4.85f, 2.13f)
            reflectiveQuadTo(26.85f, 36f)
            quadToRelative(0f, 1.15f, 0.52f, 1.77f)
            reflectiveQuadTo(29f, 38.4f)
            close()
          }
        }
        .build()
    return _gesture!!
  }

private var _gesture: ImageVector? = null

@Suppress("CheckReturnValue")
public val gesture_filled: ImageVector
  get() {
    if (_gesture_filled != null) {
      return _gesture_filled!!
    }
    _gesture_filled =
      ImageVector.Builder(
          name = "gesture",
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
            moveTo(28.9f, 43.05f)
            quadToRelative(-2.85f, 0f, -4.75f, -1.97f)
            reflectiveQuadTo(22.25f, 35.5f)
            quadToRelative(0f, -3.75f, 3.35f, -6.83f)
            reflectiveQuadTo(33.5f, 25.2f)
            quadTo(33.3f, 22.8f, 32.38f, 21.6f)
            reflectiveQuadTo(29.55f, 20.4f)
            quadToRelative(-2.1f, 0f, -3.93f, 1.4f)
            reflectiveQuadToRelative(-4.27f, 5.1f)
            quadToRelative(-2.7f, 4f, -5f, 5.73f)
            reflectiveQuadTo(11.5f, 34.35f)
            quadToRelative(-3f, 0f, -4.77f, -1.98f)
            reflectiveQuadTo(4.95f, 27.05f)
            quadToRelative(0f, -1.55f, 1.13f, -3.95f)
            reflectiveQuadTo(9.85f, 16.7f)
            quadTo(12f, 13.8f, 12.6f, 12.73f)
            reflectiveQuadTo(13.2f, 10.7f)
            quadToRelative(0f, -0.45f, -0.4f, -0.8f)
            reflectiveQuadTo(11.7f, 9.55f)
            quadToRelative(-0.8f, 0f, -1.7f, 0.58f)
            reflectiveQuadTo(8.05f, 11.9f)
            lineTo(4.95f, 8.7f)
            quadTo(6.35f, 6.85f, 8.1f, 5.88f)
            reflectiveQuadTo(11.8f, 4.9f)
            quadToRelative(2.7f, 0f, 4.25f, 1.65f)
            reflectiveQuadTo(17.6f, 10.4f)
            quadToRelative(0f, 2.05f, -0.85f, 3.8f)
            reflectiveQuadTo(13.2f, 19.9f)
            quadToRelative(-2.4f, 3.5f, -3.08f, 4.75f)
            reflectiveQuadTo(9.45f, 27.3f)
            quadToRelative(0f, 0.85f, 0.7f, 1.58f)
            reflectiveQuadTo(11.9f, 29.6f)
            quadToRelative(0.6f, 0f, 1.4f, -0.4f)
            reflectiveQuadTo(14.85f, 28f)
            quadToRelative(0.6f, -0.6f, 1.35f, -1.53f)
            reflectiveQuadTo(17.9f, 24.2f)
            quadToRelative(3.5f, -4.75f, 6.13f, -6.55f)
            reflectiveQuadToRelative(5.88f, -1.8f)
            quadToRelative(3.4f, 0f, 5.57f, 2.45f)
            reflectiveQuadTo(38f, 25f)
            horizontalLineToRelative(5.1f)
            verticalLineToRelative(4.55f)
            horizontalLineTo(38f)
            quadToRelative(-0.45f, 6.1f, -2.98f, 9.8f)
            reflectiveQuadToRelative(-6.12f, 3.7f)
            close()
            moveTo(29f, 38.4f)
            quadToRelative(1.85f, 0f, 3.05f, -2.3f)
            reflectiveQuadTo(33.6f, 29.75f)
            quadToRelative(-2.95f, 0.35f, -4.85f, 2.13f)
            reflectiveQuadTo(26.85f, 36f)
            quadToRelative(0f, 1.15f, 0.52f, 1.77f)
            reflectiveQuadTo(29f, 38.4f)
            close()
          }
        }
        .build()
    return _gesture_filled!!
  }

private var _gesture_filled: ImageVector? = null
