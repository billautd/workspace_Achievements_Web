package perso.project.psvita;

import java.nio.file.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractPSNRequestService;

@ApplicationScoped
public class PSVitaRequestService extends AbstractPSNRequestService {

	@Inject
	@ConfigProperty(name = "psvita.html.folder.path")
	private Path psVitaHTMLPath;

	@Inject
	@ConfigProperty(name = "psvita.beaten.path")
	private Path psVitaBeatenPath;

	@Inject
	@ConfigProperty(name = "psvita.mastered.path")
	private Path psVitaMasteredPath;

	@Override
	protected Path getHTMLPath() {
		return psVitaHTMLPath;
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
