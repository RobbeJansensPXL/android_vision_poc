package be.pxl.android_vision_poc.models

data class Beer(
    val auth_rating: Int,
    val beer_abv: Double,
    val beer_description: String,
    val beer_ibu: Int,
    val beer_label: String,
    val beer_name: String,
    val beer_slug: String,
    val beer_style: String,
    val bid: Int,
    val created_at: String,
    val in_production: Int,
    val wish_list: Boolean
)