package net.punchtree.thebackthrooms

import net.punchtree.thebackthrooms.BathroomTopDownPrinter.printBathroom
import kotlin.math.min
import kotlin.random.Random

class BathroomBuilder (val random: Random) {

    fun buildBathroom() {
        val width = (4..10).random()
        val maxSizeRatio = 2.25
        val length = (8..min((width * maxSizeRatio).toInt(), 15)).random()

        val bathroom = Bathroom(width, length)
        bathroom.calculateWalls()

        printBathroom(bathroom)
        placeSink(bathroom)
    }

    private fun placeDoor(bathroom: Bathroom) {
        if (bathroom.smallerDimension() < 6) {
            // do skinny/small bathroom layout
            // place door
            // from there sink/toilet position are determined
            // maybe bathtub/shower
            if (bathroom.isRectangular()) {
                val randomWall = bathroom.walls.random(random)
                TODO("Select center point of wall to place door")
            } else {
                TODO("figure it out")
            }

        } else {
            // do big bathroom things
        }
    }

    private fun placeSink(bathroom: Bathroom) {
        if (bathroom.smallerDimension() < 6) {
            // do skinny/small bathroom layout
            // place door
            // from there sink/toilet position are determined
            // maybe bathtub/shower

        } else {
            // do big bathroom things
        }
    }
}