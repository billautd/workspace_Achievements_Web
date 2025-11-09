package perso.project.steam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.CompareData;
import perso.project.model.GameData;
import perso.project.model.Model;
import perso.project.model.PlayniteGameData;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractCompareService;

@ApplicationScoped
public class SteamCompareService extends AbstractCompareService {
	@Inject
	Model model;

	@Override
	public List<CompareData> getCompareData() {
		final List<PlayniteGameData> steamPlayniteData = model
				.getPlayniteGameDataForSources(List.of(ConsoleSourceEnum.STEAM));
		final List<GameData> steamGameData = model.getGameDataForSources(List.of(ConsoleSourceEnum.STEAM));
		final List<CompareData> compareData = new ArrayList<>();

		Log.info("Comparing Playnite data for Steam");
		compareLocalToDatabase(steamPlayniteData, steamGameData, compareData);
		compareDatabaseToLocal(steamPlayniteData, steamGameData, compareData);

		return compareData;
	}

	private void compareLocalToDatabase(final List<PlayniteGameData> steamPlayniteData,
			final List<GameData> steamGameData, final List<CompareData> compareData) {
		steamPlayniteData.forEach(playniteGame -> {
			// Cannot play games always considered OK
			if (CompletionStatusEnum.CANNOT_PLAY.equals(playniteGame.getCompletionStatus())) {
				compareData.add(getOKCompareData(playniteGame, Model.STEAM_CONSOLE_ID));
				return;
			}
			final Optional<GameData> databaseGameFoundOpt = steamGameData.stream()
					.filter(databaseGame -> databaseGame.getTitle().equals(playniteGame.getName())).findFirst();
			// Game in Playnite but not in Datase
			if (databaseGameFoundOpt.isEmpty()) {
				compareData.add(getNotInDatabaseCompareData(playniteGame, Model.STEAM_CONSOLE_ID));
				return;
			}
			// Found game
			final GameData databaseGameFound = databaseGameFoundOpt.get();
			// Check if completion status are coherent between Playnite and database
			if (!databaseGameFound.getCompletionStatus().equals(playniteGame.getCompletionStatus())) {
				final boolean isPlaying = playniteGame.getCompletionStatus().equals(CompletionStatusEnum.PLAYING);
				final boolean isNoAchievementsButInterested = CompletionStatusEnum.NO_ACHIEVEMENTS
						.equals(databaseGameFound.getCompletionStatus())
						&& !CompletionStatusEnum.NO_ACHIEVEMENTS.equals(playniteGame.getCompletionStatus());
				final boolean isCannotPlay = CompletionStatusEnum.CANNOT_PLAY
						.equals(databaseGameFound.getCompletionStatus());

				if (!isPlaying && !isNoAchievementsButInterested && !isCannotPlay) {
					compareData.add(getCompletionStatusDifferentCompareData(playniteGame, databaseGameFound));
					return;
				}
			}
			compareData.add(getOKCompareData(playniteGame, Model.STEAM_CONSOLE_ID));
		});
	}

	private void compareDatabaseToLocal(final List<PlayniteGameData> steamPlayniteData,
			final List<GameData> steamGameData, final List<CompareData> compareData) {
		steamGameData.forEach(databaseGame -> {
			final Optional<PlayniteGameData> playniteGameFoundOpt = steamPlayniteData.stream()
					.filter(playniteGame -> playniteGame.getName().equals(databaseGame.getTitle())).findFirst();
			// Check if game in database is in playnite
			if (playniteGameFoundOpt.isEmpty()) {
				compareData.add(getNotInLocalCompareData(databaseGame, ConsoleSourceEnum.STEAM));
			}
		});
	}
}
