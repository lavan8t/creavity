package com.kd.creavity.domain.mutation

import com.kd.creavity.domain.model.BoardPage
import com.kd.creavity.domain.model.ImageElement
import com.kd.creavity.domain.model.Stroke
import com.kd.creavity.domain.model.TextElement
import java.util.UUID

sealed interface BoardMutation {
    val id: String
    val timestamp: Long

    fun apply(page: BoardPage): BoardPage
    fun revert(page: BoardPage): BoardPage

    data class AddStroke(
        val stroke: Stroke,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : BoardMutation {
        override fun apply(page: BoardPage): BoardPage =
            page.copy(strokes = page.strokes.filterNot { it.id == stroke.id } + stroke)

        override fun revert(page: BoardPage): BoardPage =
            page.copy(strokes = page.strokes.filterNot { it.id == stroke.id })
    }

    data class DeleteStroke(
        val stroke: Stroke,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : BoardMutation {
        override fun apply(page: BoardPage): BoardPage =
            page.copy(strokes = page.strokes.filterNot { it.id == stroke.id })

        override fun revert(page: BoardPage): BoardPage =
            if (page.strokes.none { it.id == stroke.id }) page.copy(strokes = page.strokes + stroke) else page
    }

    data class AddText(
        val textElement: TextElement,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : BoardMutation {
        override fun apply(page: BoardPage): BoardPage =
            page.copy(textElements = page.textElements.filterNot { it.id == textElement.id } + textElement)

        override fun revert(page: BoardPage): BoardPage =
            page.copy(textElements = page.textElements.filterNot { it.id == textElement.id })
    }

    data class DeleteText(
        val textElement: TextElement,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : BoardMutation {
        override fun apply(page: BoardPage): BoardPage =
            page.copy(textElements = page.textElements.filterNot { it.id == textElement.id })

        override fun revert(page: BoardPage): BoardPage =
            if (page.textElements.none { it.id == textElement.id }) page.copy(textElements = page.textElements + textElement) else page
    }

    data class AddImage(
        val imageElement: ImageElement,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : BoardMutation {
        override fun apply(page: BoardPage): BoardPage =
            page.copy(imageElements = page.imageElements.filterNot { it.id == imageElement.id } + imageElement)

        override fun revert(page: BoardPage): BoardPage =
            page.copy(imageElements = page.imageElements.filterNot { it.id == imageElement.id })
    }

    data class DeleteImage(
        val imageElement: ImageElement,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : BoardMutation {
        override fun apply(page: BoardPage): BoardPage =
            page.copy(imageElements = page.imageElements.filterNot { it.id == imageElement.id })

        override fun revert(page: BoardPage): BoardPage =
            if (page.imageElements.none { it.id == imageElement.id }) page.copy(imageElements = page.imageElements + imageElement) else page
    }

    data class MoveElements(
        val previousStrokes: List<Stroke>,
        val newStrokes: List<Stroke>,
        val previousTexts: List<TextElement>,
        val newTexts: List<TextElement>,
        val previousImages: List<ImageElement>,
        val newImages: List<ImageElement>,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : BoardMutation {
        override fun apply(page: BoardPage): BoardPage {
            val strokeMap = newStrokes.associateBy { it.id }
            val textMap = newTexts.associateBy { it.id }
            val imageMap = newImages.associateBy { it.id }
            return page.copy(
                strokes = page.strokes.map { strokeMap[it.id] ?: it },
                textElements = page.textElements.map { textMap[it.id] ?: it },
                imageElements = page.imageElements.map { imageMap[it.id] ?: it }
            )
        }

        override fun revert(page: BoardPage): BoardPage {
            val strokeMap = previousStrokes.associateBy { it.id }
            val textMap = previousTexts.associateBy { it.id }
            val imageMap = previousImages.associateBy { it.id }
            return page.copy(
                strokes = page.strokes.map { strokeMap[it.id] ?: it },
                textElements = page.textElements.map { textMap[it.id] ?: it },
                imageElements = page.imageElements.map { imageMap[it.id] ?: it }
            )
        }
    }

    data class RecolorElements(
        val previousStrokes: List<Stroke>,
        val newStrokes: List<Stroke>,
        val previousTexts: List<TextElement>,
        val newTexts: List<TextElement>,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : BoardMutation {
        override fun apply(page: BoardPage): BoardPage {
            val strokeMap = newStrokes.associateBy { it.id }
            val textMap = newTexts.associateBy { it.id }
            return page.copy(
                strokes = page.strokes.map { strokeMap[it.id] ?: it },
                textElements = page.textElements.map { textMap[it.id] ?: it }
            )
        }

        override fun revert(page: BoardPage): BoardPage {
            val strokeMap = previousStrokes.associateBy { it.id }
            val textMap = previousTexts.associateBy { it.id }
            return page.copy(
                strokes = page.strokes.map { strokeMap[it.id] ?: it },
                textElements = page.textElements.map { textMap[it.id] ?: it }
            )
        }
    }

    data class ClearPage(
        val previousStrokes: List<Stroke>,
        val previousTexts: List<TextElement>,
        val previousImages: List<ImageElement>,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : BoardMutation {
        override fun apply(page: BoardPage): BoardPage =
            page.copy(strokes = emptyList(), textElements = emptyList(), imageElements = emptyList())

        override fun revert(page: BoardPage): BoardPage =
            page.copy(strokes = previousStrokes, textElements = previousTexts, imageElements = previousImages)
    }

    data class RestoreVersion(
        val previousStrokes: List<Stroke>,
        val newStrokes: List<Stroke>,
        val previousTexts: List<TextElement>,
        val newTexts: List<TextElement>,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : BoardMutation {
        override fun apply(page: BoardPage): BoardPage =
            page.copy(strokes = newStrokes, textElements = newTexts)

        override fun revert(page: BoardPage): BoardPage =
            page.copy(strokes = previousStrokes, textElements = previousTexts)
    }
}
