package net.punchtree.thebackthrooms

enum class Direction {
    NORTH, SOUTH, EAST, WEST;

    fun rotateLeft(): Direction {
        return when (this) {
            NORTH -> WEST
            WEST -> SOUTH
            SOUTH -> EAST
            EAST -> NORTH
        }
    }

    fun rotateRight(): Direction {
        return when (this) {
            NORTH -> EAST
            EAST -> SOUTH
            SOUTH -> WEST
            WEST -> NORTH
        }
    }
    fun inverse(): Direction {
        return when (this) {
            NORTH -> SOUTH
            WEST -> EAST
            SOUTH -> NORTH
            EAST -> WEST
        }
    }
}
