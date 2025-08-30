package perso.project.utils;

import io.quarkus.logging.Log;
import perso.project.model.CompareData;
import perso.project.model.GameData;
import perso.project.model.PlayniteGameData;
import perso.project.model.enums.CompareDataStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;

public abstract class AbstractCompareService {
	protected CompareData getOKCompareData(final PlayniteGameData playniteGame, final int consoleId) {
		Log.debug(playniteGame.getName() + " for " + playniteGame.getPlatform() + " => OK");
		return new CompareData(playniteGame.getPlatform(), consoleId, playniteGame.getName(), playniteGame.getSource(),
				CompareDataStatusEnum.OK);
	}

	protected CompareData getNotInDatabaseCompareData(final PlayniteGameData playniteGame, final int consoleId) {
		Log.debug(playniteGame.getName() + " for " + playniteGame.getPlatform() + " => In Playnite but not in "
				+ playniteGame.getSource());
		return new CompareData(playniteGame.getPlatform(), consoleId, playniteGame.getName(), playniteGame.getSource(),
				CompareDataStatusEnum.NOT_IN_DATABASE);
	}

	protected CompareData getNotInLocalCompareData(final GameData databaseGame, final ConsoleSourceEnum source) {
		Log.debug(databaseGame.getTitle() + " for " + databaseGame.getConsoleName() + " => In " + source
				+ " but not in Playnite");
		return new CompareData(databaseGame.getConsoleName(), databaseGame.getConsoleId(), databaseGame.getTitle(),
				source, CompareDataStatusEnum.NOT_IN_LOCAL);
	}

	protected CompareData getCompletionStatusDifferentCompareData(final PlayniteGameData playniteGame,
			final GameData databaseGame) {
		Log.debug(playniteGame.getName() + " for " + playniteGame.getPlatform() + " => "
				+ playniteGame.getCompletionStatus() + " in Playnite but " + databaseGame.getCompletionStatus() + " in "
				+ playniteGame.getSource());
		return new CompareData(playniteGame.getPlatform(), databaseGame.getConsoleId(), playniteGame.getName(),
				playniteGame.getSource(), CompareDataStatusEnum.COMPLETION_STATUS_DIFFERENT,
				playniteGame.getCompletionStatus(), databaseGame.getCompletionStatus());
	}
}
