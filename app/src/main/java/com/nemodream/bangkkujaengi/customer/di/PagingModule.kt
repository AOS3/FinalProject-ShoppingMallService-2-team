package com.nemodream.bangkkujaengi.customer.di

import android.content.ContentResolver
import android.content.Context
import com.nemodream.bangkkujaengi.customer.data.paging.GalleryPagingSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PagingModule {

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideGalleryPagingSource(contentResolver: ContentResolver): GalleryPagingSource {
        return GalleryPagingSource(contentResolver)
    }
}