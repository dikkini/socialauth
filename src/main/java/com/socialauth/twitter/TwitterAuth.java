package com.socialauth.twitter;

import org.apache.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * akarapetov
 * com.socialauth.twitter
 * socialauth
 * 13.01.2017
 */
public class TwitterAuth {

    private Logger logger = Logger.getLogger(this.getClass());

    private String apiKey;

    private String apiSecret;

    private String serviceRedirectURL;

    /*
    Session Twitter Instance
     */
    private Twitter twitterInstance;

    /*
    Session Request Token
     */
    private RequestToken requestToken;


    public TwitterAuth(String apiKey, String apiSecret, String serviceRedirectURL) throws TwitterException {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.serviceRedirectURL = serviceRedirectURL;

        twitterInstance = new TwitterFactory().getInstance();
        twitterInstance.setOAuthConsumer(apiKey, apiSecret);
    }

    public TwitterUser getUserInfo(String oAuthVerifier) throws TwitterException {
        AccessToken oAuthAccessToken = twitterInstance.getOAuthAccessToken(requestToken, oAuthVerifier);
        String screenName = oAuthAccessToken.getScreenName();
        String userId = String.valueOf(oAuthAccessToken.getUserId());

        TwitterUser twitterUser = new TwitterUser();
        twitterUser.setId(userId);
        twitterUser.setScreenName(screenName);

        return twitterUser;
    }

    public String getTwitterRedirectURL() throws TwitterException {
        requestToken = twitterInstance.getOAuthRequestToken(serviceRedirectURL);
        return requestToken.getAuthenticationURL();
    }
}
