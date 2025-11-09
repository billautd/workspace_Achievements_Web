package perso.project.ra;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.CompareData;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.Model;
import perso.project.model.PlayniteGameData;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractCompareService;

@ApplicationScoped
public class RetroAchievementsCompareService extends AbstractCompareService {
	@Inject
	Model model;

	@Override
	public List<CompareData> getCompareData() {
		final List<PlayniteGameData> raPlayniteData = model
				.getPlayniteGameDataForSources(List.of(ConsoleSourceEnum.RETRO_ACHIEVEMENTS));
		final List<GameData> raGameData = model.getGameDataForSources(List.of(ConsoleSourceEnum.RETRO_ACHIEVEMENTS));
		final List<CompareData> compareData = new ArrayList<>();

		Log.info("Comparing Playnite data for RetroAchievements");
		// PLAYNITE
		compareLocalToDatabase(raPlayniteData, raGameData, compareData);

		// DATABASE
		compareDatabaseToLocal(raPlayniteData, raGameData, compareData);

		return compareData;
	}

	private void compareLocalToDatabase(final List<PlayniteGameData> raPlayniteData, final List<GameData> raGameData,
			final List<CompareData> compareData) {
		raPlayniteData.forEach(playniteGame -> {
			final Optional<GameData> databaseGameFoundOpt = raGameData.stream()
					.filter(databaseGame -> databaseGame.getTitle().equals(playniteGame.getName())
							&& databaseGame.getConsoleName().equals(playniteGame.getPlatform()))
					.findFirst();
			// Check if game in Playnite is in database
			if (databaseGameFoundOpt.isEmpty()) {
				// Check if console data exists
				final Optional<ConsoleData> consoleIdOpt = model.getConsoleDataMap().values().stream()
						.filter(c -> c.getName().equals(playniteGame.getPlatform())).findFirst();
				if (consoleIdOpt.isEmpty()) {
					Log.error("No console data found for playnite game " + playniteGame.getName() + " with platform "
							+ playniteGame.getPlatform());
					return;
				}
				compareData.add(getNotInDatabaseCompareData(playniteGame, consoleIdOpt.get().getId()));
				return;
			}
			final GameData databaseGameFound = databaseGameFoundOpt.get();
			// Check if completion status are coherent between Playnite and database
			if (!databaseGameFound.getCompletionStatus().equals(playniteGame.getCompletionStatus())
					&& !playniteGame.getCompletionStatus().equals(CompletionStatusEnum.PLAYING)) {
				compareData.add(getCompletionStatusDifferentCompareData(playniteGame, databaseGameFound));
				return;
			}
			compareData.add(getOKCompareData(playniteGame, databaseGameFound.getConsoleId()));
		});
	}

	private void compareDatabaseToLocal(final List<PlayniteGameData> raPlayniteData, final List<GameData> raGameData,
			final List<CompareData> compareData) {
		raGameData.forEach(databaseGame -> {
			final Optional<PlayniteGameData> playniteGameFoundOpt = raPlayniteData.stream()
					.filter(playniteGame -> playniteGame.getName().equals(databaseGame.getTitle())
							&& playniteGame.getPlatform().equals(databaseGame.getConsoleName()))
					.findFirst();
			// Check if game in database is in playnite
			if (playniteGameFoundOpt.isEmpty()) {
				compareData.add(getNotInLocalCompareData(databaseGame, ConsoleSourceEnum.RETRO_ACHIEVEMENTS));
				return;
			}
		});
	}
}
