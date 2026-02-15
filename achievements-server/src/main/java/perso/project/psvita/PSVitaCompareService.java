package perso.project.psvita;

import jakarta.enterprise.context.ApplicationScoped;
import perso.project.model.Model;
import perso.project.model.enums.ConsoleSourceEnum;
import perso.project.utils.AbstractCompareService;

@ApplicationScoped
public class PSVitaCompareService extends AbstractCompareService {

	@Override
	protected ConsoleSourceEnum getSource() {
		return ConsoleSourceEnum.PSVITA;
	}

	@Override
	protected int getId() {
		return Model.PSVITA_CONSOLE_ID;
	}
}
