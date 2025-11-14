package perso.project.ps3;

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
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractStandaloneResources;

@Path("/ps3")
public class PS3Resources extends AbstractStandaloneResources {
	@Inject
	PS3RequestService ps3RequestService;

	@Inject
	PS3CompareService ps3CompareService;

	@Inject
	@ConfigProperty(name = "ps3.database.path")
	private java.nio.file.Path ps3DatabasePath;

	@GET
	@Path("/console_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleIds() throws JsonProcessingException {
		return getConsoleIds(ps3RequestService);
	}

	@GET
	@Path("/game_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllData() throws JsonProcessingException {
		return getAllData(ps3RequestService, ps3DatabasePath);
	}

	@GET
	@Path("/compare_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCompareData() throws JsonProcessingException {
		return getCompareData(ps3RequestService, ps3CompareService);
	}

	@GET
	@Path("/existing_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExistingData() throws JsonProcessingException {
		return getExistingData(ps3RequestService, ps3DatabasePath);
	}

	@GET
	@Path("/write_database")
	@Produces(MediaType.TEXT_PLAIN)
	public String getWriteDatabase() throws JsonProcessingException {
		return writeDatabase(ps3DatabasePath);
	}

	@GET
	@Path("/full_game_data/{game_id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getFullGameData(@PathParam("game_id") final int gameId) throws JsonProcessingException {
		final GameData data = ps3RequestService.getFullGameData(gameId);
		Log.info("Returning PS3 data for game " + data.getTitle() + " (" + gameId + ")");
		return ps3RequestService.getMapper().writeValueAsString(data);
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.PS3;
	}
}
