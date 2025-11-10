package perso.project.steam;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvException;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.AchievementData;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.Model;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractRequestService;
import perso.project.utils.LoggingUtils;

@ApplicationScoped
public class SteamRequestService extends AbstractRequestService {
	static final int RETRY_MAX = 3;
	private int retryIndex = 1;

	static final String STEAM_API_KEY_KEY = "STEAM_API_KEY";

	static final String MAIN_URI = "https://api.steampowered.com/";

	static final String PLAYER_SERVICE = "IPlayerService";
	static final String STEAM_USER_STATS = "ISteamUserStats";

	static final String OWNED_GAMES_METHOD = "GetOwnedGames";
	static final String PLAYER_ACHIEVEMENTS_METHOD = "GetPlayerAchievements";
	static final String GAME_SCHEMA_METHOD = "GetSchemaForGame";
	static final String PERCENTAGES_METHOD = "GetGlobalAchievementPercentagesForApp";
	static final String APPDETAILS_URL = "https://store.steampowered.com/api/appdetails?appids=";

	static final String V001 = "v001";
	static final String V002 = "v002";

	static final String IMAGE_URL = "http://media.steampowered.com/steamcommunity/public/images/apps/";
	static final String IMAGE_URL_SUFFIX = ".jpg";

	@Inject
	@ConfigProperty(name = "steam.id")
	String steamId;

	/**
	 * Creates <b>blocking</b> HTTP request
	 * 
	 * @param method
	 * @param params
	 * @return
	 */
	HttpResponse<String> requestData(final String service, final String method, final String version,
			final String... params) {
		final String steamApiKey = System.getenv(STEAM_API_KEY_KEY);
		if (steamApiKey == null) {
			Log.error("Steam API Key not defined as environment variable");
			return null;
		}
		try {
			final StringBuilder uriString = new StringBuilder();
			uriString.append(MAIN_URI).append(service).append("/").append(method).append("/").append(version)
					.append("/").append("?").append("key=").append(steamApiKey).append("&steamid=").append(steamId);
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
		ConsoleData steamConsoleData;
		if (!model.getConsoleDataMap().containsKey(Model.STEAM_CONSOLE_ID)) {
			Log.info("Getting Steam console data");
			steamConsoleData = new ConsoleData();
			steamConsoleData.setActive(true);
			steamConsoleData.setGameSystem(true);
			steamConsoleData.setId(Model.STEAM_CONSOLE_ID);
			steamConsoleData.setName(ConsoleSourceEnum.STEAM.getName());
			steamConsoleData.setSource(ConsoleSourceEnum.STEAM);
			model.getConsoleDataMap().put(steamConsoleData.getId(), steamConsoleData);
		} else {
			steamConsoleData = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID);
		}
		return List.of(steamConsoleData);
	}

	public List<GameData> getOwnedGames() {
		Log.info("Getting all Steam owned games");
		final String resBody = requestData(PLAYER_SERVICE, OWNED_GAMES_METHOD, V001, "format=json", "include_appinfo=1",
				"include_played_free_games=1", "skip_unvetted_apps=0").body();
		LoggingUtils.prettyPrint(mapper, resBody);

		try {
			final JsonNode node = mapper.readTree(resBody);
			final String gameDataBody = node.get("response").get("games").toString();
			final List<GameData> gameData = mapper.readValue(gameDataBody, new TypeReference<List<GameData>>() {
			});
			gameData.forEach(data -> {
				data.setConsoleId(Model.STEAM_CONSOLE_ID);
				data.setConsoleName("Steam");
				data.setImageURL(
						IMAGE_URL + Integer.toString(data.getId()) + "/" + data.getImageURL() + "/" + IMAGE_URL_SUFFIX);
				model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap().put(data.getId(), data);
			});
			Log.info("Found " + gameData.size() + " games for Steam");
			return gameData;
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
			return null;
		}
	}

	public GameData getSimpleGameData(final int gameId) {
		Log.info("Getting simple game data for Steam game " + gameId);
		final GameData existingGameData = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap()
				.get(gameId);
		if (existingGameData == null) {
			Log.error("No Steam game found for id " + gameId);
			return null;
		}

		getAchievements(gameId);
		setPercentageData(existingGameData);
		parseAchievementData(existingGameData);

		return existingGameData;
	}

