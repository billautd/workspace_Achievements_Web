package perso.project.steam;

import java.util.Collection;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.GameData;
import perso.project.model.MainModel;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractResources;

@Path("/steam")
public class SteamResources extends AbstractResources {
	@Inject
	SteamRequestService steamRequestService;

	@Inject
	MainModel model;

	@Inject
	SteamCompareService steamCompareService;

	@Inject
	@ConfigProperty(name = "steam.database.path")
	private java.nio.file.Path steamDatabasePath;

	@GET
	@Path("/console_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleIds() throws JsonProcessingException {
		return getConsoleIds(steamRequestService);
	}

	@GET
	@Path("/owned_games")
	@Produces(MediaType.TEXT_PLAIN)
	public String getOwnedGames() throws JsonProcessingException {
		steamRequestService.getOwnedGames();
		steamRequestService.getLocalData();

		final Collection<GameData> data = model.getConsoleDataMap().get(Model.STEAM_CONSOLE_ID).getGameDataMap()
				.values();
		Log.info("Returning " + data.size() + " Steam owned games");
		return steamRequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/game_data/{game_id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getGameData(@PathParam("game_id") final int gameId) throws JsonProcessingException {
		final GameData data = steamRequestService.getSimpleGameData(gameId);
		Log.info("Returning Steam data for game " + data.getTitle() + " (" + gameId + ")");
		return steamRequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/full_game_data/{game_id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getFullGameData(@PathParam("game_id") final int gameId) throws JsonProcessingException {
		final GameData data = steamRequestService.getFullGameData(gameId);
		Log.info("Returning Steam data for game " + data.getTitle() + " (" + gameId + ")");
		return steamRequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/write_database")
	@Produces(MediaType.TEXT_PLAIN)
	public String getWriteDatabase() throws JsonProcessingException {
		return writeDatabase(steamDatabasePath);
	}

	@GET
	@Path("/compare_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCompareData() throws JsonProcessingException {
		return getCompareData(steamRequestService, steamCompareService);
	}

	@GET
	@Path("/existing_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExistingData() throws JsonProcessingException {
		return getExistingData(steamRequestService, steamDatabasePath);
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.STEAM;
	}
}
