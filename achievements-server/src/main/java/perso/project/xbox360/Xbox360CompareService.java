package perso.project.xbox360;

import jakarta.enterprise.context.ApplicationScoped;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.standalone.AbstractStandaloneCompareService;

@ApplicationScoped
public class Xbox360CompareService extends AbstractStandaloneCompareService {

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.XBOX_360;
	}

	@Override
	protected int getId() {
		return Model.XBOX360_CONSOLE_ID;
	}

}
