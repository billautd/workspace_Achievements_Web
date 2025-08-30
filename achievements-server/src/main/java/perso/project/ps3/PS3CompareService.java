package perso.project.ps3;

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
public class PS3CompareService extends AbstractCompareService {
	@Inject
	Model model;

	public List<CompareData> getCompareData() {
		final List<PlayniteGameData> ps3PlayniteData = model
				.getPlayniteGameDataForSources(List.of(ConsoleSourceEnum.PS3));
		final List<GameData> ps3GameData = model.getGameDataForSources(List.of(ConsoleSourceEnum.PS3));
		final List<CompareData> compareData = new ArrayList<>();

		Log.info("Comparing Playnite data for PS3");

		// PLAYNITE
		compareLocalToDatabase(ps3PlayniteData, ps3GameData, compareData);

		// DATABASE
		compareDatabaseToLocal(ps3PlayniteData, ps3GameData, compareData);
		return compareData;
	}

	private void compareLocalToDatabase(final List<PlayniteGameData> ps3PlayniteData, final List<GameData> ps3GameData,
			final List<CompareData> compareData) {
		ps3PlayniteData.forEach(playniteGame -> {
			final Optional<GameData> databaseGameFoundOpt = ps3GameData.stream()
					.filter(databaseGame -> databaseGame.getTitle().equals(playniteGame.getName())).findFirst();
			// Check if game in Playnite is in database
			if (databaseGameFoundOpt.isEmpty()) {
				compareData.add(getNotInDatabaseCompareData(playniteGame, Model.PS3_CONSOLE_ID));
				return;
			}

			final GameData databaseGameFound = databaseGameFoundOpt.get();
			// Check if completion status are coherent between Playnite and database
			if (!databaseGameFound.getCompletionStatus().equals(playniteGame.getCompletionStatus())
					&& !playniteGame.getCompletionStatus().equals(CompletionStatusEnum.PLAYING)) {
				compareData.add(getCompletionStatusDifferentCompareData(playniteGame, databaseGameFound));
				return;
			}

			compareData.add(getOKCompareData(playniteGame, Model.PS3_CONSOLE_ID));
		});
	}

	private void compareDatabaseToLocal(final List<PlayniteGameData> ps3PlayniteData, final List<GameData> ps3GameData,
			final List<CompareData> compareData) {
		ps3GameData.forEach(databaseGame -> {
			final Optional<PlayniteGameData> playniteGameFoundOpt = ps3PlayniteData.stream()
					.filter(playniteGame -> playniteGame.getName().equals(databaseGame.getTitle())).findFirst();
			// Check if game in database is in playnite
			if (playniteGameFoundOpt.isEmpty()) {
				compareData.add(getNotInLocalCompareData(databaseGame, ConsoleSourceEnum.PS3));
				return;
			}
		});
	}
}
