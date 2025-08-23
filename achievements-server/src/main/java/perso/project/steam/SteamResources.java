package perso.project.steam;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.ConsoleData;
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
	@Path("/all_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllData() {
		steamRequestService.getAllData();
		return "{}";
	}
}
