package be.pxl.android_vision_poc.models

data class Brewery(
    val brewery_active: Int,
    val brewery_id: Int,
    val brewery_label: String,
    val brewery_name: String,
    val brewery_page_url: String,
    val brewery_slug: String,
    val brewery_type: String,
    val contact: Contact,
    val country_name: String,
    val location: Location
)