package net.punchtree.thebackthrooms.experimental.rectangle

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

const val canvasSize = 35
const val maxAspectRatio = 2.25

class RectangleExperiment {

    private lateinit var random: Random

    data class Point(val x: Int, val y: Int) : Comparable<Point> {
        fun modulo(divisor: Point): Point {
            return Point((x + divisor.x) % divisor.x, (y + divisor.y) % divisor.y)
        }

        override fun compareTo(other: Point): Int {
            return if (x == other.x) {
                y.compareTo(other.y)
            } else {
                x.compareTo(other.x)
            }
        }

        operator fun plus(secondDimensions: Point): Point {
            return Point(x + secondDimensions.x, y + secondDimensions.y)
        }
    }

    class Rectangle(p1: Point, p2: Point) {
        val min = Point(min(p1.x, p2.x), min(p1.y, p2.y))
        val max = Point(max(p1.x, p2.x), max(p1.y, p2.y))

        init {
            require(max.x <= canvasSize && max.y <= canvasSize)
        }

        fun contains(point: Point): Boolean {
            val wraparoundMin = Point(min.x + canvasSize, min.y + canvasSize)
            val wraparoundMax = Point(max.x + canvasSize, max.y + canvasSize)
            val inWraparoundX = point.x in wraparoundMin.x until wraparoundMax.x
            val inWraparoundY = point.y in wraparoundMin.y until wraparoundMax.y
            val inOriginalX = point.x in min.x until max.x
            val inOriginalY = point.y in min.y until max.y
            return (inWraparoundX || inOriginalX) && (inWraparoundY || inOriginalY)
        }
    }

    val canvasMax = Point(canvasSize, canvasSize)

    val canvas = Rectangle(Point(0, 0), canvasMax)
    val subs = mutableListOf<Rectangle>()
    val openCorners = sortedSetOf<Point>()
    // TODO right now, markers with negative coordinates are not being displayed because this is just a basic map. Create a wrapper type that allows us to override writing to auto-modulo the coordinates
    val markers = mutableMapOf<Point, Char>()

    // TODO we NEED to think about the concept of operations and backstepping, either through recursion, but probably more manageably, through state/a stack of operations where each operation can be undone
    // TODO we WANT to ensure that there are CORNERS all the time! If there aren't, we're pretty close to just a grid of bathrooms! Boring!
    //   - Current: Just randomize a row of the first rectangles and hope for some corners
    //   - Better: Ensure there are different cross-dimensions, or undo and try again!
    //   - Good: Use random offsets for the initial placements of the first span of rectangles! Then one side always has corners!
    //   - Best: Don't use an initial grid-span at all! Generate one bathroom and expand based on the seam opportunities (doors, showers, mirrors, windows, closets)!

    fun doRectangleExperiment(seed : Long = Random.Default.nextLong()) {

        print("Random Seed: $seed\n")
        random = Random(seed)

        // Create the first rectangle in the bottom corner
        val firstDimensions = getRandomBathroomDimensions()
        val initialSub = Rectangle(Point(0, 0), Point(firstDimensions.x, firstDimensions.y))
        subs.add(initialSub)

        println("Initial rectangle: ${firstDimensions.x} x ${firstDimensions.y}\n")

        // The first rectangle doesn't actually create a corner because we're using a wraparound canvas (in order to make a surreal infinite bathroom-space)
        // But, the fake effects we use to imitate wraparounds mean we don't actually want rooms spanning the edge of the canvas and wrapping around
        // So, we need to add a second rectangle to create a corner
        // In the future, we could smartly create a zigzag seam and implement the seamless teleports across that zigzag seam
        // With that system, full wraparound would be possible

        // In the meantime, with a small canvas, and the inability to do that, we'll just try to subdivide the remaining space on one axis in an appealing way

        val xDimensionsOfRectanglesAlongBottom = findIdealCombinations(canvasMax.x - firstDimensions.x, 4, 14).random(random)
        println("Ideal combinations for x: $xDimensionsOfRectanglesAlongBottom")

        // For each x dimension, we'll generate a random y dimension
        var xCounter = firstDimensions.x
        for (xSize in xDimensionsOfRectanglesAlongBottom) {
            val ySize = getRandomSecondDimension(xSize)
            val size = Point(xSize, ySize)
            println("Generated rectangle: ${size}")
            val p1 = Point(xCounter, (-3..3).random(random))
            val rectangle = Rectangle(p1, p1 + size)
            subs.add(rectangle)
            xCounter += xSize
        }

        val corners = bruteForceFindCorners()
        corners.forEach { markers[it] = '*' }

        // Try to generate a new rectangle
        val randCorner = corners.random(random)
        val constraints = findConstraints(randCorner)

        corners.forEach(::testCornerDirection)

        println(" -========= Initial Rectangle =========- ")
        printCanvas()
    }

