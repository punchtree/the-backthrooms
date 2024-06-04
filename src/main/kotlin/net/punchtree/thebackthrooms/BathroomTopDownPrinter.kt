package net.punchtree.thebackthrooms

object BathroomTopDownPrinter {

    fun printBathroom(bathroom: Bathroom) {
        // print bathroom.cells
        for ((rowIndex, cellRow) in bathroom.cells.withIndex()) {
            print("%2d ".format(rowIndex))
            for (cell in cellRow) {
                val char = when {
                    cell.isWall -> "□"
                    else -> cell.wallPositions.size
//                    else -> "·"
                }
                print("$char ")
            }
            println()
        }
        print("   ")
        for (colIndex in 0 until bathroom.cells[0].size) {
            print("${('A' + colIndex)} ")
        }
        println()
    }

}
