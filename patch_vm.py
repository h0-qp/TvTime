with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'r') as f:
    content = f.read()

# 1. Update MediaRepository import if necessary, but we'll use firestoreRepository to toggle watched.
# Add toggle function
toggle_func = """
    fun toggleEpisodeWatched(showId: Int, episodeKey: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TvShowsUiState.Success) {
                val show = currentState.watchlist.find { it.id == showId } ?: return@launch
                val newWatched = show.watchedEpisodes.toMutableList()
                if (newWatched.contains(episodeKey)) {
                    newWatched.remove(episodeKey)
                } else {
                    newWatched.add(episodeKey)
                }
                firestoreRepository.addToWatchlist(show.copy(watchedEpisodes = newWatched))
            }
        }
    }
"""
if "fun toggleEpisodeWatched" not in content:
    content = content.replace("private fun fetchData() {", toggle_func + "\n    private fun fetchData() {")

# 2. Update episode fetching
new_fetch = """
                // Fetch details for each TV show in watchlist
                val deferredEpisodes = tvShows.map { show ->
                    async {
                        val detailsResult = repository.getMediaDetails(apiKey, show.id, "tv")
                        val episodesList = mutableListOf<UpcomingEpisodeData>()
                        detailsResult.onSuccess { details ->
                            // Get episodes from the most recent season
                            val latestSeasonNum = details.last_episode_to_air?.season_number ?: details.next_episode_to_air?.season_number
                            if (latestSeasonNum != null) {
                                val seasonResult = repository.getSeasonDetails(show.id, latestSeasonNum, apiKey)
                                seasonResult.onSuccess { seasonData ->
                                    seasonData.episodes.forEach { ep ->
                                        if (ep.air_date != null) {
                                            val airDate = try { LocalDate.parse(ep.air_date) } catch (e: Exception) { null }
                                            if (airDate != null) {
                                                val diff = ChronoUnit.DAYS.between(today, airDate)
                                                // Show episodes from the last 30 days and upcoming 14 days
                                                if (diff in -30..14) {
                                                    val epToAir = EpisodeToAir(ep.id, ep.name, ep.overview ?: "", ep.air_date, ep.episode_number, ep.season_number, ep.still_path)
                                                    episodesList.add(UpcomingEpisodeData(show, details, epToAir, diff))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        episodesList
                    }
                }
                
                val upcomingEpisodes = deferredEpisodes.awaitAll().flatten().sortedByDescending { it.episodeToAir.air_date }
"""

import re
content = re.sub(r"val deferredEpisodes = tvShows\.map \{ show ->.*?val upcomingEpisodes = deferredEpisodes\.awaitAll\(\)\[^\]]+", new_fetch, content, flags=re.DOTALL)
# wait, the regex might fail. I'll use a safer replacement.
