package perso.project.ra;

import jakarta.enterprise.context.ApplicationScoped;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractCompareService;

@ApplicationScoped
public class RetroAchievementsCompareService extends AbstractCompareService {
	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.RETRO_ACHIEVEMENTS;
	}
}
