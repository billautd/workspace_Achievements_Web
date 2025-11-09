package perso.project.standalone;

import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import perso.project.model.GameData;
import perso.project.utils.AbstractResources;
import perso.project.utils.DatabaseService;

public abstract class AbstractStandaloneResources extends AbstractResources {
	@Inject
	DatabaseService databaseService;

	protected String getAllData(final AbstractStandaloneRequestService requestService, final Path databasePath)
			throws JsonProcessingException {
		final List<GameData> data = requestService.getAllData();
		Log.info("Returning " + data.size() + " " + getSource() + " games");
		return requestService.getMapper().writeValueAsString(data);
	}
}
