package com.malkinfo.puzzle

class Tile(
    private var number:Int? = null
) {
    fun number():Int{
        return number!!
    }
}