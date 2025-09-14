package perso.project.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.inject.Inject;
import perso.project.model.MainModel;

public abstract class AbstractRequestService {
	protected ObjectMapper mapper;

	@Inject
	protected MainModel model;

	protected AbstractRequestService() {
		setupMapper();
	}

	ObjectMapper setupMapper() {
		mapper = new ObjectMapper();

		final JavaTimeModule module = new JavaTimeModule();
		mapper.registerModule(module);

		return mapper;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}
}
