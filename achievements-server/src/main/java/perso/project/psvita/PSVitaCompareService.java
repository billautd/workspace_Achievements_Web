package perso.project.psvita;

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
public class PSVitaCompareService extends AbstractCompareService {
	@Inject
	Model model;

	public List<CompareData> getCompareData() {
		final List<PlayniteGameData> psVitaPlayniteData = model
				.getPlayniteGameDataForSources(List.of(ConsoleSourceEnum.PSVITA));
		final List<GameData> psVitaGameData = model.getGameDataForSources(List.of(ConsoleSourceEnum.PSVITA));
		final List<CompareData> compareData = new ArrayList<>();

		Log.info("Comparing Playnite data for PSVita");

		// PLAYNITE
		compareLocalToDatabase(psVitaPlayniteData, psVitaGameData, compareData);

		// DATABASE
		compareDatabaseToLocal(psVitaPlayniteData, psVitaGameData, compareData);
		return compareData;
	}

	private void compareLocalToDatabase(final List<PlayniteGameData> psVitaPlayniteData,
			final List<GameData> psVitaGameData, final List<CompareData> compareData) {
		psVitaPlayniteData.forEach(playniteGame -> {
			final Optional<GameData> databaseGameFoundOpt = psVitaGameData.stream()
					.filter(databaseGame -> databaseGame.getTitle().equals(playniteGame.getName())).findFirst();
			// Check if game in Playnite is in database
			if (databaseGameFoundOpt.isEmpty()) {
				compareData.add(getNotInDatabaseCompareData(playniteGame, Model.PSVITA_CONSOLE_ID));
				return;
			}

			final GameData databaseGameFound = databaseGameFoundOpt.get();
			// Check if completion status are coherent between Playnite and database
			if (!databaseGameFound.getCompletionStatus().equals(playniteGame.getCompletionStatus())
					&& !playniteGame.getCompletionStatus().equals(CompletionStatusEnum.PLAYING)) {
				compareData.add(getCompletionStatusDifferentCompareData(playniteGame, databaseGameFound));
				return;
			}

			compareData.add(getOKCompareData(playniteGame, Model.PSVITA_CONSOLE_ID));
		});
	}

	private void compareDatabaseToLocal(final List<PlayniteGameData> psVitaPlayniteData,
			final List<GameData> psVitaGameData, final List<CompareData> compareData) {
		psVitaGameData.forEach(databaseGame -> {
			final Optional<PlayniteGameData> playniteGameFoundOpt = psVitaPlayniteData.stream()
					.filter(playniteGame -> playniteGame.getName().equals(databaseGame.getTitle())).findFirst();
			// Check if game in database is in playnite
			if (playniteGameFoundOpt.isEmpty()) {
				compareData.add(getNotInLocalCompareData(databaseGame, ConsoleSourceEnum.PSVITA));
				return;
			}
		});
	}
}