	public GameData getFullGameData(final int gameId) {
		Log.info("Getting full game data for Steam game " + gameId);
		final GameData existingGameData = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap()
				.get(gameId);
		if (existingGameData == null) {
			Log.error("No Steam game found for id " + gameId);
			return null;
		}

		getAchievements(gameId);
		setAchievementData(existingGameData);
		setPercentageData(existingGameData);
		setImageURL(existingGameData);
		parseAchievementData(existingGameData);

		return existingGameData;
	}

	/**
	 * Gets which achievement user has unlocked
	 * 
	 * @param gameId
	 * @return
	 */
	private GameData getAchievements(final int gameId) {
		Log.info("Getting achievements for Steam game " + gameId);
		final GameData existingGameData = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap()
				.get(gameId);
		if (existingGameData == null) {
			Log.error("No Steam game found for id " + gameId);
			return null;
		}

		final String resBody = requestData(STEAM_USER_STATS, PLAYER_ACHIEVEMENTS_METHOD, V001, "appid=" + gameId)
				.body();
		LoggingUtils.prettyPrint(mapper, resBody);

		try {
			final JsonNode node = mapper.readTree(resBody);
			final JsonNode achievementsNode = node.get("playerstats").get("achievements");
			if (achievementsNode == null) {
				// No achievements
				Log.info("Found no achievements for Steam game " + existingGameData.getTitle() + " (" + gameId + ")");
			} else {
				final String dataBody = achievementsNode.toString();
				final List<AchievementData> achievementData = mapper.readValue(dataBody,
						new TypeReference<List<AchievementData>>() {
						});
				existingGameData.getAchievementData().clear();
				existingGameData.getAchievementData().addAll(achievementData);
			}
			retryIndex = 1;
			return existingGameData;
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
			if (retryIndex > RETRY_MAX) {
				Log.error("Out of retries for " + existingGameData.getId());
				return null;
			} else {
				Log.error("Retrying for " + existingGameData.getId() + " => " + retryIndex + " / " + RETRY_MAX + "...");
				retryIndex++;
				return getAchievements(gameId);
			}
		}
	}

	/**
	 * Gets description and icons for each achievement
	 * 
	 * @param gameData
	 * @return
	 */
	private GameData setAchievementData(final GameData gameData) {
		// Game schema
		final String schemaResBody = requestData(STEAM_USER_STATS, GAME_SCHEMA_METHOD, V002,
				"appid=" + gameData.getId()).body();
		LoggingUtils.prettyPrint(mapper, schemaResBody);
		try {
			final JsonNode node = mapper.readTree(schemaResBody);
			final JsonNode gameNode = node.get("game").get("availableGameStats");
			// No achievements
			if (gameNode == null) {
				Log.info("Game " + gameData.getTitle() + " " + (gameData.getId()) + " has no achievements. Returning");
				return gameData;
			}
			final JsonNode achievementsNode = gameNode.get("achievements");
			if (achievementsNode == null) {
				// No achievements
				Log.info("Found no achievements for Steam game " + gameData.getTitle() + " (" + gameData.getId() + ")");
			} else {
				final String dataBody = achievementsNode.toString();
				final List<AchievementData> achievementData = mapper.readValue(dataBody,
						new TypeReference<List<AchievementData>>() {
						});
				achievementData.forEach(ach -> {
					final Optional<AchievementData> existingAchievement = gameData.getAchievementData().stream()
							.filter(existingAch -> existingAch.getName().equals(ach.getName())).findFirst();
					AchievementData achievement;
					if (existingAchievement.isEmpty()) {
						achievement = ach;
					} else {
						achievement = existingAchievement.get();
					}
					// Update data from existing achievement
					achievement.setDisplayName(ach.getDisplayName());
					achievement.setDescription(ach.getDescription());
					achievement.setIconLockedURL(ach.getIconLockedURL());
					achievement.setIconUnlockedURL(ach.getIconUnlockedURL());
					if (existingAchievement.isEmpty()) {
						gameData.getAchievementData().add(achievement);
					}
				});
			}
			retryIndex = 1;
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as AchievevementData", e);
			if (retryIndex > RETRY_MAX) {
				Log.error("Out of retries for " + gameData.getId());
				return null;
			} else {
				Log.error("Retrying for " + gameData.getId() + " => " + retryIndex + " / " + RETRY_MAX + "...");
				retryIndex++;
				return setAchievementData(gameData);
			}
		}
		return gameData;
	}

