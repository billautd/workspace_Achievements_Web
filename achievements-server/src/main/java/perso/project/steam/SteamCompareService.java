package perso.project.steam;

import jakarta.enterprise.context.ApplicationScoped;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractCompareService;

@ApplicationScoped
public class SteamCompareService extends AbstractCompareService {
	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.STEAM;
	}

	@Override
	protected int getId() {
		return Model.STEAM_CONSOLE_ID;
	}
}
