package net.punchtree.thebackthrooms

import net.punchtree.thebackthrooms.BathroomTopDownPrinter.printBathroom

class BathroomBuilder {

    fun buildBathroom() {
        val width = (4..10).random()
        val length = (8..15).random()

        val bathroom = Bathroom(width, length)
        val allWallPositions = bathroom.calculateAllWallPositions()
//        val randomWallPosition = allWallPositions.random()

        printBathroom(bathroom)
        placeSink(bathroom)
    }

    private fun placeSink(bathroom: Bathroom) {

    }

}