	/**
	 * Gets unlock percentage data for all achievements
	 * 
	 * @param gameData
	 * @return
	 */
	private GameData setPercentageData(final GameData gameData) {
		if (gameData.getAchievementData().isEmpty()) {
			Log.info("No achievements for Steam game " + gameData.getTitle() + " (" + gameData.getId() + "). Ignoring");
			return gameData;
		}
		// Unlock rates
		final String percentagesResBody = requestData(STEAM_USER_STATS, PERCENTAGES_METHOD, V002,
				"gameid=" + gameData.getId()).body();
		LoggingUtils.prettyPrint(mapper, percentagesResBody);

		try {
			final JsonNode node = mapper.readTree(percentagesResBody);
			final JsonNode achievementsNode = node.get("achievementpercentages");
			if (achievementsNode == null) {
				// No achievements
				Log.info("Found no achievements for Steam game " + gameData.getTitle() + " (" + gameData.getId() + ")");
			} else {
				final String dataBody = achievementsNode.get("achievements").toString();
				final List<AchievementData> achievementData = mapper.readValue(dataBody,
						new TypeReference<List<AchievementData>>() {
						});
				achievementData.forEach(ach -> {
					final Optional<AchievementData> existingAchievement = gameData.getAchievementData().stream()
							.filter(existingAch -> existingAch.getName().equals(ach.getName())).findFirst();
					AchievementData achievement;
					if (existingAchievement.isEmpty()) {
						achievement = ach;
					} else {
						achievement = existingAchievement.get();
					}
					// Update data from existing achievement
					achievement.setUnlockPercentage(ach.getUnlockPercentage());
					if (existingAchievement.isEmpty()) {
						gameData.getAchievementData().add(ach);
					}
				});
			}
			retryIndex = 1;
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as AchievevementData", e);
			if (retryIndex > RETRY_MAX) {
				Log.error("Out of retries for " + gameData.getId());
				return null;
			} else {
				Log.error("Retrying for " + gameData.getId() + " => " + retryIndex + " / " + RETRY_MAX + "...");
				retryIndex++;
				return setPercentageData(gameData);
			}
		}
		return gameData;
	}

	/**
	 * Gets game header image
	 * 
	 * @param gameData
	 * @return
	 */
	private GameData setImageURL(final GameData gameData) {
		// Game schema
		final String schemaResBody = requestHttpURI(URI.create(APPDETAILS_URL + gameData.getId())).body();
		LoggingUtils.prettyPrint(mapper, schemaResBody);
		try {
			final JsonNode node = mapper.readTree(schemaResBody);
			final JsonNode gameNode = node.get(Integer.toString(gameData.getId()));
			if (!gameNode.get("success").asBoolean()) {
				Log.info("No image for game " + gameData.getTitle() + " (" + gameData.getId() + " )");
				return gameData;
			}
			final String headerImageURL = gameNode.get("data").get("header_image").asText();
			gameData.setImageURL(headerImageURL);
			retryIndex = 1;
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
			if (retryIndex > RETRY_MAX) {
				Log.error("Out of retries for " + gameData.getId());
				return null;
			} else {
				Log.error("Retrying for " + gameData.getId() + " => " + retryIndex + " / " + RETRY_MAX + "...");
				retryIndex++;
				return setImageURL(gameData);
			}
		}
		return gameData;
	}

