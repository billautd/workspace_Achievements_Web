package perso.project.psvita;

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

@Path("/psvita")
public class PSVitaResources {
	@Inject
	PSVitaRequestService psVitaRequestService;

	@Inject
	PSVitaCompareService psVitaCompareService;

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

	@GET
	@Path("/compare_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCompareData() throws JsonProcessingException {
		playniteService.getPlayniteData(playniteDataPath);
		final List<CompareData> data = psVitaCompareService.getCompareData();
		Log.info("Returning PSVita " + data.size() + " compare data");
		return psVitaRequestService.getMapper().writeValueAsString(data);
	}

	@GET
	@Path("/existing_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExistingData() throws JsonProcessingException {
		final List<GameData> data = model.getGameDataForSources(List.of(ConsoleSourceEnum.PSVITA));
		Log.info("Returning " + data.size() + " existing PSVita games");
		return psVitaRequestService.getMapper().writeValueAsString(data);
	}
}
