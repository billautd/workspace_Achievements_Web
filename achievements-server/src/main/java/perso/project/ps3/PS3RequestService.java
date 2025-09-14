package perso.project.ps3;

import java.nio.file.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractPSNRequestService;

@ApplicationScoped
public class PS3RequestService extends AbstractPSNRequestService {

	@Inject
	@ConfigProperty(name = "ps3.html.folder.path")
	private Path ps3HTMLPath;

	@Inject
	@ConfigProperty(name = "ps3.beaten.path")
	private Path ps3BeatenPath;

	@Inject
	@ConfigProperty(name = "ps3.mastered.path")
	private Path ps3MasteredPath;

	@Override
	protected Path getHTMLPath() {
		return ps3HTMLPath;
	}

	@Override
	protected Path getGamesBeatenPath() {
		return ps3BeatenPath;
	}

	@Override
	protected Path getGamesMasteredPath() {
		return ps3MasteredPath;
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.PS3;
	}

	@Override
	protected int getId() {
		return Model.PS3_CONSOLE_ID;
	}
}