	/**
	 * Reads SteamBeaten file and update or add list
	 * 
	 * @param path
	 * @return
	 */
	public List<GameData> getSteamGames_Beaten(final Path path) {
		Log.info("Reading " + path);
		final List<GameData> beatenList = new ArrayList<>();
		final RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
		try (final FileReader fileReader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
			final CSVReader reader = new CSVReaderBuilder(fileReader).withCSVParser(rfc4180Parser).build();
			final List<String[]> stringList = reader.readAll();
			for (final String[] str : stringList) {
				final String gameName = str[0];
				final String gameIdStr = str[1];
				if (gameIdStr.isBlank()) {
					Log.error("No game id for game " + gameName);
					continue;
				}
				final int gameId = Integer.parseInt(gameIdStr);
				GameData gameData = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap().get(gameId);
				if (gameData == null) {
					gameData = new GameData();
					gameData.setTitle(gameName);
					gameData.setId(gameId);
					gameData.setConsoleId(Model.STEAM_CONSOLE_ID);
					gameData.setConsoleName("Steam");
				}
				gameData.setCompletionStatus(CompletionStatusEnum.BEATEN);
				parseAchievementData(gameData);

				model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap().put(gameId, gameData);
				beatenList.add(gameData);
				Log.info(gameName + " (" + gameId + ") for Steam is Beaten");
			}
			return beatenList;
		} catch (final IOException | CsvException e) {
			Log.error("Cannot read Steam beaten file at " + path);
			return null;
		}
	}

	/**
	 * Reads SteamMastered file and update or add list
	 * 
	 * @param path
	 * @return
	 */
	public List<GameData> getSteamGames_Mastered(final Path path) {
		Log.info("Reading " + path);
		final List<GameData> masteredList = new ArrayList<>();
		final RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
		try (final FileReader fileReader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
			final CSVReader reader = new CSVReaderBuilder(fileReader).withCSVParser(rfc4180Parser).build();
			final List<String[]> stringList = reader.readAll();
			for (final String[] str : stringList) {
				final String gameName = str[0];
				final String gameIdStr = str[1];
				if (gameIdStr.isBlank()) {
					Log.error("No game id for game " + gameName);
					continue;
				}
				final int gameId = Integer.parseInt(gameIdStr);
				GameData gameData = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap().get(gameId);
				if (gameData == null) {
					gameData = new GameData();
					gameData.setTitle(gameName);
					gameData.setId(gameId);
					gameData.setConsoleId(Model.STEAM_CONSOLE_ID);
					gameData.setConsoleName("Steam");
				}
				gameData.setCompletionStatus(CompletionStatusEnum.MASTERED);
				parseAchievementData(gameData);

				model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap().put(gameId, gameData);
				masteredList.add(gameData);
				Log.info(gameName + " (" + gameId + ") for Steam is Mastered");
			}
			return masteredList;
		} catch (final IOException | CsvException e) {
			Log.error("Cannot read Steam mastered file at " + path);
			return null;
		}
	}

	/**
	 * Reads games that are in user database but removed from store
	 * 
	 * @param path
	 * @return
	 */
	public List<GameData> getSteamGames_NotInDatabase(final Path path) {
		Log.info("Reading " + path);
		final List<GameData> removedList = new ArrayList<>();
		final RFC4180Parser rfc4180Parser = new RFC4180ParserBuilder().build();
		try (final FileReader fileReader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
			final CSVReader reader = new CSVReaderBuilder(fileReader).withCSVParser(rfc4180Parser).build();
			final List<String[]> stringList = reader.readAll();
			for (int i = 0; i < stringList.size(); i++) {
				final String[] str = stringList.get(i);
				final String gameName = str[0];
				final String gameIdStr = str[1];
				Integer gameId = null;
				if (gameIdStr.isBlank()) {
					Log.error("No game id for game " + gameName);
					continue;
				}
				try {
					gameId = Integer.parseInt(gameIdStr);
				} catch (final NumberFormatException e) {
					Log.error("Cannot parse int value for string " + gameIdStr + " at index " + i);
					continue;
				}
				GameData gameData = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap().get(gameId);
				if (gameData == null) {
					gameData = new GameData();
					gameData.setTitle(gameName);
					gameData.setId(gameId);
					gameData.setConsoleId(Model.STEAM_CONSOLE_ID);
					gameData.setConsoleName("Steam");
				} else {
					Log.error("Removed game " + gameName + " (" + gameId + ") exists in Steam database");
				}
				// Completion status is parsed in standard way through parseAchievementData
				model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap().put(gameId, gameData);
				removedList.add(gameData);
				Log.info(gameName + " (" + gameId + ") for Steam is not in database");
			}
			return removedList;
		} catch (final IOException | CsvException e) {
			Log.error("Cannot read Steam removed file at " + path);
			return null;
		}
	}

