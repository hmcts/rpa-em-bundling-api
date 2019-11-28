package uk.gov.hmcts.reform.em.stitching.template;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.*;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.em.stitching.service.impl.DocumentTaskProcessingException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Component
public class TemplateRenditionClient {

    @Value("${docmosis.render.endpoint}")
    private String docmosisRenderEndpoint;
    @Value("${docmosis.accessKey}")
    private String docmosisAccessKey;
    private final OkHttpClient client;

    @Autowired
    public TemplateRenditionClient(@Autowired OkHttpClient client) {
        this.client = client;
    }

    public File renderTemplate(String templateId, JsonNode payload) throws IOException, DocumentTaskProcessingException {
        String tempFileName = String.format("%s%s",
                UUID.randomUUID().toString(), ".pdf");

        MultipartBody requestBody = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "templateName",
                        templateId)
                .addFormDataPart(
                        "accessKey",
                        docmosisAccessKey)
                .addFormDataPart(
                        "outputName",
                        tempFileName)
                .addFormDataPart(
                        "data",
                        String.valueOf(payload))
                .build();

        Request request = new Request.Builder()
                .url(docmosisRenderEndpoint)
                .method("POST", requestBody)
                .build();

        Response response =  client.newCall(request).execute();

        if (response.isSuccessful()) {
            File file = File.createTempFile(
                    "docmosis-rendition",
                    ".pdf");
            IOUtils.copy(response.body().byteStream(), new FileOutputStream(file));
            return file;
        } else {
            throw new DocumentTaskProcessingException(
                    "Could not render Cover Page template. Error: " + response.body().string());
        }
    }
}
