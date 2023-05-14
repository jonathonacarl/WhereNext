package hu.ait.wherenext.data

data class PinPost(
    var uid: String = "",
    var author: String = "",
    var title: String = "",
    var body: String = "",
    var imgUrl: String = "",
    var location: LatLng = LatLng(0.0, 0.0),
    var address: String = ""
)

data class PinPostWithID(
    val pinPostID: String,
    val pinPost: PinPost
)