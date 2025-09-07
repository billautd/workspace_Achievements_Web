package perso.project.xbox360;

import java.nio.file.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractStandaloneRequestService;

@ApplicationScoped
public class Xbox360RequestService extends AbstractStandaloneRequestService {

	@Inject
	@ConfigProperty(name = "xbox360.games.path")
	private Path xbox360GamesPath;

	@Inject
	@ConfigProperty(name = "xbox360.beaten.path")
	private Path xbox360BeatenPath;

	@Inject
	@ConfigProperty(name = "xbox360.mastered.path")
	private Path xbox360MasteredPath;

	@Override
	protected Path getGamesPath() {
		return xbox360GamesPath;
	}

	@Override
	protected Path getGamesBeatenPath() {
		return xbox360BeatenPath;
	}

	@Override
	protected Path getGamesMasteredPath() {
		return xbox360MasteredPath;
	}

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.XBOX_360;
	}

	@Override
	protected int getId() {
		return Model.XBOX360_CONSOLE_ID;
	}

}
