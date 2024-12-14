package com.littlelemon.storyapp

import com.littlelemon.storyapp.data.response.ListStory

object DataDummyTest {
    fun generateDummyQuoteResponse(): List<ListStory> {
        val items: MutableList<ListStory> = arrayListOf()
        for (i in 0..20) {
            val story = ListStory(
                id = i.toString(),
                name = "Story App $i",
                description = "StoryDescription $i",
                createdAt = "2024-12-$i",
                photoUrl = "https://pbs.twimg.com/profile_images/1544955480816369664/tbdn5Cc8_400x400.jpg",
                lat = 3.5952,
                lon = 98.6722,
            )
            items.add(story)
        }
        return items
    }
}