    private fun getRandomFirstDimension(max: Int = Int.MAX_VALUE): Int {
        return (4..min(max, 14)).random(random)
    }

    /*
     * We need to come up with a data structure that represents a max in the second dimension for each possible value along the first dimension
     */

    private fun testCornerDirection(corner: Point) {
        val cornerType = getCornerType(corner)
        val xNorm = cornerType.xNorm
        val yNorm = cornerType.yNorm
        markers[Point(corner.x + xNorm, corner.y)] = 'o'
        markers[Point(corner.x, corner.y + yNorm)] = 'o'
    }

    private fun findAllConstraints2(corner: Point) {
        val cornerType = getCornerType(corner)
    }

    enum class CornerType(val xNorm: Int, val yNorm: Int) {
        BOTTOM_LEFT(1, 1),
        BOTTOM_RIGHT(-1, 1),
        TOP_LEFT(1, -1),
        TOP_RIGHT(-1, -1)
    }

    private fun getCornerType(corner: Point): CornerType {
        check(isFree(corner))
        return when {
            isOccupied(Point(corner.x - 1, corner.y)) && isOccupied(Point(corner.x, corner.y - 1)) -> CornerType.BOTTOM_LEFT
            isOccupied(Point(corner.x + 1, corner.y)) && isOccupied(Point(corner.x, corner.y - 1)) -> CornerType.BOTTOM_RIGHT
            isOccupied(Point(corner.x - 1, corner.y)) && isOccupied(Point(corner.x, corner.y + 1)) -> CornerType.TOP_LEFT
            isOccupied(Point(corner.x + 1, corner.y)) && isOccupied(Point(corner.x, corner.y + 1)) -> CornerType.TOP_RIGHT
            else -> throw IllegalArgumentException("Corner $corner is not a corner")
        }
    }


    private fun getRandomSecondDimension(firstDimension: Int, max: Int = Int.MAX_VALUE): Int {
        return when (firstDimension) {
            4 -> (7..min(max, 9)).random(random)
            5 -> (6..min(max, 10)).random(random)
            6 -> (5..min(max, 10)).random(random)
            7 -> (4..min(max, 10)).random(random)
            8 -> (4..min(max, 10)).random(random)
            9 -> (4..min(max, 8)).random(random)
            10 -> (5..min(max, 8)).random(random)
            // Above 10 and we are necessarily including a shower
            11,12 -> (6..min(max, 8)).random(random)
            13,14 -> (7..min(max, 8)).random(random)
            else -> throw IllegalArgumentException("First dimension $firstDimension is not valid")
        }
    }

    private fun getRandomBathroomDimensions(): Point {
//        val smallerPassedInConstraint = min(constraints.x, constraints.y)
//        val largerPassedInConstraint = max(constraints.x, constraints.y)
//        val maxSmaller = min(smallerPassedInConstraint, 10)
//        val maxLarger = min(largerPassedInConstraint, 15)
//        val smaller = (4..maxSmaller).random(random)
//        val maxLargerAccountingForAspectRatio = min((smaller * maxAspectRatio).toInt(), maxLarger)
//        val larger = (8..maxLargerAccountingForAspectRatio).random(random)
        val firstDimension = getRandomFirstDimension()
        val secondDimension = getRandomSecondDimension(firstDimension)
        return if (random.nextBoolean()) {
            Point(firstDimension, secondDimension)
        } else {
            Point(secondDimension, firstDimension)
        }
    }

    private fun isOccupied(point: Point) =
        subs.any { it.contains(point.modulo(canvasMax)) }

    private fun isFree(corner: Point) =
        !isOccupied(corner)

    data class Constraint(val dimension: Dimension, val v: Int) {
        enum class Dimension {
            X, Y
        }
    }

