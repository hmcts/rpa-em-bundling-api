package uk.gov.hmcts.reform.em.stitching.testutil;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.stitching.domain.Bundle;
import uk.gov.hmcts.reform.em.stitching.domain.BundleDocument;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class TestUtil {

    private String s2sToken;
    private String idamToken;

    public File getDocumentBinary(String documentId) throws Exception {
        Response response = s2sAuthRequest()
                .header("user-roles", "caseworker")
                .request("GET", Env.getDmApiUrl() + "/documents/" + documentId + "/binary");

        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir") + "/" + documentId + "-test.pdf");

        Files.copy(response.getBody().asInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);

        return tempPath.toFile();
    }

    public String uploadDocument(String pdfName) {
        String newDocUrl = s2sAuthRequest()
                .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
                .multiPart("files", "test.pdf",  ClassLoader.getSystemResourceAsStream(pdfName), "application/pdf")
                .multiPart("classification", "PUBLIC")
                .request("POST", Env.getDmApiUrl() + "/documents")
                .getBody()
                .jsonPath()
                .get("_embedded.documents[0]._links.self.href");

        return newDocUrl;
    }

    public String uploadDocument() {
        return uploadDocument("annotationTemplate.pdf");
    }

    public RequestSpecification authRequest() {
        return s2sAuthRequest()
            .header("Authorization", "Bearer " + getIdamToken("test@test.com"));
    }

    public RequestSpecification s2sAuthRequest() {
        RestAssured.useRelaxedHTTPSValidation();
        return RestAssured
                .given()
                .header("ServiceAuthorization", "Bearer " + getS2sToken());
    }

    public String getIdamToken() {
        return getIdamToken("test@test.com");
    }

    public String getIdamToken(String username) {
        if (idamToken == null) {
            createUser(username, "password");
            String userId = findUserIdByUserEmail(username).toString();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", userId);
            jsonObject.put("role", "caseworker");

            Response response = RestAssured
                    .given()
                    .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .formParam("id", userId)
                    .formParam("role", "caseworker")
                    .post(Env.getIdamURL() + "/testing-support/lease");

            idamToken = response.getBody().print();
        }
        return idamToken;
    }

    private Integer findUserIdByUserEmail(String email) {
        return RestAssured
                .get(Env.getIdamURL() + "/users?email=" + email)
                .getBody()
                .jsonPath()
                .get("id");
    }

    public void createUser(String email, String password) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("password", password);
        jsonObject.put("forename", "test");
        jsonObject.put("surname", "test");

        RestAssured
            .given()
            .header("Content-Type", "application/json")
            .body(jsonObject.toString())
            .post(Env.getIdamURL() + "/testing-support/accounts");

    }


    public String getS2sToken() {

        if (s2sToken == null) {
            String otp = String.valueOf(new GoogleAuthenticator().getTotpPassword(Env.getS2SToken()));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("microservice", Env.getS2SServiceName());
            jsonObject.put("oneTimePassword", otp);

            Response response = RestAssured
                    .given()
                    .header("Content-Type", "application/json")
                    .body(jsonObject.toString())
                    .post(Env.getS2SURL() + "/lease");
            s2sToken = response.getBody().asString();
            s2sToken = response.getBody().print();
        }

        return s2sToken;
    }

    public Bundle getTestBundle() {
        String documentUrl = uploadDocument();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
        BundleDocument document = new BundleDocument();

        document.setDocumentId(Long.parseLong(documentId));
        document.setDocumentURI(documentUrl);

        Bundle bundle = new Bundle();
        bundle.setDescription("Test bundle");
        bundle.getDocuments().add(document);

        return bundle;
    }
}
