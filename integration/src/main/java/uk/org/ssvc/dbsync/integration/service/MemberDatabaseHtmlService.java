package uk.org.ssvc.dbsync.integration.service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.jayway.restassured.RestAssured.given;

@Singleton
public class MemberDatabaseHtmlService {

    private static final String AUTH_COOKIE_NAME = "PHPSESSID";
    private static final String LOGIN_PATH = "/include/do_login.php";
    private static final String ACTIVE_MEMBER_PATH = "/reporting_index.php?rep=curallmem&out=onscreen";
    private static final String LAPSED_MEMBER_PATH = "/reporting_index.php?rep=expmemall&out=onscreen";

    private final String url;
    private final String username;
    private final String password;

    @Inject
    public MemberDatabaseHtmlService(
            @Named("ssvc.members.url") String url,
            @Named("ssvc.members.username") String username,
            @Named("ssvc.members.password") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String fetchHtmlForActiveMembersPage() {
        return given()
            .cookie(AUTH_COOKIE_NAME, generateAuthenticatedSessionToken())
            .get(url + ACTIVE_MEMBER_PATH)
            .then()
            .extract().asString();
    }

    public String fetchHtmlForLapsedMembersPage() {
        return given()
            .cookie(AUTH_COOKIE_NAME, generateAuthenticatedSessionToken())
            .get(url + LAPSED_MEMBER_PATH)
            .then()
            .extract().asString();
    }

    public String generateAuthenticatedSessionToken() {
        return given()
            .formParam("username", username)
            .formParam("userpass", password)
            .when()
            .post(url + LOGIN_PATH)
            .then()
            .statusCode(302)
            .extract()
            .cookie(AUTH_COOKIE_NAME);
    }

}