    private fun findConstraints(corner: Point) : List<Constraint> {
        check(isFree(corner))
//        check(isOccupied(Point(corner.x - 1, corner.y)))
//        check(isOccupied(Point(corner.x, corner.y - 1)))

        // find the maximum points along the width and height that are adjacent to other rectangles
//        var maxAdjacentX = corner.x
//        var isTouchingEdge = isOccupied(Point(maxAdjacentX, corner.y - 1))
//        var isInFreeSpace = isFree(Point(maxAdjacentX, corner.y))
//        while (isTouchingEdge && isInFreeSpace) {
//            maxAdjacentX += 1
//            isTouchingEdge = isOccupied(Point(maxAdjacentX, corner.y - 1))
//            isInFreeSpace = isFree(Point(maxAdjacentX, corner.y))
//            if (!isTouchingEdge || !isInFreeSpace) {
//                println("Breaking X at $maxAdjacentX, touching edge: $isTouchingEdge, expanding into free space: $isInFreeSpace\n")
//            }
//        }
//        maxAdjacentX -= 1
//
//        var maxAdjacentY = corner.y
//        isTouchingEdge = isOccupied(Point(corner.x - 1, maxAdjacentY)) || corner.x == 0
//        // TODO check whole edge length
//        isInFreeSpace = isFree(Point(corner.x, maxAdjacentY))
//        while (isTouchingEdge && isInFreeSpace) {
//            maxAdjacentY += 1
//            isTouchingEdge = isOccupied(Point(corner.x - 1, maxAdjacentY))
//            isInFreeSpace = isFree(Point(corner.x, maxAdjacentY))
//            if (!isTouchingEdge || !isInFreeSpace) {
//                println("Breaking Y at $maxAdjacentY, touching edge: $isTouchingEdge, expanding into free space: $isInFreeSpace\n")
//            }
//        }
//        maxAdjacentY -= 1

        val constraints = mutableListOf<Constraint>()

        var maxAdjacentX = corner.x
        var isInFreeSpace = isFree(Point(maxAdjacentX, corner.y))
        while (isInFreeSpace && maxAdjacentX - corner.x < 14) {
            maxAdjacentX += 1
            isInFreeSpace = isFree(Point(maxAdjacentX, corner.y))
            if (!isInFreeSpace) {
                println("Breaking X at $maxAdjacentX, expanding into free space: $isInFreeSpace\n")
                constraints.add(Constraint(Constraint.Dimension.X, maxAdjacentX))
            }
        }
        maxAdjacentX -= 1

        var maxAdjacentY = corner.y
        // TODO check whole edge length
        isInFreeSpace = isFree(Point(corner.x, maxAdjacentY))
        while (isInFreeSpace && maxAdjacentY - corner.y < 14) {
            maxAdjacentY += 1
            isInFreeSpace = isFree(Point(corner.x, maxAdjacentY))
            if (!isInFreeSpace) {
                println("Breaking Y at $maxAdjacentY, expanding into free space: $isInFreeSpace\n")
                constraints.add(Constraint(Constraint.Dimension.Y, maxAdjacentY))
            }
        }
        maxAdjacentY -= 1

        println("Max adjacent x: $maxAdjacentX, max adjacent y: $maxAdjacentY, constraints: $constraints\n")

        return constraints
    }

    private fun verifyNoOverlaps() : Boolean {
        for (sub in subs) {
            for (otherSub in subs) {
                if (sub == otherSub) continue
                if ((sub.min.x < otherSub.max.x && sub.max.x > otherSub.min.x) &&
                    (sub.min.y < otherSub.max.y && sub.max.y > otherSub.min.y)) {
                    println("Overlap detected between $sub and $otherSub")
                    return false
                }
            }
        }
        return true
    }

    private fun indexOfContainingSub(point: Point): Int {
        for ((index, sub) in subs.withIndex()) {
            if (sub.contains(point)) {
                return index
            }
        }
        throw IllegalArgumentException("No sub contains point $point")
    }

    private fun printCanvas() {
        for (x in canvasSize - 1 downTo 0) {
            for (y in 0 until canvasSize) {
                if (markers.contains(Point(x, y))) {
                    print("${markers[Point(x, y)]} ")
                } else if (isOccupied(Point(x, y))) {
                    print("${'A' + indexOfContainingSub(Point(x, y))} ")
//                    print("□ ")
                } else {
                    print("· ")
                }
            }
            println()
        }
    }

    private fun bruteForceFindCorners(): MutableList<Point> {
        val corners = mutableListOf<Point>()
        for (x in 0 until canvasSize) {
            for (y in 0 until canvasSize) {
                val point = Point(x, y)
                if (isOccupied(point)) continue
                val up = Point(x, y + 1)
                val right = Point(x + 1, y)
                val down = Point(x, y - 1)
                val left = Point(x - 1, y)
                if ((isOccupied(up) && isOccupied(right))
                    || (isOccupied(right) && isOccupied(down))
                    || (isOccupied(down) && isOccupied(left))
                    || (isOccupied(left) && isOccupied(up))) {
                    corners.add(point)
                }
            }
        }
        return corners
    }

    companion object {
        internal fun findAllSumCombinations(sum: Int, min: Int, max: Int): List<List<Int>> {
            val combinations = mutableListOf<List<Int>>()
            for (i in min..max) {
                if (i == sum) {
                    combinations.add(listOf(i))
                } else if (i < sum) {
                    val subCombinations = findAllSumCombinations(sum - i, min, max)
                    for (subCombination in subCombinations) {
                        combinations.add(listOf(i) + subCombination)
                    }
                }
            }
            return combinations
        }

        internal fun findIdealCombinations(sum: Int, min: Int, max: Int): List<List<Int>> {
            val combinations = findAllSumCombinations(sum, min, max)
            val combinationsWithNoRepeatNumbers = combinations.filter {
                val toSet = it.toSet()
                toSet.size == it.size && toSet.any((7..10)::contains)
            }
            return combinationsWithNoRepeatNumbers
        }
    }

}

fun main() {
    RectangleExperiment().doRectangleExperiment()
}
