package net.punchtree.thebackthrooms

import kotlin.math.min

data class Bathroom(val width: Int, val length: Int) {

    var cells : Array<Array<BathroomColumn>> = Array(width + 2) { row ->
        Array(length + 2){col ->
            BathroomColumn(this, row, col, Array(10){
                BathroomCell()
            })
        }
    }

    lateinit var walls : List<BathroomWall>

    init {
        // Set all the perimeter cells to walls
        for (i in 0 until width + 2) {
            cells[i][0].isWall = true
            cells[i][length + 1].isWall = true
        }
        for (i in 0 until length + 2) {
            cells[0][i].isWall = true
            cells[width + 1][i].isWall = true
        }
    }

    fun smallerDimension() = min(width, length)

    fun size() = width * length

    fun calculateWalls() {
        // Start in the top left. Then iterate around following all walls
        val allWalls = mutableListOf<BathroomWall>()

        var x = 0
        var y = 0

        // Start by finding the initial position. Start at the top left. Iterate to the right and down until we find a
        // position that is air
        var initialPosition = this[x, y]
        while (initialPosition.isWall) {
            if (x < width) {
                x++
            } else {
                x = 0
                y++
            }
            initialPosition = this[x, y]
        }

        var currentPosition = initialPosition
        var currentRotation = Direction.NORTH
        // We use the following algorithm to iterate through spots adjacent to the wall:
        // If there is not a wall in front of us, rotate left and move right
        // Else, if there is not a wall to our right, move right
        // Else, rotate right
        var currentWallPositions = mutableListOf<WallPosition>()
        currentWallPositions.add(WallPosition(this, currentPosition, Direction.NORTH))
        currentPosition.wallPositions.add(currentWallPositions.last())
        do {
            val right = currentPosition.right(currentRotation)
            val forward = currentPosition.forward(currentRotation)
            println("Current position: (${currentPosition.xBathroom},${currentPosition.yBathroom}), Rotation: $currentRotation, Right: ${right.isWall}, Forward: ${forward.isWall}, WallPos: ${currentPosition.wallPositions.size}")
            if (!forward.isWall) {
                allWalls.add(BathroomWall(currentRotation.inverse(),currentWallPositions))
                currentWallPositions = mutableListOf()
                currentRotation = currentRotation.rotateLeft()
                check(currentPosition.right(currentRotation) == forward)
                currentPosition = forward
            } else if (!right.isWall) {
                currentPosition = right
            } else {
                allWalls.add(BathroomWall(currentRotation.inverse(),currentWallPositions))
                currentWallPositions = mutableListOf()
                currentRotation = currentRotation.rotateRight()
            }
            currentWallPositions.add(WallPosition(this, currentPosition, currentRotation))
            currentPosition.wallPositions.add(currentWallPositions.last())
        } while (currentPosition != initialPosition || currentRotation != Direction.WEST)

        this.walls =  allWalls
    }

    operator fun get(x: Int, y: Int): BathroomColumn {
        return cells[x][y]
    }

    fun isRectangular(): Boolean {
        return walls.size == 4

    }

}

