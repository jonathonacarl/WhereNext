package hu.ait.wherenext.data

import com.google.android.gms.maps.model.LatLng

data class PinPost(
    var uid: String = "",
    var author: String = "",
    var title: String = "",
    var body: String = "",
    var imgUrl: String = "",
    var location: LatLng
)

data class PinPostWithID(
    val pinPostID: String,
    val pinPost: PinPost
)