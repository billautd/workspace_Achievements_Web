package perso.project.ra;

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

@Path("/ra")
public class RetroAchievementsResources {
	@Inject
	RetroAchievementsRequestService raRequestService;

	@Inject
	MainModel model;

	@GET
	@Path("/console_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleIds() throws JsonProcessingException {
		final List<ConsoleData> data = raRequestService.getConsoleIds();
		Log.info("Returning RetroAchievements data for " + data.size() + " consoles");
		return raRequestService.getMapper().writeValueAsString(data);
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
	public String getConsoleIds(@PathParam("console_id") final int consoleId) throws JsonProcessingException {
		final List<GameData> data = raRequestService.getConsoleGames(consoleId);
		Log.info("Returning RetroAchievements " + data.size() + " games for console " + consoleId);
		return raRequestService.getMapper()
				.writeValueAsString(model.getConsoleDataMap().get(consoleId).getGameDataMap().values());
	}
}
