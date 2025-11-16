package perso.project.ra;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.AchievementData;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractRequestService;
import perso.project.utils.LoggingUtils;
import perso.project.utils.SleepUtils;

@ApplicationScoped
public class RetroAchievementsRequestService extends AbstractRequestService {
	static final String RA_API_KEY_KEY = "RA_API_KEY";

	static final String MAIN_URI = "https://retroachievements.org/API";
	static final String MEDIA_URL = "https://media.retroachievements.org";

	static final String CONSOLE_IDS_METHOD = "GetConsoleIDs";
	static final String CONSOLE_GAMES_METHOD = "GetGameList";
	static final String USER_COMPLETION_PROGRESS_METHOD = "GetUserCompletionProgress";
	static final String GAME_INFO_PROGRESS_METHOD = "GetGameInfoAndUserProgress";

	static final String MASTERY_COMPLETION_STRING = "mastered";
	static final String GAME_BEATEN_STRING = "beaten-hardcore";

	static final String BADGE_URL = MEDIA_URL + "/Badge/";
	static final String BADGE_LOCKED = "_lock.png";
	static final String BADGE_UNLOCKED = ".png";

	static final String BOXART_URL = MEDIA_URL;

	@Inject
	@ConfigProperty(name = "ra.username")
	String raUsername;

	/**
	 * Creates <b>blocking</b> HTTP request
	 * 
	 * @param method
	 * @param params
	 * @return
	 */
	HttpResponse<String> requestData(final String method, final String... params) {
		final String raApiKey = System.getenv(RA_API_KEY_KEY);
		if (raApiKey == null) {
			Log.error("RA API Key not defined as environment variable");
			return null;
		}
		try {
			final StringBuilder uriString = new StringBuilder();
			uriString.append(MAIN_URI).append("/").append("API_").append(method).append(".php").append("?").append("u=")
					.append(raUsername).append("&").append("y=").append(raApiKey);
			for (final String param : params) {
				uriString.append("&").append(param);
			}
			final URI uri = new URI(uriString.toString());
			return requestHttpURI(uri);
		} catch (final URISyntaxException e) {
			Log.error("Error creating URI", e);
			return null;
		}
	}

	@Override
	public List<ConsoleData> getConsoleIds() {
		Log.info("Getting console ids");
		final String resBody = requestData(CONSOLE_IDS_METHOD, "a=1", "g=1").body();
		LoggingUtils.prettyPrint(mapper, resBody);
		try {
			final List<ConsoleData> consoleData = mapper.readValue(resBody, new TypeReference<List<ConsoleData>>() {
			});
			Log.info("Found " + consoleData.size() + " consoles");
			// Add console data to map only if it is a running game system
			consoleData.forEach(data -> {
				data.setSource(ConsoleSourceEnum.RETRO_ACHIEVEMENTS);
				Log.debug("Setting console data for " + data.getName() + " (" + data.getId() + ")");

				if (!model.getConsoleDataMap().containsKey(data.getId())) {
					model.getConsoleDataMap().put(data.getId(), data);
				}
			});
			Log.info("Console data map is size " + model.getConsoleDataMap().size());
			return consoleData;
		} catch (IOException e) {
			Log.error("Error reading response body as ConsoleData", e);
			return null;
		}
	}

	public List<GameData> getUserCompletionProgress() {
		Log.info("Getting user completion progress");
		final List<GameData> gameData = new ArrayList<>();
		// Request takes at most 500 games at once. We loop to get all of them
		requestCompletionProgressLoop(0, gameData);

		Log.info("Found " + gameData.size() + " played games");
		gameData.forEach(data -> {
			final ConsoleData consoleData = model.getConsoleDataMap().get(data.getConsoleId());
			if (consoleData == null) {
				Log.error("Console data not found for " + data.getConsoleName() + " (" + data.getConsoleId() + ")");
				return;
			}
			parseUserCompletionData(data);
			if (!consoleData.getGameDataMap().containsKey(data.getId())) {
				consoleData.getGameDataMap().put(data.getId(), data);
			}
		});
		return gameData;
	}

