package perso.project.psvita;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.MainModel;

@Path("/psvita")
public class PSVitaResources {
	@Inject
	PSVitaRequestService psVitaRequestService;

	@Inject
	MainModel model;

	@GET
	@Path("/console_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleIds() throws JsonProcessingException {
		final List<ConsoleData> data = psVitaRequestService.getConsoleIds();
		Log.info("Returning PSVita console data");
		return psVitaRequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/game_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllData() throws JsonProcessingException {
		final List<GameData> data = psVitaRequestService.getAllData();
		Log.info("Returning " + data.size() + " PSVita games");
		return psVitaRequestService.getMapper().writeValueAsString(data);
	}
}
