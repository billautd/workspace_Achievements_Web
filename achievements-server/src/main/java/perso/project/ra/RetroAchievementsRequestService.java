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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.back.ConsoleData;
import perso.project.model.back.GameData;
import perso.project.model.back.MainModel;
import perso.project.model.front.GameDataFront;
import perso.project.utils.LoggingUtils;

@ApplicationScoped
public class RetroAchievementsRequestService {
	static final String RA_API_KEY_KEY = "RA_API_KEY";

	static final String MAIN_URI = "https://retroachievements.org/API";
	static final String CONSOLE_IDS_METHOD = "GetConsoleIDs";
	static final String CONSOLE_GAMES_METHOD = "GetGameList";
	static final String USER_AWARDS_METHOD = "GetUserAwards";
	static final String USER_COMPLETED_GAMES_METHOD = "GetUserCompletedGames";

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
			// Add console data to map only if it is a running game system
			consoleData.forEach(data -> {
				model.getConsoleDataMap().put(data.getId(), data);
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
	}

	public void getUserCompletedGames() {
		Log.info("Getting user completed games");
		final String resBody = requestData(USER_COMPLETED_GAMES_METHOD).body();
		LoggingUtils.prettyPrint(mapper, resBody);
		try {
			final List<GameData> gameData = mapper.readValue(resBody, new TypeReference<List<GameData>>() {
			});
			// Set num awarded for completion percentage
			gameData.forEach(data -> {
				model.getConsoleDataMap().get(data.getConsoleId()).getGameDataMap().get(data.getId())
						.setNumAchievementsAwarded(data.getNumAchievementsAwarded());
			});
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
		}
	}

	public void getConsoleGames(final int consoleId) {
		Log.info("Getting console games for console id " + consoleId);
		final String resBody = requestData(CONSOLE_GAMES_METHOD, "i=" + Integer.toString(consoleId)).body();
		LoggingUtils.prettyPrint(mapper, resBody);
		try {
			final List<GameData> gameData = mapper.readValue(resBody, new TypeReference<List<GameData>>() {
			});
			// Add game data to console data
			final ConsoleData consoleData = model.getConsoleDataMap().get(consoleId);
			gameData.forEach(data -> {
				consoleData.getGameDataMap().put(data.getId(), data);
			});
		} catch (JsonProcessingException e) {
			Log.error("Error reading response body as GameData", e);
		}
	}

	public void getAllConsoleGames() {
		Log.info("Getting all console games");
		// for (final int consoleId : model.getConsoleDataMap().keySet()) {
		// TODO
		// }
		if (!model.getConsoleDataMap().containsKey(69)) {
			getConsoleIds();
		}

		if (model.getConsoleDataMap().get(69).getGameDataMap().isEmpty()) {
			getConsoleGames(69);
		}

		// Get Back GameData and create GameDataFront
		final List<GameDataFront> gameDataFrontList = new ArrayList<>();
		model.getConsoleDataMap().get(69).getGameDataMap().forEach((key, value) -> {
			gameDataFrontList.add(new GameDataFront(value));
		});
		try {
			gamesSocketEndpoint.sendStringDataBroadcast(mapper.writeValueAsString(gameDataFrontList));
		} catch (JsonProcessingException e) {
			Log.error("Cannot parse GameDataFront data", e);
		}
	}
}