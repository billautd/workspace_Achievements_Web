package perso.project.standalone;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import perso.project.model.CompareData;
import perso.project.model.GameData;
import perso.project.model.Model;
import perso.project.model.PlayniteGameData;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractCompareService;

public abstract class AbstractStandaloneCompareService extends AbstractCompareService {
	@Inject
	Model model;

	protected abstract ConsoleSourceEnum getSource();

	protected abstract int getId();

	public List<CompareData> getCompareData() {
		final List<PlayniteGameData> saPlayniteData = model.getPlayniteGameDataForSources(List.of(getSource()));
		final List<GameData> saGameData = model.getGameDataForSources(List.of(getSource()));
		final List<CompareData> compareData = new ArrayList<>();

		Log.info("Comparing Playnite data for " + getSource());

		// PLAYNITE
		compareLocalToDatabase(saPlayniteData, saGameData, compareData);

		// DATABASE
		compareDatabaseToLocal(saPlayniteData, saGameData, compareData);
		return compareData;
	}

	private void compareLocalToDatabase(final List<PlayniteGameData> saPlayniteData, final List<GameData> saGameData,
			final List<CompareData> compareData) {
		saPlayniteData.forEach(playniteGame -> {
			final Optional<GameData> databaseGameFoundOpt = saGameData.stream()
					.filter(databaseGame -> databaseGame.getTitle().equals(playniteGame.getName())).findFirst();
			// Check if game in Playnite is in database
			if (databaseGameFoundOpt.isEmpty()) {
				compareData.add(getNotInDatabaseCompareData(playniteGame, getId()));
				return;
			}

			final GameData databaseGameFound = databaseGameFoundOpt.get();
			// Check if completion status are coherent between Playnite and database
			if (!databaseGameFound.getCompletionStatus().equals(playniteGame.getCompletionStatus())
					&& !playniteGame.getCompletionStatus().equals(CompletionStatusEnum.PLAYING)) {
				compareData.add(getCompletionStatusDifferentCompareData(playniteGame, databaseGameFound));
				return;
			}

			compareData.add(getOKCompareData(playniteGame, getId()));
		});
	}

	private void compareDatabaseToLocal(final List<PlayniteGameData> saPlayniteData, final List<GameData> saGameData,
			final List<CompareData> compareData) {
		saGameData.forEach(databaseGame -> {
			final Optional<PlayniteGameData> playniteGameFoundOpt = saPlayniteData.stream()
					.filter(playniteGame -> playniteGame.getName().equals(databaseGame.getTitle())).findFirst();
			// Check if game in database is in playnite
			if (playniteGameFoundOpt.isEmpty()) {
				compareData.add(getNotInLocalCompareData(databaseGame, getSource()));
				return;
			}
		});
	}
}
