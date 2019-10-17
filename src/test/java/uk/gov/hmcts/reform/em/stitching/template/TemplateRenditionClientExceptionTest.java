package uk.gov.hmcts.reform.em.stitching.template;

import okhttp3.*;
import org.apache.pdfbox.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.em.stitching.service.impl.DocumentTaskProcessingException;

import java.io.IOException;
import java.io.InputStream;

public class TemplateRenditionClientExceptionTest {

    private TemplateRenditionClient client;

    private static final String COVER_PAGE_TEMPLATE = "FL-FRM-GOR-ENG-12345";

    @Before
    public void setup() {
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(TemplateRenditionClientExceptionTest::intercept)
                .build();

        client = new TemplateRenditionClient(okHttpClient, "http://example.org", "key");
    }

    private static Response intercept(Interceptor.Chain chain) throws IOException {
        InputStream file = ClassLoader.getSystemResourceAsStream(COVER_PAGE_TEMPLATE + ".pdf");

        return new Response.Builder()
                .body(ResponseBody.create(MediaType.get("application/pdf"), IOUtils.toByteArray(file)))
                .request(chain.request())
                .message("Error!")
                .code(400)
                .protocol(Protocol.HTTP_2)
                .build();
    }

    @Test(expected = DocumentTaskProcessingException.class)
    public void renderTemplate() throws IOException, DocumentTaskProcessingException {
        client.renderTemplate(COVER_PAGE_TEMPLATE, "json_blob");
    }
}