package perso.project.psvita;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractStandaloneResources;

@Path("/psvita")
public class PSVitaResources extends AbstractStandaloneResources {
	@Inject
	PSVitaRequestService psVitaRequestService;

	@Inject
	PSVitaCompareService psVitaCompareService;

	@Inject
	@ConfigProperty(name = "psvita.database.path")
	private java.nio.file.Path psVitaDatabasePath;

	@GET
	@Path("/console_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConsoleIds() throws JsonProcessingException {
		return getConsoleIds(psVitaRequestService);
	}

	@GET
	@Path("/game_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllData() throws JsonProcessingException {
		return getAllData(psVitaRequestService, psVitaDatabasePath);
	}

	@GET
	@Path("/compare_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCompareData() throws JsonProcessingException {
		return getCompareData(psVitaRequestService, psVitaCompareService);
	}

	@GET
	@Path("/existing_data")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExistingData() throws JsonProcessingException {
		return getExistingData(psVitaRequestService, psVitaDatabasePath);
	}

	@GET
	@Path("/write_database")
	@Produces(MediaType.TEXT_PLAIN)
	public String getWriteDatabase() throws JsonProcessingException {
		return writeDatabase(psVitaDatabasePath);
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.PSVITA;
	}
}
