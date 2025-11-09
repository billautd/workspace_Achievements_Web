package perso.project.ra;

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
import perso.project.model.GameData;
import perso.project.model.MainModel;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractResources;

@Path("/ra")
public class RetroAchievementsResources extends AbstractResources {
	@Inject
	RetroAchievementsRequestService raRequestService;

	@Inject
	RetroAchievementsCompareService raCompareService;

	@Inject
	MainModel model;

	@Inject
	@ConfigProperty(name = "ra.database.path")
	private java.nio.file.Path raDatabasePath;

	@GET
	@Path("/console_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleIds() throws JsonProcessingException {
		return getConsoleIds(raRequestService);
	}

	@GET
	@Path("/completion_progress")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCompletionProgress() throws JsonProcessingException {
		final List<GameData> data = raRequestService.getUserCompletionProgress();
		Log.info("Returning RetroAchievements completion progress for " + data.size() + " games");
		return raRequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/game_data/{console_id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getGameData(@PathParam("console_id") final int consoleId) throws JsonProcessingException {
		final List<GameData> data = raRequestService.getConsoleGames(consoleId);
		Log.info("Returning RetroAchievements " + data.size() + " games for console " + consoleId);
		return raRequestService.getMapper()
				.writeValueAsString(model.getConsoleDataMap().get(consoleId).getGameDataMap().values());
	}

	@GET
	@Path("/compare_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCompareData() throws JsonProcessingException {
		return getCompareData(raRequestService, raCompareService);
	}

	@GET
	@Path("/existing_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExistingData() throws JsonProcessingException {
		return getExistingData(raRequestService, raDatabasePath);
	}

	@GET
	@Path("/full_game_data/{game_id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getFullGameData(@PathParam("game_id") final int gameId) throws JsonProcessingException {
		final GameData data = raRequestService.getFullGameData(gameId);
		Log.info("Returning Steam data for game " + data.getTitle() + " (" + gameId + ")");
		return raRequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/write_database")
	@Produces(MediaType.TEXT_PLAIN)
	public String getWriteDatabase() throws JsonProcessingException {
		return writeDatabase(raDatabasePath);
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.RETRO_ACHIEVEMENTS;
	}
}
