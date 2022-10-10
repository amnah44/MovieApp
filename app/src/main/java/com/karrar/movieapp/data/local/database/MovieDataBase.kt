package com.karrar.movieapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.karrar.movieapp.data.local.database.daos.MovieDao
import com.karrar.movieapp.data.local.database.entity.WatchList
import com.karrar.movieapp.data.local.database.entity.SearchHistoryEntity
import com.karrar.movieapp.data.local.database.entity.WatchHistoryEntity

@Database(
    entities = [WatchList::class, SearchHistoryEntity::class, WatchHistoryEntity::class],
    version = 1
)
abstract class MovieDataBase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}