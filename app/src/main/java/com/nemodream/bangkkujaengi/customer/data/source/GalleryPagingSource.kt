package com.nemodream.bangkkujaengi.customer.data.paging

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import javax.inject.Inject

class GalleryPagingSource @Inject constructor(
    private val contentResolver: ContentResolver
) : PagingSource<Int, Uri>() {

    override fun getRefreshKey(state: PagingState<Int, Uri>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Uri> {
        return try {
            val pageNumber = params.key ?: 0
            val galleryImages = mutableListOf<Uri>()
            val offset = pageNumber * params.loadSize

            val projection = arrayOf(MediaStore.Images.Media._ID)
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                cursor.moveToPosition(offset)
                var count = 0
                while (cursor.moveToNext() && count < params.loadSize) {
                    val id = cursor.getLong(0)
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    galleryImages.add(contentUri)
                    count++
                }
            }

            LoadResult.Page(
                data = galleryImages,
                prevKey = if (pageNumber == 0) null else pageNumber - 1,
                nextKey = if (galleryImages.isEmpty()) null else pageNumber + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}