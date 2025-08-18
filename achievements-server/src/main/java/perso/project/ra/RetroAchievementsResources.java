package perso.project.ra;

import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.back.MainModel;

@Path("/ra")
public class RetroAchievementsResources {
	@Inject
	RetroAchievementsRequestService raRequestService;

	@Inject
	MainModel model;

	@GET
	@Path("/console_ids")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleIds() {
		raRequestService.getConsoleIds();
		return model.getConsoleDataMap().size() + " consoles. \nIds are " + String.join(", ", model.getConsoleDataMap()
				.keySet().stream().map(id -> Integer.toString(id)).collect(Collectors.toList()));
	}

	@GET
	@Path("/console_games/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleGames(@PathParam("id") int consoleId) {
		raRequestService.getConsoleGames(consoleId);
		return "Console id " + consoleId + " : " + model.getConsoleDataMap().get(consoleId).getGameDataMap().size()
				+ " games";
	}

	@GET
	@Path("/all_console_games")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllConsoleGames() {
		raRequestService.getAllConsoleGames();
		return "{}";
	}

	@GET
	@Path("/user_awards")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserAwards() {
		raRequestService.getUserAwards();
		return "{}";
	}

	@GET
	@Path("/user_completed_games")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserCompletedGames() {
		raRequestService.getUserCompletedGames();
		return "{}";
	}
}
