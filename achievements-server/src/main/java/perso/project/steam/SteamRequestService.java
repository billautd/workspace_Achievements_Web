package perso.project.steam;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.MainModel;
import perso.project.model.Model;
import perso.project.model.SteamAchievementData;
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.socket.GamesSocketEndpoint;
import perso.project.utils.LoggingUtils;
import perso.project.utils.SleepUtils;

@ApplicationScoped
public class SteamRequestService {
	static final int RETRY_MAX = 3;
	private int retryIndex = 1;

	static final String STEAM_API_KEY_KEY = "STEAM_API_KEY";

	static final String MAIN_URI = "https://api.steampowered.com/";

	static final String PLAYER_SERVICE = "IPlayerService";
	static final String STEAM_USER_STATS = "ISteamUserStats";

	static final String OWNED_GAMES_METHOD = "GetOwnedGames";
	static final String PLAYER_ACHIEVEMENTS_METHOD = "GetPlayerAchievements";

	static final String V001 = "v001";
	static final String V002 = "v002";

	@Inject
	@ConfigProperty(name = "steam.id")
	String steamId;

	@Inject
	MainModel model;

	@Inject
	GamesSocketEndpoint gamesSocketEndpoint;

	ObjectMapper mapper;

	public SteamRequestService() {
		setupMapper();
	}

	ObjectMapper setupMapper() {
		mapper = new ObjectMapper();

		final JavaTimeModule module = new JavaTimeModule();
		mapper.registerModule(module);

		return mapper;
	}

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
			Log.info("Creating request for URI : " + uri.toString());

