package perso.project.steam;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.MainModel;

@Path("/steam")
public class SteamResources {
	@Inject
	SteamRequestService steamRequestService;

	@Inject
	MainModel model;

	@GET
	@Path("/console_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleIds() throws JsonProcessingException {
		final List<ConsoleData> data = steamRequestService.getConsoleIds();
		Log.info("Returning Steam console data");
		return steamRequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/owned_games")
	@Produces(MediaType.TEXT_PLAIN)
	public String getOwnedGames() throws JsonProcessingException {
		final List<GameData> data = steamRequestService.getOwnedGames();
		Log.info("Returning " + data.size() + " Steam owned games");
		steamRequestService.getSteamGames_Beaten("C:\\Users\\dbill\\Downloads\\SteamBeaten.xlsx");
		steamRequestService.getSteamGames_Mastered("C:\\Users\\dbill\\Downloads\\SteamMastered.xlsx");
		return steamRequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/game_data/{game_id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getGameData(@PathParam("game_id") final int gameId) throws JsonProcessingException {
		final GameData data = steamRequestService.getAchievements(gameId);
		Log.info("Returning Steam data for game " + data.getTitle() + " (" + gameId + ")");
		return steamRequestService.getMapper().writeValueAsString(data);
	}
}
