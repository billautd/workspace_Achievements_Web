package perso.project.ps3;

import jakarta.enterprise.context.ApplicationScoped;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractCompareService;

@ApplicationScoped
public class PS3CompareService extends AbstractCompareService {

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.PS3;
	}

	@Override
	protected int getId() {
		return Model.PS3_CONSOLE_ID;
	}
}
