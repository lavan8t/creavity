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
public val visibility: ImageVector
  get() {
    if (_visibility != null) {
      return _visibility!!
    }
    _visibility =
      ImageVector.Builder(
          name = "visibility",
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
            moveTo(29.93f, 28.93f)
            quadTo(32.35f, 26.5f, 32.35f, 23f)
            reflectiveQuadTo(29.93f, 17.08f)
            reflectiveQuadTo(24f, 14.65f)
            reflectiveQuadToRelative(-5.93f, 2.43f)
            reflectiveQuadTo(15.65f, 23f)
            reflectiveQuadToRelative(2.42f, 5.93f)
            reflectiveQuadTo(24f, 31.35f)
            reflectiveQuadToRelative(5.93f, -2.42f)
            close()
            moveTo(20.6f, 26.4f)
            quadTo(19.2f, 25.01f, 19.2f, 23f)
            reflectiveQuadToRelative(1.4f, -3.4f)
            reflectiveQuadTo(24f, 18.2f)
            reflectiveQuadToRelative(3.4f, 1.4f)
            reflectiveQuadTo(28.8f, 23f)
            reflectiveQuadToRelative(-1.4f, 3.4f)
            reflectiveQuadTo(24f, 27.8f)
            reflectiveQuadTo(20.6f, 26.4f)
            close()
            moveTo(10.28f, 34.55f)
            quadTo(4.1f, 30.1f, 1.05f, 23f)
            quadTo(4.1f, 15.9f, 10.28f, 11.45f)
            reflectiveQuadTo(24f, 7f)
            reflectiveQuadToRelative(13.73f, 4.45f)
            reflectiveQuadTo(46.95f, 23f)
            quadTo(43.9f, 30.1f, 37.73f, 34.55f)
            reflectiveQuadTo(24f, 39f)
            reflectiveQuadTo(10.28f, 34.55f)
            close()
            moveTo(24f, 23f)
            close()
            moveToRelative(11.04f, 8.75f)
            quadTo(40.04f, 28.49f, 42.7f, 23f)
            quadTo(40.04f, 17.51f, 35.04f, 14.25f)
            reflectiveQuadTo(24f, 11f)
            reflectiveQuadTo(12.93f, 14.25f)
            reflectiveQuadTo(5.25f, 23f)
            quadToRelative(2.66f, 5.49f, 7.69f, 8.75f)
            reflectiveQuadTo(24f, 35f)
            reflectiveQuadTo(35.04f, 31.75f)
            close()
          }
        }
        .build()
    return _visibility!!
  }

private var _visibility: ImageVector? = null

@Suppress("CheckReturnValue")
public val visibility_filled: ImageVector
  get() {
    if (_visibility_filled != null) {
      return _visibility_filled!!
    }
    _visibility_filled =
      ImageVector.Builder(
          name = "visibility",
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
            moveTo(29.93f, 28.93f)
            quadTo(32.35f, 26.5f, 32.35f, 23f)
            reflectiveQuadTo(29.93f, 17.08f)
            reflectiveQuadTo(24f, 14.65f)
            reflectiveQuadToRelative(-5.93f, 2.43f)
            reflectiveQuadTo(15.65f, 23f)
            reflectiveQuadToRelative(2.42f, 5.93f)
            reflectiveQuadTo(24f, 31.35f)
            reflectiveQuadToRelative(5.93f, -2.42f)
            close()
            moveTo(20.6f, 26.4f)
            quadTo(19.2f, 25f, 19.2f, 23f)
            reflectiveQuadToRelative(1.4f, -3.4f)
            reflectiveQuadTo(24f, 18.2f)
            reflectiveQuadToRelative(3.4f, 1.4f)
            reflectiveQuadTo(28.8f, 23f)
            reflectiveQuadToRelative(-1.4f, 3.4f)
            reflectiveQuadTo(24f, 27.8f)
            reflectiveQuadTo(20.6f, 26.4f)
            close()
            moveTo(10.28f, 34.55f)
            quadTo(4.1f, 30.1f, 1.05f, 23f)
            quadTo(4.1f, 15.9f, 10.28f, 11.45f)
            reflectiveQuadTo(24f, 7f)
            reflectiveQuadToRelative(13.73f, 4.45f)
            reflectiveQuadTo(46.95f, 23f)
            quadTo(43.9f, 30.1f, 37.73f, 34.55f)
            reflectiveQuadTo(24f, 39f)
            reflectiveQuadTo(10.28f, 34.55f)
            close()
          }
        }
        .build()
    return _visibility_filled!!
  }

private var _visibility_filled: ImageVector? = null
