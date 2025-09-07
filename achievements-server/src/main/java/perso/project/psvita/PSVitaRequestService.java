package perso.project.psvita;

import java.nio.file.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractStandaloneRequestService;

@ApplicationScoped
public class PSVitaRequestService extends AbstractStandaloneRequestService {

	@Inject
	@ConfigProperty(name = "psvita.games.path")
	private Path psVitaGamesPath;

	@Inject
	@ConfigProperty(name = "psvita.beaten.path")
	private Path psVitaBeatenPath;

	@Inject
	@ConfigProperty(name = "psvita.mastered.path")
	private Path psVitaMasteredPath;

	@Override
	protected Path getGamesPath() {
		return psVitaGamesPath;
	}

	@Override
	protected Path getGamesBeatenPath() {
		return psVitaBeatenPath;
	}

	@Override
	protected Path getGamesMasteredPath() {
		return psVitaMasteredPath;
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.PSVITA;
	}

	@Override
	protected int getId() {
		return Model.PSVITA_CONSOLE_ID;
	}

}
