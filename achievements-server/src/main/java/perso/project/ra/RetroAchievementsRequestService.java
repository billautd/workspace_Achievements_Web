package perso.project.ra;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

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
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.LoggingUtils;
import perso.project.utils.SleepUtils;

@ApplicationScoped
public class RetroAchievementsRequestService {
	static final String RA_API_KEY_KEY = "RA_API_KEY";

	static final String MAIN_URI = "https://retroachievements.org/API";

	static final String CONSOLE_IDS_METHOD = "GetConsoleIDs";
	static final String CONSOLE_GAMES_METHOD = "GetGameList";
	static final String USER_COMPLETION_PROGRESS_METHOD = "GetUserCompletionProgress";

	static final String MASTERY_COMPLETION_STRING = "mastered";
	static final String GAME_BEATEN_STRING = "beaten-hardcore";

	@Inject
	@ConfigProperty(name = "ra.username")
	String raUsername;

	@Inject
	MainModel model;

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
			Log.debug("Creating request for URI : " + uri.toString());

			final HttpClient client = HttpClient.newBuilder().build();
			final HttpRequest request = HttpRequest.newBuilder(uri).build();
			return client.send(request, BodyHandlers.ofString());
		} catch (URISyntaxException | IOException | InterruptedException e) {
			Log.error("Error creating URI", e);
			return null;
		}
	}

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
			mapCompletionStatus(data);
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
				if (!consoleData.getGameDataMap().containsKey(data.getId())) {
					consoleData.getGameDataMap().put(data.getId(), data);
					data.setCompletionStatus(CompletionStatusEnum.NOT_PLAYED);
				}
			});
			return gameData;
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
			return null;
		}
	}

	private void mapCompletionStatus(final GameData gameData) {
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
			gameData.setCompletionStatus(CompletionStatusEnum.TRIED);
		}
		Log.info(gameData.getTitle() + " (" + gameData.getId() + ") is " + gameData.getCompletionStatus());
	}

	public ObjectMapper getMapper() {
		return mapper;
	}
}