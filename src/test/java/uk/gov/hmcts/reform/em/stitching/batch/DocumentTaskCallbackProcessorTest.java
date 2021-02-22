package uk.gov.hmcts.reform.em.stitching.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.em.stitching.Application;
import uk.gov.hmcts.reform.em.stitching.domain.Bundle;
import uk.gov.hmcts.reform.em.stitching.domain.Callback;
import uk.gov.hmcts.reform.em.stitching.domain.DocumentTask;
import uk.gov.hmcts.reform.em.stitching.domain.enumeration.CallbackState;
import uk.gov.hmcts.reform.em.stitching.service.mapper.DocumentTaskMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DocumentTaskCallbackProcessorTest {

    DocumentTaskCallbackProcessor documentTaskCallbackProcessor;

    DocumentTask documentTask;

    @Autowired
    private DocumentTaskMapper documentTaskMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        documentTask = new DocumentTask();
        documentTask.setJwt("jwt");
        uk.gov.hmcts.reform.em.stitching.domain.Callback callback = new Callback();
        documentTask.setCallback(callback);
        callback.setCallbackUrl("https://mycallback.com");
        Bundle bundle = new Bundle();
        bundle.setId(1234L);
        documentTask.setBundle(bundle);
    }

    @Test
    public void testCallback200() throws InterruptedException {

        documentTaskCallbackProcessor = buildProcessorWithHttpClientIntercepted(200, "{}");
        documentTaskCallbackProcessor.callBackMaxAttempts = 3;

        DocumentTask processedDocumentTask =
                documentTaskCallbackProcessor.process(documentTask);

        Assert.assertEquals(CallbackState.SUCCESS, processedDocumentTask.getCallback().getCallbackState());

    }

    @Test
    public void testCallback500() throws InterruptedException {

        documentTaskCallbackProcessor = buildProcessorWithHttpClientIntercepted(543, "errorx");
        documentTaskCallbackProcessor.callBackMaxAttempts = 1;

        DocumentTask processedDocumentTask =
                documentTaskCallbackProcessor.process(documentTask);

        Assert.assertEquals(CallbackState.FAILURE, processedDocumentTask.getCallback().getCallbackState());

    }

    private DocumentTaskCallbackProcessor buildProcessorWithHttpClientIntercepted(int httpStatus, String responseBody) {
        OkHttpClient http = new OkHttpClient
                .Builder()
                .addInterceptor(chain -> new Response.Builder()
                    .body(
                        ResponseBody.create(
                            MediaType.get("application/json"),
                            responseBody
                        )
                    )
                    .request(chain.request())
                    .message("")
                    .code(httpStatus)
                    .protocol(Protocol.HTTP_2)
                    .build())
                .build();

        return new DocumentTaskCallbackProcessor(http, () -> "auth", documentTaskMapper, objectMapper);

    }

}
