package perso.project.ps3;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.MainModel;

@Path("/ps3")
public class PS3Resources {
	@Inject
	PS3RequestService ps3RequestService;

	@Inject
	MainModel model;

	@GET
	@Path("/all_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllData() {
		ps3RequestService.getAllData();
		return "{}";
	}
}
