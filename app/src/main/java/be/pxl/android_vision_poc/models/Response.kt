package be.pxl.android_vision_poc.models

data class Response(
    val beers: Beers,
    val breweries: Breweries,
    val brewery_id: Int,
    val found: Int,
    val homebrew: Homebrew,
    val limit: Int,
    val message: String,
    val offset: Int,
    val parsed_term: String,
    val search_type: String,
    val search_version: Int,
    val term: String,
    val time_taken: Double,
    val type_id: Int
)