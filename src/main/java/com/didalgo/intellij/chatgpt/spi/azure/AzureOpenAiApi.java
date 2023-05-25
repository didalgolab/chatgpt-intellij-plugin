/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.spi.azure;

import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.edit.EditResult;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.engine.Engine;
import com.theokanning.openai.file.File;
import com.theokanning.openai.finetune.FineTuneEvent;
import com.theokanning.openai.finetune.FineTuneRequest;
import com.theokanning.openai.finetune.FineTuneResult;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.model.Model;
import com.theokanning.openai.moderation.ModerationRequest;
import com.theokanning.openai.moderation.ModerationResult;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface AzureOpenAiApi extends OpenAiApi {

    @GET("/openai/models")
    Single<OpenAiResponse<Model>> listModels();

    @GET("/openai/models/{model_id}")
    Single<Model> getModel(@Path("model_id") String modelId);

    @POST("/openai/deployments/{deployment_id}/completions")
    Single<CompletionResult> createCompletion(@Body CompletionRequest request);

    @Streaming
    @POST("/openai/deployments/{deployment_id}/completions")
    Call<ResponseBody> createCompletionStream(@Body CompletionRequest request);

    @POST("/openai/deployments/{deployment_id}/chat/completions")
    Single<ChatCompletionResult> createChatCompletion(@Body ChatCompletionRequest request);

    @Streaming
    @POST("/openai/deployments/{deployment_id}/chat/completions")
    Call<ResponseBody> createChatCompletionStream(@Body ChatCompletionRequest request);

    @Deprecated
    default Single<CompletionResult> createCompletion(@Path("engine_id") String engineId, @Body CompletionRequest request) {
        throw new UnsupportedOperationException("This method is no longer available.");
    }

    default Single<EditResult> createEdit(@Body EditRequest request) {
        throw new UnsupportedOperationException("This method is not available.");
    }

    @Deprecated
    @POST("/v1/engines/{engine_id}/edits")
    default Single<EditResult> createEdit(@Path("engine_id") String engineId, @Body EditRequest request) {
        throw new UnsupportedOperationException("This method is no longer available.");
    }

    @POST("/openai/deployments/{deployment_id}/embeddings")
    Single<EmbeddingResult> createEmbeddings(@Body EmbeddingRequest request);

    @Deprecated
    default Single<EmbeddingResult> createEmbeddings(@Path("engine_id") String engineId, @Body EmbeddingRequest request) {
        throw new UnsupportedOperationException("This method is no longer available.");
    }

    @GET("/openai/files")
    Single<OpenAiResponse<File>> listFiles();

    @Multipart
    @POST("/openai/files")
    Single<File> uploadFile(@Part("purpose") RequestBody purpose, @Part MultipartBody.Part file);

    @DELETE("/openai/files/{file_id}")
    Single<DeleteResult> deleteFile(@Path("file_id") String fileId);

    @GET("/openai/files/{file_id}")
    Single<File> retrieveFile(@Path("file_id") String fileId);

    @POST("/openai/fine-tunes")
    Single<FineTuneResult> createFineTune(@Body FineTuneRequest request);

    @POST("/v1/completions")
    Single<CompletionResult> createFineTuneCompletion(@Body CompletionRequest request);

    @GET("/openai/fine-tunes")
    Single<OpenAiResponse<FineTuneResult>> listFineTunes();

    @GET("/openai/fine-tunes/{fine_tune_id}")
    Single<FineTuneResult> retrieveFineTune(@Path("fine_tune_id") String fineTuneId);

    @POST("/openai/fine-tunes/{fine_tune_id}/cancel")
    Single<FineTuneResult> cancelFineTune(@Path("fine_tune_id") String fineTuneId);

    @GET("/openai/fine-tunes/{fine_tune_id}/events")
    Single<OpenAiResponse<FineTuneEvent>> listFineTuneEvents(@Path("fine_tune_id") String fineTuneId);

    @DELETE("/openai/fine-tunes/{fine_tune_id}")
    Single<DeleteResult> deleteFineTune(@Path("fine_tune_id") String fineTuneId);

    @POST("/v1/images/generations")
    Single<ImageResult> createImage(@Body CreateImageRequest request);

    @POST("/v1/images/edits")
    Single<ImageResult> createImageEdit(@Body RequestBody requestBody);

    @POST("/v1/images/variations")
    Single<ImageResult> createImageVariation(@Body RequestBody requestBody);

    @POST("/v1/moderations")
    Single<ModerationResult> createModeration(@Body ModerationRequest request);

    @Deprecated
    default Single<OpenAiResponse<Engine>> getEngines() {
        throw new UnsupportedOperationException("This method is no longer available.");
    }

    @Deprecated
    default Single<Engine> getEngine(@Path("engine_id") String engineId) {
        throw new UnsupportedOperationException("This method is no longer available.");
    }
}
