package perso.project.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import io.quarkus.logging.Log;
import perso.project.model.GameData;
import perso.project.model.LocalGameData;
import perso.project.model.enums.CompletionStatusEnum;

public abstract class AbstractSingleIdRequestService extends AbstractRequestService {
	protected abstract int getId();

	protected abstract Path getLocalDataPath();

	protected abstract GameData parseAchievementData(final GameData gameData);

	public Map<Integer, LocalGameData> getLocalData() {
		Log.info("Reading " + getLocalDataPath());
		final List<LocalGameData> localData = new ArrayList<>();
		try (final FileInputStream fis = new FileInputStream(getLocalDataPath().toFile())) {
			final Map<Integer, LocalGameData> data = mapper.readValue(fis,
					new TypeReference<Map<Integer, LocalGameData>>() {
					});
			data.entrySet().forEach(e -> {
				final int gameId = e.getKey();
				GameData gameData = model.getConsoleDataMap().get(getId()).getGameDataMap().get(gameId);
				if (gameData == null) {
					gameData = new GameData();
					gameData.setTitle(e.getValue().getName());
					gameData.setId(gameId);
					gameData.setConsoleId(getId());
					gameData.setConsoleName(getSource().getName());
				}
				gameData.setLocalData(true);
				gameData.setCompletionStatus(e.getValue().getStatus());
				parseAchievementData(gameData);

				model.getConsoleDataMap().get(getId()).getGameDataMap().put(gameId, gameData);
				localData.add(e.getValue());
			});
			return data;
		} catch (IOException e) {
			Log.error("Error reading local data", e);
			return null;
		}
	}

	protected void addToLocalDatabase(final int gameId, final CompletionStatusEnum status) {
		Log.info("Add " + getSource() + " game " + gameId + " as " + status);

		final Map<Integer, LocalGameData> localData = new TreeMap<Integer, LocalGameData>(getLocalData());
		GameData gameData = model.getConsoleDataMap().get(getId()).getGameDataMap().get(gameId);
		localData.put(gameId, new LocalGameData(gameData.getTitle(), status));

		try {
			mapper.writer(new DefaultPrettyPrinter()).writeValue(getLocalDataPath().toFile(), localData);
		} catch (IOException e) {
			Log.error("Cannot write to " + getLocalDataPath());
		}
	}

	public void setGameAsBeaten(final int gameId) {
		addToLocalDatabase(gameId, CompletionStatusEnum.BEATEN);
	}

	public void setGameAsMastered(final int gameId) {
		addToLocalDatabase(gameId, CompletionStatusEnum.MASTERED);
	}
}
