package com.example.savestate.data.models

// main genres and their corresponding slug in the requests
enum class RawgGenreFilter(val slug: String, val displayName: String) {
    ACTION("action", "Action"),
    RPG("role-playing-games-rpg", "RPG"),
    SHOOTER("shooter", "Shooter"),
    ADVENTURE("adventure", "Adventure"),
    STRATEGY("strategy", "Strategy"),
    PUZZLE("puzzle", "Puzzle"),
    SPORTS("sports", "Sports"),
    RACING("racing", "Racing"),
    FIGHTING("fighting", "Fighting"),
    SIMULATION("simulation", "Simulation"),
    PLATFORMER("platformer", "Platformer"),
    INDIE("indie", "Indie")
}

// main platforms and their corresponding is in the requests
enum class RawgPlatformFilter(val id: Int, val displayName: String) {
    PC(4, "PC"),
    PS5(187, "PS5"),
    PS4(18, "PS4"),
    PS3(16, "PS3"),
    XBOX_SERIES(186, "Xbox Series"),
    XBOX_ONE(1, "Xbox One"),
    XBOX_360(14, "Xbox 360"),
    SWITCH(7, "Switch"),
    ANDROID(21, "Android"),
    IOS(3, "iOS")
}

// the possible ordering of the items in the search screen
enum class RawgOrdering(val value: String, val displayName: String) {
    RELEVANCE("", "Relevance"),
    RATING_DESC("-rating", "Rating ↓"),
    RATING_ASC("rating", "Rating ↑"),
    RELEASED_DESC("-released", "Newest first"),
    RELEASED_ASC("released", "Oldest first"),
    NAME_ASC("name", "Name A-Z"),
    NAME_DESC("-name", "Name Z-A")
}

/**
 * The filters used to create the RAWG request
 *
 * @param genres a set of genres to include in the response. Defaults to none.
 * @param platforms a set of platforms that must support the response's games. Defaults to none.
 * @param minRating the min RAWG rating of the games. Defaults to 0.
 * @param ordering how the games in the response must be displayed. Defaults to relevance.
 */
data class SearchFilters(
    val genres: Set<RawgGenreFilter> = emptySet(),
    val platforms: Set<RawgPlatformFilter> = emptySet(),
    val minRating: Float = 0f,
    val ordering: RawgOrdering = RawgOrdering.RELEVANCE
)