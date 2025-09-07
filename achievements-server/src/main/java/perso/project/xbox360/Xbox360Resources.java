package perso.project.xbox360;

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

@Path("/xbox360")
public class Xbox360Resources {
	@Inject
	Xbox360RequestService xbox360RequestService;

	@Inject
	Xbox360CompareService xbox360CompareService;

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
		final List<ConsoleData> data = xbox360RequestService.getConsoleIds();
		Log.info("Returning Xbox 360 console data");
		return xbox360RequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/game_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllData() throws JsonProcessingException {
		final List<GameData> data = xbox360RequestService.getAllData();
		Log.info("Returning " + data.size() + " Xbox 360 games");
		return xbox360RequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/compare_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCompareData() throws JsonProcessingException {
		playniteService.getPlayniteData(playniteDataPath);
		final List<CompareData> data = xbox360CompareService.getCompareData();
		Log.info("Returning Xbox 360 " + data.size() + " compare data");
		return xbox360RequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/existing_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExistingData() throws JsonProcessingException {
		final List<GameData> data = model.getGameDataForSources(List.of(ConsoleSourceEnum.XBOX_360));
		Log.info("Returning " + data.size() + " existing Xbox 360 games");
		return xbox360RequestService.getMapper().writeValueAsString(data);
	}
}
