package perso.project.ps3;

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

@Path("/ps3")
public class PS3Resources {
	@Inject
	PS3RequestService ps3RequestService;

	@Inject
	MainModel model;

	@GET
	@Path("/console_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleIds() throws JsonProcessingException {
		final List<ConsoleData> data = ps3RequestService.getConsoleIds();
		Log.info("Returning PS3 console data");
		return ps3RequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/game_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllData() throws JsonProcessingException {
		final List<GameData> data = ps3RequestService.getAllData();
		Log.info("Returning " + data.size() + " PS3 games");
		return ps3RequestService.getMapper().writeValueAsString(data);
	}
}
