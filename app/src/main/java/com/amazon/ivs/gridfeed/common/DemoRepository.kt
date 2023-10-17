package com.amazon.ivs.gridfeed.common

import com.amazon.ivs.gridfeed.repository.models.GridFeedItemModel

const val LOAD_MORE_DELTA = 5 // Row count till end
const val LOAD_MORE_COUNT = 10 // Demo items (Groups of 15) to add

val demoItems get() = listOf(
    GridFeedItemModel(
        id = 0,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/animals_square/bear.png"
    ),
    GridFeedItemModel(
        id = 1,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/animals_square/bird.png"
    ),
    GridFeedItemModel(
        id = 2,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/animals_square/bird2.png"
    ),
    GridFeedItemModel(
        id = 3,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/animals_square/giraffe.png"
    ),
    GridFeedItemModel(
        id = 4,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/social-ugc-demo/thumb-1.png",
        videoUrl = "https://4c62a87c1810.us-west-2.playback.live-video.net/api/video/v1/us-west-2.049054135175.channel.mO0Brcl9xRBd.m3u8"
    ),
    GridFeedItemModel(
        id = 5,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/social-ugc-demo/thumb-2.png",
        videoUrl = "https://4c62a87c1810.us-west-2.playback.live-video.net/api/video/v1/us-west-2.049054135175.channel.LdpM3A5sXF52.m3u8"
    ),
    GridFeedItemModel(
        id = 6,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/animals_square/hedgehog.png"
    ),
    GridFeedItemModel(
        id = 7,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/animals_square/hippo.png"
    ),
    GridFeedItemModel(
        id = 8,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/social-ugc-demo/thumb-3.png",
        videoUrl = "https://4c62a87c1810.us-west-2.playback.live-video.net/api/video/v1/us-west-2.049054135175.channel.ELpWxGZeDdqg.m3u8"
    ),
    GridFeedItemModel(
        id = 9,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/social-ugc-demo/thumb-4.png",
        videoUrl = "https://4c62a87c1810.us-west-2.playback.live-video.net/api/video/v1/us-west-2.049054135175.channel.88HW3bSgcjE1.m3u8"
    ),
    GridFeedItemModel(
        id = 10,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/social-ugc-demo/thumb-5.png",
        videoUrl = "https://4c62a87c1810.us-west-2.playback.live-video.net/api/video/v1/us-west-2.049054135175.channel.F59L5JYmQCdA.m3u8"
    ),
    GridFeedItemModel(
        id = 11,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/social-ugc-demo/thumb-6.png",
        videoUrl = "https://4c62a87c1810.us-west-2.playback.live-video.net/api/video/v1/us-west-2.049054135175.channel.sPmSurUL2ovR.m3u8"
    ),
    GridFeedItemModel(
        id = 12,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/animals_square/bird2.png"
    ),
    GridFeedItemModel(
        id = 13,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/social-ugc-demo/thumb-4.png",
        videoUrl = "https://4c62a87c1810.us-west-2.playback.live-video.net/api/video/v1/us-west-2.049054135175.channel.88HW3bSgcjE1.m3u8"
    ),
    GridFeedItemModel(
        id = 14,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/animals_square/hedgehog.png"
    ),
    GridFeedItemModel(
        id = 15,
        imageUrl = "https://d39ii5l128t5ul.cloudfront.net/assets/animals_square/hippo.png"
    ),
)

fun List<GridFeedItemModel>.loadMore(): List<GridFeedItemModel> {
    val newList = toMutableList()
    for (row in (0 .. LOAD_MORE_COUNT)) {
        newList.addAll(demoItems)
    }
    return newList
}