	private List<GameData> requestCompletionProgressLoop(final int currentCount, final List<GameData> data) {
		final String resBody = requestData(USER_COMPLETION_PROGRESS_METHOD, "o=" + currentCount).body();
		try {
			final JsonNode node = mapper.readTree(resBody);
			final int newCount = node.get("Count").asInt();
			final int totalCount = node.get("Total").asInt();
			final String gameDataBody = node.get("Results").toString();
			final List<GameData> gameData = mapper.readValue(gameDataBody, new TypeReference<List<GameData>>() {
			});
			data.addAll(gameData);
			if (currentCount < totalCount) {
				// Sleep to avoid too many requests
				SleepUtils.sleep(500);
				Log.info("Reading " + (newCount + currentCount) + " / " + totalCount);
				requestCompletionProgressLoop(newCount + currentCount, data);
			}
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
			return null;
		}
		return data;
	}

	public List<GameData> getConsoleGames(final int consoleId) {
		Log.info("Getting console games for console id " + consoleId);
		final String resBody = requestData(CONSOLE_GAMES_METHOD, "i=" + Integer.toString(consoleId), "f=1").body();
		LoggingUtils.prettyPrint(mapper, resBody);
		try {
			final List<GameData> gameData = mapper.readValue(resBody, new TypeReference<List<GameData>>() {
			});
			Log.info("Found " + gameData.size() + " games for console " + consoleId);
			// Add game data to console data
			gameData.forEach(data -> {
				final ConsoleData consoleData = model.getConsoleDataMap().get(data.getConsoleId());
				if (consoleData == null) {
					Log.error("Console data not found for " + data.getConsoleName() + " (" + data.getConsoleId() + ")");
					return;
				}
				GameData existingGameData;
				if (!consoleData.getGameDataMap().containsKey(data.getId())) {
					existingGameData = data;
					existingGameData.setPercent(0d);
					existingGameData.setCompletionStatus(CompletionStatusEnum.NOT_PLAYED);
					consoleData.getGameDataMap().put(data.getId(), existingGameData);
				} else {
					existingGameData = consoleData.getGameDataMap().get(data.getId());
					existingGameData.setTotalPoints(data.getTotalPoints());
				}
				existingGameData.setTruePoints(existingGameData.getTotalPoints());
			});
			return gameData;
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
			return null;
		}
	}

	public GameData getFullGameData(final int gameId) {
		final Optional<GameData> existingGameDataOpt = getRAGameById(gameId);
		if (existingGameDataOpt.isEmpty()) {
			Log.error("No RA game found for id " + gameId);
			return null;
		}
		Log.info("Getting full game data for RA game " + gameId);
		final GameData existingGameData = existingGameDataOpt.get();

		// Game schema
		final String schemaResBody = requestData(GAME_INFO_PROGRESS_METHOD, "g=" + gameId).body();
		LoggingUtils.prettyPrint(mapper, schemaResBody);

		try {
			final JsonNode node = mapper.readTree(schemaResBody);
			// Total players
			final int totalPlayers = node.get("NumDistinctPlayers").asInt();
			existingGameData.setTotalPlayers(totalPlayers);
			// Image
			final String imageBoxArt = node.get("ImageBoxArt").asText();
			existingGameData.setImageURL(BOXART_URL + imageBoxArt);
			// Achievements
			final JsonNode achievementsNode = node.get("Achievements");
			final String dataBody = achievementsNode.toString();
			final Map<Integer, AchievementData> achievementData = mapper.readValue(dataBody,
					new TypeReference<Map<Integer, AchievementData>>() {
					});
			achievementData.values().forEach(ach -> {
				final Optional<AchievementData> existingAchievement = existingGameData.getAchievementData().stream()
						.filter(existingAch -> existingAch.getId() == ach.getId()).findFirst();
				AchievementData achievement;
				if (existingAchievement.isEmpty()) {
					achievement = ach;
				} else {
					achievement = existingAchievement.get();
				}
				// Update data from existing achievement
				final double percent = Math.round(10d * 100d * ach.getNumAwarded() / totalPlayers) / 10d;
				achievement.setUnlockPercentage(percent);
				achievement.setIconUnlockedURL(BADGE_URL + ach.getBadgeId() + BADGE_UNLOCKED);
				achievement.setIconLockedURL(BADGE_URL + ach.getBadgeId() + BADGE_LOCKED);
				achievement.setAchieved(!ach.getDateEarned().isBlank());
				if (existingAchievement.isEmpty()) {
					existingGameData.getAchievementData().add(achievement);
				}
			});
			// Game data
			parseFullAchievementData(existingGameData);
			return existingGameData;
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as AchievevementData", e);
			return existingGameData;
		}
	}

