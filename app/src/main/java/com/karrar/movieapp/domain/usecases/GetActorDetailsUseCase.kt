package com.karrar.movieapp.domain.usecases

import com.karrar.movieapp.data.repository.MovieRepository
import com.karrar.movieapp.domain.mappers.MovieMappersContainer
import com.karrar.movieapp.domain.models.ActorDetails
import javax.inject.Inject

class GetActorDetailsUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
    private val movieMappersContainer: MovieMappersContainer,
) {
    suspend operator fun invoke(actorId: Int): ActorDetails {
        return movieMappersContainer.actorDetailsMapper.map(movieRepository.getActorDetails(actorId))
    }
}
