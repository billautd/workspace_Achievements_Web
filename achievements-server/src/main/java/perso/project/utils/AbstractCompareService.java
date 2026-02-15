package perso.project.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import perso.project.model.CompareData;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.Model;
import perso.project.model.PlayniteGameData;
import perso.project.model.enums.CompareDataStatusEnum;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;

public abstract class AbstractCompareService {
	@Inject
	Model model;

	protected abstract ConsoleSourceEnum getSource();

	protected int getId() {
		return -1;
	}

	public List<CompareData> getCompareData() {
		final List<PlayniteGameData> playniteData = model.getPlayniteGameDataForSources(List.of(getSource()));
		final List<GameData> gameData = model.getGameDataForSources(List.of(getSource()));
		final List<CompareData> compareData = new ArrayList<>();

		Log.info("Comparing Playnite data for " + getSource());
		// PLAYNITE
		compareLocalToDatabase(playniteData, gameData, compareData);

		// DATABASE
		compareDatabaseToLocal(playniteData, gameData, compareData);

		return compareData;
	}

	private void compareLocalToDatabase(final List<PlayniteGameData> playniteData, final List<GameData> gameData,
			final List<CompareData> compareData) {
		playniteData.forEach(playniteGame -> {
			// Check if console data exists
			final Optional<ConsoleData> consoleIdOpt = model.getConsoleDataMap().values().stream()
					.filter(c -> c.getName().equals(playniteGame.getPlatform()) || c.getId() == getId()).findFirst();
			if (consoleIdOpt.isEmpty()) {
				Log.error("No console data found for playnite game " + playniteGame.getName() + " with platform "
						+ playniteGame.getPlatform());
				return;
			}
			// Cannot play games always considered OK
			if (CompletionStatusEnum.CANNOT_PLAY.equals(playniteGame.getCompletionStatus())) {
				compareData.add(getOKCompareData(playniteGame, Model.STEAM_CONSOLE_ID));
				return;
			}

			final Optional<GameData> databaseGameFoundOpt = gameData.stream()
					.filter(databaseGame -> databaseGame.getTitle().equals(playniteGame.getName())
							&& (databaseGame.getConsoleName().equals(playniteGame.getPlatform())
									|| databaseGame.getConsoleId() == getId()))
					.findFirst();
			// Check if game in Playnite is in database
			if (databaseGameFoundOpt.isEmpty()) {
				compareData.add(getNotInDatabaseCompareData(playniteGame, consoleIdOpt.get().getId()));
				return;
			}
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
			compareData.add(getOKCompareData(playniteGame, databaseGameFound.getConsoleId()));
		});
	}

	private void compareDatabaseToLocal(final List<PlayniteGameData> playniteData, final List<GameData> gameData,
			final List<CompareData> compareData) {
		gameData.forEach(databaseGame -> {
			final Optional<PlayniteGameData> playniteGameFoundOpt = playniteData.stream()
					.filter(playniteGame -> playniteGame.getName().equals(databaseGame.getTitle())
							&& (playniteGame.getPlatform().equals(databaseGame.getConsoleName())
									|| databaseGame.getConsoleId() == getId()))
					.findFirst();
			// Check if game in database is in playnite
			if (playniteGameFoundOpt.isEmpty()) {
				compareData.add(getNotInLocalCompareData(databaseGame, ConsoleSourceEnum.RETRO_ACHIEVEMENTS));
				return;
			}
		});
	}

	protected CompareData getOKCompareData(final PlayniteGameData playniteGame, final int consoleId) {
		Log.debug(playniteGame.getName() + " for " + playniteGame.getPlatform() + " => OK");
		return new CompareData(playniteGame.getPlatform(), consoleId, playniteGame.getName(), playniteGame.getSource(),
				CompareDataStatusEnum.OK);
	}

	protected CompareData getNotInDatabaseCompareData(final PlayniteGameData playniteGame, final int consoleId) {
		Log.error(playniteGame.getName() + " for " + playniteGame.getPlatform() + " => In Playnite but not in "
				+ playniteGame.getSource());
		return new CompareData(playniteGame.getPlatform(), consoleId, playniteGame.getName(), playniteGame.getSource(),
				CompareDataStatusEnum.NOT_IN_DATABASE);
	}

	protected CompareData getNotInLocalCompareData(final GameData databaseGame, final ConsoleSourceEnum source) {
		Log.error(databaseGame.getTitle() + " for " + databaseGame.getConsoleName() + " => In " + source
				+ " but not in Playnite");
		return new CompareData(databaseGame.getConsoleName(), databaseGame.getConsoleId(), databaseGame.getTitle(),
				source, CompareDataStatusEnum.NOT_IN_LOCAL);
	}

	protected CompareData getCompletionStatusDifferentCompareData(final PlayniteGameData playniteGame,
			final GameData databaseGame) {
		Log.error(playniteGame.getName() + " for " + playniteGame.getPlatform() + " => "
				+ playniteGame.getCompletionStatus() + " in Playnite but " + databaseGame.getCompletionStatus() + " in "
				+ playniteGame.getSource());
		return new CompareData(playniteGame.getPlatform(), databaseGame.getConsoleId(), playniteGame.getName(),
				playniteGame.getSource(), CompareDataStatusEnum.COMPLETION_STATUS_DIFFERENT,
				playniteGame.getCompletionStatus(), databaseGame.getCompletionStatus());
	}
}