	private GameData mapCompletionStatus(final GameData gameData) {
		if (MASTERY_COMPLETION_STRING.equals(gameData.getAwardKind())) {
			// Check for previous mastery
			if (gameData.getAwardedAchievements() < gameData.getTotalAchievements()) {
				gameData.setCompletionStatus(CompletionStatusEnum.BEATEN);
			} else {
				gameData.setCompletionStatus(CompletionStatusEnum.MASTERED);
			}
		} else if (GAME_BEATEN_STRING.equals(gameData.getAwardKind())) {
			gameData.setCompletionStatus(CompletionStatusEnum.BEATEN);
		} else {
			if (gameData.getAwardedAchievements() > 0) {
				gameData.setCompletionStatus(CompletionStatusEnum.TRIED);
			} else {
				gameData.setCompletionStatus(CompletionStatusEnum.NOT_PLAYED);
			}
		}
		Log.info(gameData + " (" + gameData.getId() + ") for " + gameData.getConsoleName() + " is "
				+ gameData.getCompletionStatus());

		return gameData;
	}

	private GameData parseUserCompletionData(final GameData gameData) {
		mapCompletionStatus(gameData);
		gameData.setPercent(100d * gameData.getAwardedAchievements() / gameData.getTotalAchievements());
		return gameData;
	}

	private GameData parseFullAchievementData(final GameData gameData) {
		// Parse game total achievements
		gameData.setTotalAchievements(gameData.getAchievementData().size());
		gameData.setAwardedAchievements(
				(int) gameData.getAchievementData().stream().filter(AchievementData::isAchieved).count());

		mapCompletionStatus(gameData);
		setGameAchievementPercent(gameData);

		// Parse total achievements points
		gameData.setTotalPoints(gameData.getAchievementData().stream().mapToInt(AchievementData::getPoints).sum());
		gameData.setTruePoints(gameData.getAchievementData().stream().mapToInt(AchievementData::getRealPoints).sum());
		if (gameData.getTotalPoints() != 0) {
			gameData.setRatio((double) gameData.getTruePoints() / gameData.getTotalPoints());
		} else {
			gameData.setRatio(1);
		}

		// Parse earned achievements points
		gameData.setEarnedPoints(gameData.getAchievementData().stream().filter(AchievementData::isAchieved)
				.mapToInt(AchievementData::getPoints).sum());
		gameData.setEarnedTruePoints(gameData.getAchievementData().stream().filter(AchievementData::isAchieved)
				.mapToInt(AchievementData::getRealPoints).sum());
		if (gameData.getEarnedPoints() != 0) {
			gameData.setEarnedRatio((double) gameData.getEarnedTruePoints() / gameData.getEarnedPoints());
		} else {
			gameData.setEarnedRatio(1);
		}

		Log.info(gameData.getTitle() + " (" + gameData.getId() + ") for Steam is " + gameData.getCompletionStatus()
				+ " with " + gameData.getAwardedAchievements() + " / " + gameData.getTotalAchievements()
				+ " achievements and " + gameData.getTotalPoints() + " (" + gameData.getTruePoints() + ") points");
		return gameData;
	}

	private Optional<GameData> getRAGameById(final int gameId) {
		for (final ConsoleData console : model.getConsoleDataMap().values()) {
			if (!ConsoleSourceEnum.RETRO_ACHIEVEMENTS.equals(console.getSource())) {
				continue;
			}
			for (final GameData game : console.getGameDataMap().values()) {
				if (game.getId() == gameId) {
					return Optional.of(game);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public ObjectMapper getMapper() {
		return mapper;
	}
}