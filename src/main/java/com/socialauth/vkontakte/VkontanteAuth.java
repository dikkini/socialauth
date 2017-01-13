package com.socialauth.vkontakte;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.socialauth.common.SocialAuthException;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.apache.log4j.Logger;

import java.io.StringReader;
import java.util.Arrays;

/**
 * akarapetov
 * com.socialauth.vkontakte
 * socialauth
 * 13.01.2017
 */
public class VkontanteAuth {

    private Logger logger = Logger.getLogger(this.getClass());

    private String clientId;

    private String clientSecret;

    private String serviceRedirectURL;


    public VkontanteAuth(String clientId, String clientSecret, String redirectURL, VkontakteScope... vkontakteScopes) {
        logger.info("VkontanteAuth constructor with parameters = {clientId: " + clientId + ",redirectURL:" + redirectURL + ",vkontakteScopes:" + Arrays.toString(vkontakteScopes));
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.serviceRedirectURL = redirectURL;

        logger.debug("Build redirect URL for client");

        String vkontakteRedirectURL = "https://oauth.vk.com/authorize?";
        StringBuilder vkontakteRedirectURLBuilder = new StringBuilder(vkontakteRedirectURL);
        vkontakteRedirectURLBuilder.append("clientId");
        vkontakteRedirectURLBuilder.append(clientId);
        vkontakteRedirectURLBuilder.append("&scope=");
        int vkScopesCount = 0;
        for (VkontakteScope vkontakteScope : vkontakteScopes) {
            vkontakteRedirectURLBuilder.append(vkontakteScope);
            if (vkScopesCount++ == vkontakteScopes.length) {
                continue;
            }
            vkontakteRedirectURLBuilder.append(",");
        }
        vkontakteRedirectURLBuilder.append("&redirect_uri=");
        vkontakteRedirectURLBuilder.append(this.serviceRedirectURL);
        vkontakteRedirectURLBuilder.append("&response_type=");
        vkontakteRedirectURLBuilder.append("code");
        vkontakteRedirectURLBuilder.append("&v=");
        vkontakteRedirectURLBuilder.append("5.62");
        vkontakteRedirectURL = vkontakteRedirectURLBuilder.toString();
        logger.debug("Redirect URL: " + vkontakteRedirectURL);
    }

    public VkontakteUser getUserInfo(String code) throws ClientException {
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient, new Gson());
        String textResponse = vk.oauth().userAuthorizationCodeFlow(Integer.valueOf(clientId), clientSecret,
                serviceRedirectURL, code).executeAsString();
        JsonReader jsonReader = new JsonReader(new StringReader(textResponse));
        JsonObject json = (JsonObject)(new JsonParser()).parse(jsonReader);
        if (json.has("error")) {
            throw new SocialAuthException("VkontakteAuth: " + json.get("error"));
        }

        VkontakteAuthUser vkAuthResponse = new Gson().fromJson(json, VkontakteAuthUser.class);

        String email = vkAuthResponse.getEmail();
        Integer userId = vkAuthResponse.getUserId();

        VkontakteUser vkontakteUser = new VkontakteUser();
        vkontakteUser.setId(String.valueOf(userId));
        vkontakteUser.setEmail(email);

        return vkontakteUser;
    }
}