			final HttpClient client = HttpClient.newBuilder().build();
			final HttpRequest request = HttpRequest.newBuilder(uri).build();
			return client.send(request, BodyHandlers.ofString());
		} catch (URISyntaxException | IOException | InterruptedException e) {
			Log.error("Error creating URI", e);
			return null;
		}
	}

	public void getOwnedGames() {
		Log.info("Getting all Steam owned games");
		final String resBody = requestData(PLAYER_SERVICE, OWNED_GAMES_METHOD, V001, "format=json", "include_appinfo=1",
				"include_played_free_games=1", "skip_unvetted_apps=0").body();
		LoggingUtils.prettyPrint(mapper, resBody);

		final ConsoleData steamConsoleData = new ConsoleData();
		steamConsoleData.setActive(true);
		steamConsoleData.setGameSystem(true);
		steamConsoleData.setId(Model.STEAM_CONSOLE_ID);
		steamConsoleData.setName("Steam");
		model.getConsoleDataMap().put(steamConsoleData.getId(), steamConsoleData);
		try {
			final JsonNode node = mapper.readTree(resBody);
			final String gameDataBody = node.get("response").get("games").toString();
			final List<GameData> gameData = mapper.readValue(gameDataBody, new TypeReference<List<GameData>>() {
			});
			gameData.forEach(data -> {
				data.setConsoleId(Model.STEAM_CONSOLE_ID);
				data.setConsoleName("Steam");
				steamConsoleData.getGameDataMap().put(data.getId(), data);
			});
			Log.info("Found " + gameData.size() + " games for Steam");
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
		}
	}

	public void getAchievements(final GameData gameData) {
		Log.info("Getting achievements for " + gameData.getTitle() + " (" + gameData.getId() + ")");
		final String resBody = requestData(STEAM_USER_STATS, PLAYER_ACHIEVEMENTS_METHOD, V001,
				"appid=" + gameData.getId()).body();
		LoggingUtils.prettyPrint(mapper, resBody);

		try {
			final JsonNode node = mapper.readTree(resBody);
			final JsonNode achievementsNode = node.get("playerstats").get("achievements");
			if (achievementsNode == null) {
				// No achievements
				Log.info("Found no achievements for Steam game " + gameData.getTitle() + " (" + gameData.getId() + ")");
			} else {
				final String dataBody = achievementsNode.toString();
				final List<SteamAchievementData> achievementData = mapper.readValue(dataBody,
						new TypeReference<List<SteamAchievementData>>() {
						});
				achievementData.forEach(gameData.getSteamAchievementData()::add);
			}
			parseAchievementData(gameData);

			retryIndex = 1;
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
			if (retryIndex > RETRY_MAX) {
				Log.error("Out of retries for " + gameData.getId());
			} else {
				Log.error("Retrying for " + gameData.getId() + " => " + retryIndex + " / " + RETRY_MAX + "...");
				retryIndex++;
			}
			getAchievements(gameData);
		}
	}

	private void parseAchievementData(final GameData gameData) {
		// Check if already set by Steambeaten and SteamMastered files
		if (gameData.getSteamAchievementData().isEmpty()) {
			gameData.setAwardedAchievements(0);
			gameData.setTotalAchievements(0);
			// If beaten or mastered, already set by other methods
			if (CompletionStatusEnum.NOT_PLAYED.equals(gameData.getCompletionStatus())) {
				gameData.setCompletionStatus(CompletionStatusEnum.NO_ACHIEVEMENTS);
			}
		} else {
			gameData.setTotalAchievements(gameData.getSteamAchievementData().size());
			gameData.setAwardedAchievements(
					(int) gameData.getSteamAchievementData().stream().filter(ach -> ach.isAchieved()).count());
			if (CompletionStatusEnum.NOT_PLAYED.equals(gameData.getCompletionStatus())) {
				if (gameData.getAwardedAchievements() == gameData.getTotalAchievements()) {
					gameData.setCompletionStatus(CompletionStatusEnum.MASTERED);
				} else if (gameData.getAwardedAchievements() > 0) {
					gameData.setCompletionStatus(CompletionStatusEnum.TRIED);
				}
			}
		}
		Log.info(gameData.getTitle() + " (" + gameData.getId() + ") for Steam is " + gameData.getCompletionStatus()
				+ " with " + gameData.getAwardedAchievements() + " / " + gameData.getTotalAchievements()
				+ " achievements");
	}

	public void getSteamGames_Beaten(final String path) {
		Log.info("Reading " + path + "file");
		try (final XSSFWorkbook beatenWorkbook = new XSSFWorkbook(new File(path))) {
			for (final Row row : beatenWorkbook.getSheetAt(0)) {
				String gameName = row.getCell(0).getStringCellValue();
				int gameId = (int) Double.parseDouble(row.getCell(1).toString());
				final GameData gameData = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap()
						.get(gameId);
				if (gameData == null) {
					Log.error(gameName + " (" + gameId + ") does not exist for Steam");
					continue;
				}
				gameData.setCompletionStatus(CompletionStatusEnum.BEATEN);
				Log.info(gameName + " (" + gameId + ") for Steam is Beaten");
			}
		} catch (InvalidFormatException | IOException e) {
			Log.error("Cannot read Steam beaten file at " + path);
		}
	}

	public void getSteamGames_Mastered(final String path) {
		Log.info("Reading " + path + "file");
		try (final XSSFWorkbook masteredWorkbook = new XSSFWorkbook(new File(path))) {
			for (final Row row : masteredWorkbook.getSheetAt(0)) {
				String gameName = row.getCell(0).getStringCellValue();
				int gameId = (int) Double.parseDouble(row.getCell(1).toString());
				final GameData gameData = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap()
						.get(gameId);
				if (gameData == null) {
					Log.error(gameName + " (" + gameId + ") does not exist for Steam");
					continue;
				}
				gameData.setCompletionStatus(CompletionStatusEnum.MASTERED);
				Log.info(gameName + " (" + gameId + ") for Steam is Mastered");
			}
		} catch (InvalidFormatException | IOException e) {
			Log.error("Cannot read Steam beaten file at " + path);
		}
	}

	public void getAllData() {
		Log.info("Getting all Steam games");
		getOwnedGames();
		SleepUtils.sleep(2000);
		getSteamGames_Beaten("C:\\Users\\dbill\\Downloads\\SteamBeaten.xlsx");
		getSteamGames_Mastered("C:\\Users\\dbill\\Downloads\\SteamMastered.xlsx");
		final List<GameData> gameData = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap().values()
				.stream().toList();
		for (int i = 0; i < gameData.size(); i++) {
			Log.info("Processing Steam : " + (i + 1) + " / " + gameData.size());
			getAchievements(gameData.get(i));
		}
		try {
			gamesSocketEndpoint.sendStringDataBroadcast(mapper.writeValueAsString(
					model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap().values()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

//		String toSend = "";
//		try (final BufferedReader reader = new BufferedReader(
//				new FileReader(new File("C:\\Users\\dbill\\Downloads\\dataToSend_Steam.txt")))) {
//			toSend = String.join("", reader.lines().toList());
//			gamesSocketEndpoint.sendStringDataBroadcast(toSend);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}
}
