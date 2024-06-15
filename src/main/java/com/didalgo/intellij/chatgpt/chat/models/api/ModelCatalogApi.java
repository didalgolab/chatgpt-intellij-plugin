/*
 * Copyright (c) 2024 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.chat.models.api;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.didalgo.intellij.chatgpt.chat.models.CustomModel;
import com.didalgo.intellij.chatgpt.chat.models.ModelFamily;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.openapi.components.Service;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

/**
 * Client for retrieving available models from a model catalog.
 *
 * @author Christian Tzolov
 */
@Service
public final class ModelCatalogApi {

	private static final List<String> MODEL_NAME_BLACKLIST = List.of(
			"-embed", "gemini-pro", "gemini-1.0-pro", "babbage-", "davinci-", "dall-e-", "tts-", "whisper-");

	private final RestClient restClient;

	/**
	 * Constructs a new {@code ModelCatalogClient} with the specified base URL and API token.
	 *
	 */
	@ConstructorBinding
	public ModelCatalogApi() {
		this.restClient = RestClient.builder()
				.defaultHeaders(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
				.defaultStatusHandler(RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER)
				.build();
	}

	/**
	 * Retrieves a list of available models from the model catalog.
	 *
	 * @return the list of available models
	 */
	public List<CustomModel> getAvailableModels(ModelFamily family, String baseUrl, String apiKey) {
		return this.restClient.get()
				.uri(baseUrl, getApiEndpointSpec(family))
				.headers(getRequiredApiHeaders(family, apiKey))
				.retrieve()
				.toEntity(getResponseBodyType(family))
				.getBody()
				.toList(family);
	}

	private Function<UriBuilder, URI> getApiEndpointSpec(ModelFamily family) {
		String path;
		if (family == ModelFamily.GEMINI) {
			path = "/v1beta/models";
		} else if (family == ModelFamily.OLLAMA) {
			path = "/api/tags";
		} else {
			path = "/v1/models";
		}

		return uriBuilder -> uriBuilder
				.path(path)
				.build();
	}

	private Consumer<HttpHeaders> getRequiredApiHeaders(ModelFamily family, String apiKey) {
		return headers -> {
			if (apiKey != null) {
				if (family == ModelFamily.GEMINI) {
					headers.add("x-goog-api-key", apiKey);
				} else {
					headers.setBearerAuth(apiKey);
				}
			}
			headers.setContentType(MediaType.APPLICATION_JSON);
		};
	}

	private Class<? extends ModelCatalogResponse> getResponseBodyType(ModelFamily family) {
		if (family == ModelFamily.GEMINI) {
			return GeminiModelCatalogResponse.class;
		} else if (family == ModelFamily.OLLAMA) {
			return OllamaModelCatalogResponse.class;
		} else {
			return OpenAiCompatibleModelCatalogResponse.class;
		}
	}

	private static Predicate<CustomModel> modelNotBlacklisted() {
		return model -> MODEL_NAME_BLACKLIST.stream().noneMatch(bl -> model.id().contains(bl));
	}

	interface ModelCatalogResponse {

		List<CustomModel> toList(ModelFamily family);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record OpenAiCompatibleModelCatalogResponse(
			@JsonProperty("data") List<ModelData> data)
			implements ModelCatalogResponse {

		@Override
		public List<CustomModel> toList(ModelFamily family) {
			return data.stream()
					.map(data -> {
						int inputTokenLimit = (data.inputTokenLimit() == null) ? Integer.MAX_VALUE : data.inputTokenLimit();
						return new CustomModel(data.id(), family, inputTokenLimit);
					})
					.filter(modelNotBlacklisted())
					.sorted(Comparator.comparing(CustomModel::id))
					.toList();
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ModelData(@JsonProperty("id") String id, @JsonProperty("context_length") Integer inputTokenLimit) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GeminiModelCatalogResponse(
			@JsonProperty("models") List<GeminiModelData> models)
			implements ModelCatalogResponse {

		@Override
		public List<CustomModel> toList(ModelFamily family) {
			return models.stream()
					.filter(data -> data.supportedGenerationMethods().contains("generateContent"))
					.map(data -> {
						var name = (data.name().startsWith("models/")) ? data.name().substring(7) : data.name();
						int inputTokenLimit = (data.inputTokenLimit() == null) ? Integer.MAX_VALUE : data.inputTokenLimit();
						return new CustomModel(name, family, inputTokenLimit);
					})
					.filter(modelNotBlacklisted())
					.sorted(Comparator.comparing(CustomModel::id))
					.toList();
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GeminiModelData(@JsonProperty("name") String name,
								   @JsonProperty("inputTokenLimit") Integer inputTokenLimit,
								   @JsonProperty("supportedGenerationMethods") List<String> supportedGenerationMethods) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record OllamaModelCatalogResponse(
			@JsonProperty("models") List<OllamaModelData> models)
			implements ModelCatalogResponse {

		@Override
		public List<CustomModel> toList(ModelFamily family) {
			return models.stream()
					.map(data -> new CustomModel(data.name(), family, Integer.MAX_VALUE))
					.sorted(Comparator.comparing(CustomModel::id))
					.toList();
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record OllamaModelData(@JsonProperty("name") String name) {
	}
}