	private GameData parseAchievementData(final GameData gameData) {
		gameData.setTotalAchievements(gameData.getAchievementData().size());
		gameData.setAwardedAchievements(
				(int) gameData.getAchievementData().stream().filter(ach -> ach.isAchieved()).count());

		// Check if already set by Steam beaten and SteamMastered files
		// If beaten or mastered, already set by other methodso
		if (CompletionStatusEnum.NOT_PLAYED.equals(gameData.getCompletionStatus())) {
			if (gameData.getTotalAchievements() == 0) {
				gameData.setCompletionStatus(CompletionStatusEnum.NO_ACHIEVEMENTS);
			} else if (gameData.getAwardedAchievements() == gameData.getTotalAchievements()) {
				gameData.setCompletionStatus(CompletionStatusEnum.MASTERED);
			} else if (gameData.getAwardedAchievements() > 0) {
				gameData.setCompletionStatus(CompletionStatusEnum.TRIED);
			}
		}

		// Parse achievements percentage and points
		setGameAchievementPercent(gameData);
		gameData.getAchievementData().forEach(ach -> parseAchievementPoints(gameData, ach));

		// Parse total achievement data
		gameData.setTotalPoints(gameData.getAchievementData().stream().mapToInt(AchievementData::getPoints).sum());
		gameData.setTruePoints(gameData.getAchievementData().stream().mapToInt(AchievementData::getRealPoints).sum());
		if (gameData.getTotalPoints() != 0) {
			gameData.setRatio((double) gameData.getTruePoints() / gameData.getTotalPoints());
		} else {
			gameData.setRatio(0);
		}

		// Parse earned achievement data
		gameData.setEarnedPoints(gameData.getAchievementData().stream().filter(AchievementData::isAchieved)
				.mapToInt(AchievementData::getPoints).sum());
		gameData.setEarnedTruePoints(gameData.getAchievementData().stream().filter(AchievementData::isAchieved)
				.mapToInt(AchievementData::getRealPoints).sum());
		if (gameData.getEarnedPoints() != 0) {
			gameData.setEarnedRatio((double) gameData.getEarnedTruePoints() / gameData.getEarnedPoints());
		} else {
			gameData.setEarnedRatio(0);
		}

		Log.info(gameData.getTitle() + " (" + gameData.getId() + ") for Steam is " + gameData.getCompletionStatus()
				+ " with " + gameData.getAwardedAchievements() + " / " + gameData.getTotalAchievements()
				+ " achievements and " + gameData.getTotalPoints() + " (" + gameData.getTruePoints() + ") points");
		return gameData;
	}

	private AchievementData parseAchievementPoints(final GameData game, final AchievementData ach) {
		final double p = ach.getUnlockPercentage();
		int points;
		if (p <= 100 && p >= 90) {
			points = 1;
		} else if (p < 90 && p >= 75) {
			points = 2;
		} else if (p < 75 && p >= 60) {
			points = 3;
		} else if (p < 60 && p >= 40) {
			points = 4;
		} else if (p < 40 && p >= 20) {
			points = 5;
		} else if (p < 20 && p >= 10) {
			points = 10;
		} else if (p < 10 && p >= 5) {
			points = 20;
		} else if (p < 5 && p >= 3) {
			points = 30;
		} else if (p < 3 && p >= 1) {
			points = 50;
		} else if (p < 1 && p >= 0) {
			points = 100;
		} else {
			points = 0;
		}
		ach.setPoints(points);

		// Retro achievements formula
		// p = points
		// a = achievers
		// t = total users who have played the game
		// r = 0.6
		// RP = p * r + ( p * ( t / a ) * ( 1 - r ) )
		// This formula is the same but replaces t / a by 100/percentage
		double ratio = 0.6;
		int truePoints = (int) Math.round((points * ratio) + points * (100 / p) * (1 - ratio));
		ach.setRealPoints(truePoints);

		return ach;
	}

	@Override
	public ObjectMapper getMapper() {
		return mapper;
	}
}
