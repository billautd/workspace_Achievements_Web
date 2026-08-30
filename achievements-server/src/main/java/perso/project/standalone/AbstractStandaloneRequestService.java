package perso.project.standalone;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.core.type.TypeReference;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractSingleIdRequestService;

public abstract class AbstractStandaloneRequestService extends AbstractSingleIdRequestService {
	static final String HTM_EXTENSION = "htm";
	protected int id = 1;

	@Inject
	@ConfigProperty(name = "standalone.games.by.ids.path")
	private Path standaloneGamesByIdsPath;

	protected abstract Path getHTMLPath();

	protected abstract void parseDocument(final File htmlFile, final List<GameData> gameData, final Document document);

	protected abstract void parseAchievements(final List<GameData> gameData);

	public List<GameData> getAllData() {
		Log.info("Getting all " + getSource() + " games");
		getGameDataFromHTML(getHTMLPath());
		getLocalData();
		final List<GameData> gameData = model.getConsoleDataMap().get(getId()).getGameDataMap().values().stream()
				.toList();
		Log.info("Processing " + gameData.size() + " " + getSource() + " games");
		parseAchievements(gameData);
		return gameData;
	}

	public List<GameData> getGameDataFromHTML(final Path pathToFolder) {
		id = 1;
		final List<GameData> gameData = new ArrayList<>();
		for (final File htmlFile : pathToFolder.toFile().listFiles()) {
			if (!FilenameUtils.getExtension(htmlFile.getName()).equals(HTM_EXTENSION)) {
				continue;
			}
			Document doc;
			try {
				Log.info("Reading " + htmlFile.getName());
				doc = Jsoup.parse(htmlFile);
				parseDocument(htmlFile, gameData, doc);
			} catch (IOException e) {
				Log.error("Cannot parse htmlFile " + htmlFile.getName());
				continue;
			}
		}
		return gameData;
	}

	@Override
	public List<ConsoleData> getConsoleIds() {
		ConsoleData saConsoleData;
		if (!model.getConsoleDataMap().containsKey(getId())) {
			Log.info("Getting " + getSource() + " console data");
			saConsoleData = new ConsoleData();
			saConsoleData.setActive(true);
			saConsoleData.setGameSystem(true);
			saConsoleData.setId(getId());
			saConsoleData.setName(getSource().getName());
			saConsoleData.setSource(getSource());
			model.getConsoleDataMap().put(saConsoleData.getId(), saConsoleData);
		} else {
			saConsoleData = model.getConsoleDataMap().get(getId());
		}
		return List.of(saConsoleData);
	}

	public GameData parseCompletionStatus(final GameData gameData) {
		if (gameData.getCompletionStatus() == null) {
			Log.error("Game " + gameData.getTitle() + " has null completion status");
			return gameData;
		}
		// If beaten or mastered, already set by other methods
		if (!gameData.isLocalData()) {
			if (gameData.getTotalAchievements() == 0) {
				gameData.setCompletionStatus(CompletionStatusEnum.NO_ACHIEVEMENTS);
			} else if (gameData.getAwardedAchievements() == gameData.getTotalAchievements()) {
				gameData.setCompletionStatus(CompletionStatusEnum.MASTERED);
			} else if (gameData.getAwardedAchievements() > 0) {
				gameData.setCompletionStatus(CompletionStatusEnum.TRIED);
			}
		}
		setGameAchievementPercent(gameData);
		return gameData;
	}

	protected void readStandaloneGamesByIds() {
		try {
			final Map<ConsoleSourceEnum, Map<String, String>> values = mapper.readValue(
					standaloneGamesByIdsPath.toFile(),
					new TypeReference<Map<ConsoleSourceEnum, Map<String, String>>>() {
					});
			model.getStandaloneGamesByIds().clear();
			values.forEach(model.getStandaloneGamesByIds()::put);
		} catch (final IOException e) {
			Log.error("Cannot read file at " + standaloneGamesByIdsPath, e);
		}
	}
}
