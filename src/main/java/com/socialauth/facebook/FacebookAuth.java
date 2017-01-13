package com.socialauth.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.User;
import com.socialauth.common.SocialAuthException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import java.io.IOException;
import java.util.Arrays;

/**
 * akarapetov
 * com.socialauth.facebook
 * socialauth
 * 12.01.2017
 */
public class FacebookAuth {

    private Logger logger = Logger.getLogger(this.getClass());

    /*
    Facebook application Id
     */
    private Integer appId;

    /*
    Facebook application Exchange Key
     */
    private String exchangeKey;

    /*
    Facebook application Client Secret
     */
    private String clientSecret;

    /*
    Your service redirectURL
     */
    private String serviceRedirectURL;

    /*
    URL where user should be redirected to be authenticated
     */
    private String facebookRedirectURL = "https://www.facebook.com/dialog/oauth/?";

    /*
    facebook graph URL. Used to retrieve access token and call other facebook API methods
     */
    private String facebookGraphURL = "https://graph.facebook.com/oauth/access_token?";

    public FacebookAuth(Integer appId, String redirectURL, String exchangeKey, String clientSecret, FacebookScope... facebookScopes) {
        logger.info("FacebookAuth constructor with parameters = {appId: " + appId + ",redirectURL:" + redirectURL + ",exchangeKey:" + exchangeKey + ",clientSecret:" + clientSecret + ",facebookScopes:" + Arrays.toString(facebookScopes));
        this.appId = appId;
        this.exchangeKey = exchangeKey;
        this.clientSecret = clientSecret;
        this.serviceRedirectURL = redirectURL;

        logger.debug("Build redirect URL for client");

        StringBuilder facebookRedirectURLBuilder = new StringBuilder(this.facebookRedirectURL);
        facebookRedirectURLBuilder.append("client_id=");
        facebookRedirectURLBuilder.append(this.appId);
        facebookRedirectURLBuilder.append("&redirect_uri=");
        facebookRedirectURLBuilder.append(this.serviceRedirectURL);
        facebookRedirectURLBuilder.append("&scope=");
        int fbScopesCount = 0;
        for (FacebookScope facebookScope : facebookScopes) {
            facebookRedirectURLBuilder.append(facebookScope);
            if (fbScopesCount++ == facebookScopes.length) {
                continue;
            }
            facebookRedirectURLBuilder.append(",");
        }
        facebookRedirectURLBuilder.append("&state=");
        facebookRedirectURLBuilder.append(exchangeKey);
        facebookRedirectURLBuilder.append("&display=page");
        facebookRedirectURLBuilder.append("&response_type=code");

        this.facebookRedirectURL = facebookRedirectURLBuilder.toString();
        logger.debug("Redirect URL: " + this.facebookRedirectURL);
    }

    public FacebookUser getUserInfo(String code) throws IOException {
        StringBuilder facebookGraphURLBuilder = new StringBuilder(this.facebookGraphURL);
        facebookGraphURLBuilder.append("client_id=");
        facebookGraphURLBuilder.append(this.appId);
        facebookGraphURLBuilder.append("&redirect_uri=");
        facebookGraphURLBuilder.append(this.serviceRedirectURL);
        facebookGraphURLBuilder.append("&client_secret=");
        facebookGraphURLBuilder.append(this.clientSecret);
        facebookGraphURLBuilder.append("&code=");
        facebookGraphURLBuilder.append(code);

        String facebookGraphURL = facebookGraphURLBuilder.toString();

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();
        // Create a method instance.
        GetMethod method = new GetMethod(facebookGraphURL);
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));
        // Execute the method.
        int statusCode = client.executeMethod(method);
        method.releaseConnection();
        byte[] responseBody = method.getResponseBody();
        String responseBodyString = new String(responseBody);
        if (statusCode != HttpStatus.SC_OK) {
            logger.error("FacebookAuth: Bad HTTP status code. ResponseBody: " + responseBodyString);
            throw new SocialAuthException("FacebookGraph's response HTTP code is not 200.");
        }
        if (!responseBodyString.contains("access_token")) {
            throw new SocialAuthException("FacebookGraph's response does not contain access token field. Check all parameters.");
        }
        String[] mainResponseArray = responseBodyString.split("&");
        // {"access_token= AAADD1QFhDlwBADrKkn87ZABAz6ZCBQZ//DZD ","expires=5178320"}
        String accesstoken = null;
        for (String string : mainResponseArray) {
            if (string.contains("access_token")) {
                accesstoken = string.replace("access_token=", "").trim();
            }
        }

        if (accesstoken == null) {
            logger.error("FacebookAuth: Access Token is NULL. ResponseBody: " + responseBodyString);
            throw new SocialAuthException("Access Token from request to FacebookGraph API is null. Check all parameters.");
        }

        FacebookClient facebookClient = new DefaultFacebookClient(accesstoken, Version.LATEST);

        User fbRestUser = facebookClient.fetchObject("me", com.restfb.types.User.class,
                Parameter.with("fields", "id,email,first_name,last_name,name,locale,name_format"));

        String firstName = fbRestUser.getFirstName();
        String lastName = fbRestUser.getLastName();
        String email = fbRestUser.getEmail();
        String id = fbRestUser.getId();

        FacebookUser facebookUser = new FacebookUser();
        facebookUser.setId(id);
        facebookUser.setFirstName(firstName);
        facebookUser.setLastName(lastName);
        facebookUser.setEmail(email);

        return facebookUser;
    }

    private static String generateHashFacebookAuth() {
        String word = RandomStringUtils.randomAlphabetic(10);
        PasswordEncoder encoder = new StandardPasswordEncoder();
        return encoder.encode(word);
    }
}
