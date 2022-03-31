package be.pxl.android_vision_poc.models

data class Item(
    val beer: Beer,
    val brewery: Brewery,
    val checkin_count: Int,
    val have_had: Boolean,
    val your_count: Int
)