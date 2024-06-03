package net.punchtree.thebackthrooms

data class Bathroom(val width: Int, val length: Int) {
    var cells : Array<Array<BathroomColumn>> = Array(width) {
        Array(length){
            BathroomColumn(Array(10){
                BathroomCell()
            })
        }
    }


    fun size() = width * length

}
