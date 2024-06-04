package net.punchtree.thebackthrooms

data class BathroomColumn(val bathroom: Bathroom,
                          val xBathroom: Int,
                          val yBathroom: Int,
                          val cells : Array<BathroomCell>,
                          var isWall: Boolean = false) {

    var wallPositions = mutableListOf<WallPosition>()

    fun adjacent(x: Int, y: Int, rotation: Direction = Direction.NORTH): BathroomColumn {
        return when (rotation) {
            Direction.NORTH -> {
                bathroom[xBathroom + x, yBathroom + y]
            }
            Direction.EAST -> {
                bathroom[xBathroom - y, yBathroom + x]
            }
            Direction.SOUTH -> {
                bathroom[xBathroom - x, yBathroom - y]
            }
            Direction.WEST -> {
                bathroom[xBathroom + y, yBathroom - x]
            }
        }
    }

    fun right(rotation: Direction = Direction.NORTH) = adjacent(1, 0, rotation)
    fun forward(rotation: Direction = Direction.NORTH) = adjacent(0, -1, rotation)
    fun left(rotation: Direction = Direction.NORTH) = adjacent(-1, 0, rotation)
    fun backward(rotation: Direction = Direction.NORTH) = adjacent(0, 1, rotation)
    fun forwardRight(rotation: Direction = Direction.NORTH) = adjacent(1, -1, rotation)
    fun forwardLeft(rotation: Direction = Direction.NORTH) = adjacent(-1, -1, rotation)
    fun backwardRight(rotation: Direction = Direction.NORTH) = adjacent(1, 1, rotation)
    fun backwardLeft(rotation: Direction = Direction.NORTH) = adjacent(-1, 1, rotation)
}
