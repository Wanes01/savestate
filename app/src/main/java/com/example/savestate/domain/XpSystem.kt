package com.example.savestate.domain

object XpSystem {
    private const val XP_PER_MINUTE = 2
    private const val XP_GAME_COMPLETED = 100
    private const val LEVEL_BASE_XP = 100

    /**
     * Maps the day streak to an XP multiplier.
     */
    fun streakMultiplier(dayStreak: Int): Double = when {
        dayStreak >= 30 -> 2.00
        dayStreak >= 14 -> 1.75
        dayStreak >= 7 -> 1.50
        dayStreak >= 3 -> 1.25
        else -> 1.00
    }

    /**
     * Achievements are categorized by rarity.
     * The harder an achievement is to earn, the more XP it grants.
     */
    enum class AchievementTier(val baseXp: Int, val label: String) {
        COMMON(10, "Common"),
        RARE(25, "Rare"),
        EPIC(60, "Epic"),
        LEGENDARY(150, "Legendary")
    }


    /**
     * Map the completion percentage of an achievement to its rarity.
     */
    fun achievementTier(completionPercent: Float): AchievementTier = when {
        completionPercent < 2f -> AchievementTier.LEGENDARY // < 2% of players
        completionPercent < 10f -> AchievementTier.EPIC // 2-10% of players
        completionPercent < 40f -> AchievementTier.RARE // 10-40% of players
        else -> AchievementTier.COMMON // > 40% of players
    }

    // xp methods

    /**
     * Computes the XP earned based on the duration of a gaming
     * session in minutes and the daily streak
     */
    fun xpForSession(durationMinutes: Int, dayStreak: Int): Int {
        val base = durationMinutes * XP_PER_MINUTE
        return (base * streakMultiplier(dayStreak)).toInt()
    }

    /**
     * Calculate the XP earned based on the average completion percentage
     * of an achievement and the daily streak
     */
    fun xpForAchievement(completionPercent: Float, dayStreak: Int): Int {
        val base = achievementTier(completionPercent).baseXp
        return (base * streakMultiplier(dayStreak)).toInt()
    }

    /**
     * Calculate the XP earned after completing a game
     * and based on the daily streak
     */
    fun xpForGameCompleted(dayStreak: Int): Int {
        return (XP_GAME_COMPLETED * streakMultiplier(dayStreak)).toInt()
    }

    // level methods

    /**
     * Total XP required to reach a specific level (starting from level 0)
     *
     * A simple quadratic formula is used to track the level and make
     * the progression more challenging
     */
    fun xpToReachLevel(level: Int): Int = LEVEL_BASE_XP * level * level


    /**
     * Returns the level corresponding to a total amount of XP
     */
    fun levelFromXp(totalXp: Int): Int {
        var level = 0
        while (xpToReachLevel(level + 1) <= totalXp) level++
        return level
    }

    /**
     * The amount of XP needed to advance to the next level
     * is based on the current total XP
     */
    fun xpToNextLevel(totalXp: Int): Int {
        val current = levelFromXp(totalXp)
        return xpToReachLevel(current + 1) - totalXp
    }

    /**
     * Returns the progress within the current level (a value between 0 and 1)
     * based on the current total XP
     */
    fun levelProgress(totalXp: Int): Float {
        val current = levelFromXp(totalXp)
        val xpAtCurrentLevel = xpToReachLevel(current)
        val xpAtNextLevel = xpToReachLevel(current + 1)
        return (totalXp - xpAtCurrentLevel).toFloat() / (xpAtNextLevel - xpAtCurrentLevel)
    }

    /**
     * Assign a symbolic title to each level
     */
    fun levelTitle(level: Int): String = when (level) {
        0 -> "Noobie"
        1 -> "Player One"
        2 -> "Side Quester"
        3 -> "Grinder"
        4 -> "Achievement Hunter"
        5 -> "Speed Runner"
        6 -> "Completionist"
        7 -> "Gaming Wizard"
        8 -> "Loot Goblin"
        9 -> "Final Boss"
        else -> "Legend (Lv.$level)"
    }
}