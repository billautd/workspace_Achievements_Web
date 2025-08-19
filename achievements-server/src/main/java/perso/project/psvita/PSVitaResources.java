package perso.project.psvita;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.MainModel;

@Path("/psvita")
public class PSVitaResources {
	@Inject
	PSVitaRequestService psVitaRequestService;

	@Inject
	MainModel model;

	@GET
	@Path("/all_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllData() {
		psVitaRequestService.getAllData();
		return "{}";
	}
}
