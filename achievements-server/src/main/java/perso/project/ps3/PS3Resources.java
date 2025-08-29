package perso.project.ps3;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.CompareData;
import perso.project.model.ConsoleData;
import perso.project.model.GameData;
import perso.project.model.MainModel;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.playnite.PlayniteService;

@Path("/ps3")
public class PS3Resources {
	@Inject
	PS3RequestService ps3RequestService;

	@Inject
	PS3CompareService ps3CompareService;

	@Inject
	MainModel model;

	@Inject
	PlayniteService playniteService;

	@Inject
	@ConfigProperty(name = "playnite.data.path")
	private java.nio.file.Path playniteDataPath;

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

	@GET
	@Path("/compare_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCompareData() throws JsonProcessingException {
		playniteService.getPlayniteData(playniteDataPath);
		final List<CompareData> data = ps3CompareService.getCompareData();
		Log.info("Returning PS3 " + data.size() + " compare data");
		return ps3RequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/existing_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExistingData() throws JsonProcessingException {
		final List<GameData> data = model.getGameDataForSources(List.of(ConsoleSourceEnum.PS3));
		Log.info("Returning " + data.size() + " existing PS3 games");
		return ps3RequestService.getMapper().writeValueAsString(data);
	}
}
