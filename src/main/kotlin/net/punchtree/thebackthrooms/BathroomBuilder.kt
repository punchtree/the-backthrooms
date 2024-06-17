package net.punchtree.thebackthrooms

import net.punchtree.thebackthrooms.BathroomTopDownPrinter.printBathroom
import kotlin.math.min

class BathroomBuilder {

    fun buildBathroom() {
        val width = (4..10).random()
        val maxSizeRatio = 2.25
        val length = (8..min((width * maxSizeRatio).toInt(), 15)).random()

        val bathroom = Bathroom(width, length)
        val allWallPositions = bathroom.calculateAllWallPositions()
//        val randomWallPosition = allWallPositions.random()

        printBathroom(bathroom)
        placeSink(bathroom)
    }

    private fun placeSink(bathroom: Bathroom) {

    }

}