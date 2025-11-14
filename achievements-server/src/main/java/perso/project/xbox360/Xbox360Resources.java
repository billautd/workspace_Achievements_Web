package perso.project.xbox360;

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

@Path("/xbox360")
public class Xbox360Resources extends AbstractStandaloneResources {
	@Inject
	Xbox360RequestService xbox360RequestService;

	@Inject
	Xbox360CompareService xbox360CompareService;

	@Inject
	@ConfigProperty(name = "xbox360.database.path")
	private java.nio.file.Path xbox360DatabasePath;

	@GET
	@Path("/console_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleIds() throws JsonProcessingException {
		return getConsoleIds(xbox360RequestService);
	}

	@GET
	@Path("/game_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllData() throws JsonProcessingException {
		return getAllData(xbox360RequestService, xbox360DatabasePath);
	}

	@GET
	@Path("/compare_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCompareData() throws JsonProcessingException {
		return getCompareData(xbox360RequestService, xbox360CompareService);
	}

	@GET
	@Path("/existing_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExistingData() throws JsonProcessingException {
		return getExistingData(xbox360RequestService, xbox360DatabasePath);
	}

	@GET
	@Path("/write_database")
	@Produces(MediaType.TEXT_PLAIN)
	public String getWriteDatabase() throws JsonProcessingException {
		return writeDatabase(xbox360DatabasePath);
	}

	@GET
	@Path("/full_game_data/{game_id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getFullGameData(@PathParam("game_id") final int gameId) throws JsonProcessingException {
		final GameData data = xbox360RequestService.getFullGameData(gameId);
		Log.info("Returning Xbox 360 data for game " + data.getTitle() + " (" + gameId + ")");
		return xbox360RequestService.getMapper().writeValueAsString(data);
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.XBOX_360;
	}
}
