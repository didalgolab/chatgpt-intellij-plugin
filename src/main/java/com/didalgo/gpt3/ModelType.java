/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.gpt3;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * ModelType represents a list of available OpenAI GPT models, also providing information about
 * their maximum token size and encoding types.
 *
 * @author Mariusz Bernacki
 */
public enum ModelType {
	// chat
	GPT_4("gpt-4", EncodingType.CL100K_BASE, 8192),
	GPT_4_32K("gpt-4", EncodingType.CL100K_BASE, 32768),
	GPT_3_5_TURBO("gpt-3.5-turbo", EncodingType.CL100K_BASE, 4096),

	// text
	TEXT_DAVINCI_003("text-davinci-003", EncodingType.P50K_BASE, 4097),
	TEXT_DAVINCI_002("text-davinci-002", EncodingType.P50K_BASE, 4097),
	TEXT_DAVINCI_001("text-davinci-001", EncodingType.R50K_BASE, 2049),
	TEXT_CURIE_001("text-curie-001", EncodingType.R50K_BASE, 2049),
	TEXT_BABBAGE_001("text-babbage-001", EncodingType.R50K_BASE, 2049),
	TEXT_ADA_001("text-ada-001", EncodingType.R50K_BASE, 2049),
	DAVINCI("davinci", EncodingType.R50K_BASE, 2049),
	CURIE("curie", EncodingType.R50K_BASE, 2049),
	BABBAGE("babbage", EncodingType.R50K_BASE, 2049),
	ADA("ada", EncodingType.R50K_BASE, 2049),

	// code
	CODE_DAVINCI_002("code-davinci-002", EncodingType.P50K_BASE, 8001),

	// edit
	TEXT_DAVINCI_EDIT_001("text-davinci-edit-001", EncodingType.P50K_EDIT, 2049),
	CODE_DAVINCI_EDIT_001("code-davinci-edit-001", EncodingType.P50K_EDIT, 2049),

	// embeddings
	TEXT_EMBEDDING_ADA_002("text-embedding-ada-002", EncodingType.CL100K_BASE, 8192);


	private final String modelName;
	private final EncodingType encodingType;
	private final int maxTokens;

	ModelType(String modelName, EncodingType encodingType, int maxTokens) {
		this.modelName = modelName;
		this.encodingType = encodingType;
		this.maxTokens = maxTokens;
	}

	public String modelName() {
		return modelName;
	}

	public EncodingType encodingType() {
		return encodingType;
	}

	public int maxTokens() {
		return maxTokens;
	}

	/**
	 * Returns a {@link ModelType} for the given modelName, or {@link Optional#empty()} if no
	 * such model type exists.
	 *
	 * @param modelName the modelName of the model type
	 * @return the model type or {@link Optional#empty()}
	 */
	public static Optional<ModelType> forModel(final String modelName) {
		for (final ModelType modelType : values()) {
			if (modelType.modelName().equals(modelName)) {
				return Optional.of(modelType);
			}
		}
		return Optional.empty();
	}

	private static final class Cache {

		private static final Map<ModelType, SoftReference<GPT3Tokenizer>> gptTokenizersCache = Collections.synchronizedMap(new EnumMap<>(ModelType.class));

		private static GPT3Tokenizer getTokenizer(ModelType model) {
			GPT3Tokenizer tokenizer;
			SoftReference<GPT3Tokenizer> ref = Cache.gptTokenizersCache.get(model);
			if (ref == null || (tokenizer = ref.get()) == null)
				Cache.gptTokenizersCache.put(model, new SoftReference<>(tokenizer = new GPT3Tokenizer(model.getEncoding())));

			return tokenizer;
		}
	}

	public Encoding getEncoding() {
		return Encoding.forName(encodingType().encodingName());
	}

	public GPT3Tokenizer getTokenizer() {
		return Cache.getTokenizer(this);
	}

	public ChatFormatDescriptor getChatFormatDescriptor() {
		return ChatFormatDescriptor.forModel(modelName());
	}
}
