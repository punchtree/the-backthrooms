package net.punchtree.thebackthrooms

class BathroomBuilder {

    fun buildBathroom() {
        val width = (4..10).random()
        val length = (8..15).random()

        val bathroom = Bathroom(width, length)
        placeSink(bathroom)
    }
    private fun placeSink(bathroom: Bathroom) {
        val perimeter = width*2 + length*2 - 4

    }

}