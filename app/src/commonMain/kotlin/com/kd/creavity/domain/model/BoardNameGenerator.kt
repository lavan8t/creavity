package com.kd.creavity.domain.model

import kotlin.random.Random

/**
 * Generates evocative 2-word fusion names for boards (e.g. "Cosmic Canvas", "Solar Drift").
 * Replaces generic "Untitled Board" placeholders with inspiring, memorable titles.
 */
object BoardNameGenerator {

    private val ADJECTIVES = listOf(
        "Cosmic", "Velvet", "Solar", "Amber", "Neon", "Astral", "Golden", "Cobalt", "Misty", "Radiant",
        "Lunar", "Frosted", "Quiet", "Vivid", "Starlit", "Harmonic", "Crystal", "Breezy", "Electric", "Emerald",
        "Rustic", "Saffron", "Midnight", "Swift", "Infinite", "Whimsical", "Verdant", "Celestial", "Prismatic", "Serene",
        "Luminous", "Obsidian", "Copper", "Ethereal", "Dappled", "Sunlit", "Indigo", "Gilded", "Auburn", "Cerulean",
        "Zephyr", "Echoing", "Blushing", "Enchanted", "Polished", "Wandering", "Shimmering", "Opal", "Silver", "Twilight",
        "Crimson", "Mystic", "Lucid", "Stellar", "Whispering", "Halcyon", "Vibrant", "Azure", "Noble", "Brave"
    )

    private val NOUNS = listOf(
        "Canvas", "Horizon", "Peak", "Breeze", "Drift", "Spark", "Stream", "Echo", "Wave", "Bloom",
        "Dune", "Summit", "Meadow", "Tide", "Nexus", "Cascade", "Pulse", "Grove", "Ridge", "Solitude",
        "Prism", "Odyssey", "Canopy", "Oasis", "Glade", "Harbor", "Comet", "Constellation", "Mirage", "Zenith",
        "Haven", "Pinnacle", "Canyon", "Fjord", "Reef", "Archipelago", "Valley", "Lagoon", "Glacier", "Spire",
        "Aurora", "Orbit", "Current", "Shore", "Vale", "Beacon", "Sanctuary", "Frontier", "Vista", "Matrix",
        "Nebula", "Cosmos", "Solace", "Silhouette", "Resonance", "Realm", "Pathway", "Tapestry", "Atrium", "Origin"
    )

    /**
     * Generates a unique 2-word title, attempting to avoid collisions with [existingTitles].
     */
    fun generate(existingTitles: Collection<String> = emptySet(), random: Random = Random): String {
        for (i in 0 until 50) {
            val adj = ADJECTIVES[random.nextInt(ADJECTIVES.size)]
            val noun = NOUNS[random.nextInt(NOUNS.size)]
            val candidate = "$adj $noun"
            if (!existingTitles.contains(candidate)) {
                return candidate
            }
        }
        // Fallback with random suffix if space exhausted
        val adj = ADJECTIVES[random.nextInt(ADJECTIVES.size)]
        val noun = NOUNS[random.nextInt(NOUNS.size)]
        return "$adj $noun"
    }
}
