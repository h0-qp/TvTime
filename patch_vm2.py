with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'r') as f:
    content = f.read()

start_idx = content.find("val deferredEpisodes = tvShows.map { show ->")
end_idx = content.find("_uiState.value = TvShowsUiState.Success", start_idx)

new_fetch = """val deferredEpisodes = tvShows.map { show ->
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

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + new_fetch + content[end_idx:]
    with open('app/src/main/java/com/example/ui/screens/tvshows/TvShowsViewModel.kt', 'w') as f:
        f.write(content)
