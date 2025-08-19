package perso.project.ra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Optional;

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
import perso.project.model.enums.CompletionStatusEnum;
import perso.project.model.enums.UserAwardData;
import perso.project.utils.LoggingUtils;

@ApplicationScoped
public class RetroAchievementsRequestService {
	static final String RA_API_KEY_KEY = "RA_API_KEY";

	static final String MAIN_URI = "https://retroachievements.org/API";

	static final String CONSOLE_IDS_METHOD = "GetConsoleIDs";
	static final String CONSOLE_GAMES_METHOD = "GetGameList";
	static final String USER_AWARDS_METHOD = "GetUserAwards";
	static final String USER_COMPLETED_GAMES_METHOD = "GetUserCompletedGames";

	static final String MASTERY_COMPLETION_STRING = "Mastery/Completion";
	static final String GAME_BEATEN_STRING = "Game Beaten";

	@Inject
	@ConfigProperty(name = "ra.username")
	String raUsername;

	@Inject
	MainModel model;

	@Inject
	GamesSocketEndpoint gamesSocketEndpoint;

	ObjectMapper mapper;

	RetroAchievementsRequestService() {
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
			Log.info("Creating request for URI : " + uri.toString());

			final HttpClient client = HttpClient.newBuilder().build();
			final HttpRequest request = HttpRequest.newBuilder(uri).build();
			return client.send(request, BodyHandlers.ofString());
		} catch (URISyntaxException | IOException | InterruptedException e) {
			Log.error("Error creating URI", e);
			return null;
		}
	}

	public void getConsoleIds() {
		Log.info("Getting console ids");
		final String resBody = requestData(CONSOLE_IDS_METHOD).body();
		LoggingUtils.prettyPrint(mapper, resBody);
		try {
			final List<ConsoleData> consoleData = mapper.readValue(resBody, new TypeReference<List<ConsoleData>>() {
			});
			Log.info("Found " + consoleData.size() + " consoles");
			// Add console data to map only if it is a running game system
			consoleData.forEach(data -> {
				if (data.isGameSystem()) {
					Log.debug("Adding console data for " + data.getName() + " (" + data.getId() + ")");
					model.getConsoleDataMap().put(data.getId(), data);
				}
			});
			Log.info("Console data map is now size " + model.getConsoleDataMap().size());
		} catch (IOException e) {
			Log.error("Error reading response body as ConsoleData", e);
		}
	}

	public void getUserAwards() {
		Log.info("Getting user awards");
		final String resBody = requestData(USER_AWARDS_METHOD).body();
		LoggingUtils.prettyPrint(mapper, resBody);
		try {
			final JsonNode node = mapper.readTree(resBody);
			final String gameDataBody = node.get("VisibleUserAwards").toString();
			final List<UserAwardData> userAwardData = mapper.readValue(gameDataBody,
					new TypeReference<List<UserAwardData>>() {
					});
			Log.info("Found " + userAwardData.size() + " user awards");
			userAwardData.forEach(data -> {
				// Id not present, find by title
				final ConsoleData consoleData = model.getConsoleDataMap().get(data.getConsoleId());
				if (consoleData == null) {
					Log.error("Console data not found for " + data.getConsoleName() + " (" + data.getConsoleId() + ")");
					return;
				}
				final Optional<GameData> gameDataOpt = consoleData.getGameDataMap().values().stream()
						.filter(val -> val.getTitle().equals(data.getTitle())).findFirst();
				if (gameDataOpt.isEmpty()) {
					Log.error("Game data not found for title " + data.getTitle() + " for " + data.getConsoleName());
					return;
				}
				final GameData foundGameData = gameDataOpt.get();
				final boolean alreadyHasAward = foundGameData.getUserAwards().stream()
						.anyMatch(award -> award.getAwardType().equals(data.getAwardType()));
				if (!alreadyHasAward) {
					Log.debug("Adding user award with status " + data.getAwardType() + " for game " + data.getTitle()
							+ " on " + data.getConsoleName());
					foundGameData.getUserAwards().add(data);
				}
				mapCompletionStatus(foundGameData);
				Log.debug(foundGameData.getTitle() + " (" + foundGameData.getId() + ") is now "
						+ foundGameData.getCompletionStatus());
			});
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
		}
	}

	public void getUserCompletedGames() {
		Log.info("Getting user completed games");
		final String resBody = requestData(USER_COMPLETED_GAMES_METHOD).body();
		LoggingUtils.prettyPrint(mapper, resBody);
		try {
			final List<GameData> gameData = mapper.readValue(resBody, new TypeReference<List<GameData>>() {
			});
			Log.info("Found " + gameData.size() + " completed games");
			// Set num awarded for completion percentage
			gameData.forEach(data -> {
				final ConsoleData consoleData = model.getConsoleDataMap().get(data.getConsoleId());
				if (consoleData == null) {
					Log.error("Console data not found for " + data.getConsoleName() + " (" + data.getConsoleId() + ")");
					return;
				}
				final GameData gameDataValue = consoleData.getGameDataMap().get(data.getId());
				if (gameDataValue == null) {
					Log.error("Game data for " + data.getTitle() + " (" + data.getId() + ") not found");
					return;
				}
				Log.debug(data.getTitle() + " (" + data.getId() + ") has " + data.getAwardedAchievements()
						+ " awarded achievements out of" + data.getTotalAchievements());
				model.getConsoleDataMap().get(data.getConsoleId()).getGameDataMap().get(data.getId())
						.setAwardedAchievements(data.getAwardedAchievements());
			});
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
		}
	}

	public void getConsoleGames(final int consoleId) {
		Log.info("Getting console games for console id " + consoleId);
		final String resBody = requestData(CONSOLE_GAMES_METHOD, "i=" + Integer.toString(consoleId), "f=1").body();
		LoggingUtils.prettyPrint(mapper, resBody);
		try {
			final List<GameData> gameData = mapper.readValue(resBody, new TypeReference<List<GameData>>() {
			});
			Log.info("Found " + gameData.size() + " games for console " + consoleId);
			// Add game data to console data
			final ConsoleData consoleData = model.getConsoleDataMap().get(consoleId);
			gameData.forEach(data -> {
				Log.debug("Adding game data for " + data.getTitle() + " (" + data.getId() + ") on "
						+ data.getConsoleName() + " (" + data.getConsoleId() + ")");
				consoleData.getGameDataMap().put(data.getId(), data);
			});
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
		}
	}

	public void getAllConsoleGames() {
		Log.info("Getting all console games");

		// Calls are made sequentially to ensure model coherence
		// getConsoleIds();
		// SleepUtils.sleep(2000);
		// for (final int consoleId : model.getConsoleDataMap().keySet()) {
		// getConsoleGames(consoleId);
		// SleepUtils.sleep(2000);
		// }
		// getUserCompletedGames();
		// SleepUtils.sleep(2000);
		// getUserAwards();
		// SleepUtils.sleep(2000);
		// Send data as one packet
		// TODO : Send data in multiple packets
		// final List<GameData> dataToSend = new ArrayList<>();
		// model.getConsoleDataMap().values()
		// .forEach(consoleData ->
		// dataToSend.addAll(consoleData.getGameDataMap().values()));

		String toSend = "";
		try {
			final BufferedReader reader = new BufferedReader(
					new FileReader(new File("C:\\Users\\dbill\\Downloads\\dataToSend.txt")));
			toSend = String.join("", reader.lines().toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		gamesSocketEndpoint.sendStringDataBroadcast(toSend);
	}

	private void mapCompletionStatus(final GameData gameData) {
		CompletionStatusEnum status = CompletionStatusEnum.NOT_PLAYED;
		final boolean containsMastery = gameData.getUserAwards().stream()
				.anyMatch(d -> MASTERY_COMPLETION_STRING.equals(d.getAwardType()));
		final boolean containsBeaten = gameData.getUserAwards().stream()
				.anyMatch(d -> GAME_BEATEN_STRING.equals(d.getAwardType()));

		if (containsMastery) {
			if (gameData.getTotalAchievements() == gameData.getAwardedAchievements()) {
				status = CompletionStatusEnum.MASTERED;
			} else {
				status = CompletionStatusEnum.BEATEN;
			}
		} else if (containsBeaten) {
			status = CompletionStatusEnum.BEATEN;
		} else if (gameData.getAwardedAchievements() > 0) {
			status = CompletionStatusEnum.TRIED;
		}
		gameData.setCompletionStatus(status);
	}
}