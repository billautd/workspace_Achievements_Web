package perso.project.steam;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.CompareData;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.MainModel;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.playnite.PlayniteService;

@Path("/steam")
public class SteamResources {
	@Inject
	SteamRequestService steamRequestService;

	@Inject
	MainModel model;

	@Inject
	SteamCompareService steamCompareService;

	@Inject
	PlayniteService playniteService;

	@Inject
	@ConfigProperty(name = "playnite.data.path")
	private java.nio.file.Path playniteDataPath;

	@Inject
	@ConfigProperty(name = "steam.beaten.path")
	private java.nio.file.Path steamBeatenPath;

	@Inject
	@ConfigProperty(name = "steam.mastered.path")
	private java.nio.file.Path steamMasteredPath;

	@Inject
	@ConfigProperty(name = "steam.removed.path")
	private java.nio.file.Path steamRemovedPath;

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
		steamRequestService.getSteamGames_Beaten(steamBeatenPath);
		steamRequestService.getSteamGames_Mastered(steamMasteredPath);
		steamRequestService.getSteamGames_Removed(steamRemovedPath);
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

	@GET
	@Path("/compare_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCompareData() throws JsonProcessingException {
		playniteService.getPlayniteData(playniteDataPath);
		steamRequestService.getSteamGames_Beaten(steamBeatenPath);
		steamRequestService.getSteamGames_Mastered(steamMasteredPath);
		steamRequestService.getSteamGames_Removed(steamRemovedPath);
		final List<CompareData> data = steamCompareService.getCompareData();
		Log.info("Returning Steam " + data.size() + " compare data");
		return steamRequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/existing_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExistingData() throws JsonProcessingException {
		final List<GameData> data = model.getGameDataForSources(List.of(ConsoleSourceEnum.STEAM));
		Log.info("Returning " + data.size() + " existing Steam games");
		return steamRequestService.getMapper().writeValueAsString(data);
	}
}
