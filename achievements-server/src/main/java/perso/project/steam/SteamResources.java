package perso.project.steam;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.MainModel;

@Path("/steam")
public class SteamResources {
	@Inject
	SteamRequestService steamRequestService;

	@Inject
	MainModel model;

	@GET
	@Path("/all_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllData() {
		steamRequestService.getAllData();
		return "{}";
	}
}
