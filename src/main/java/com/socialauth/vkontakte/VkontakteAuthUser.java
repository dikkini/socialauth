package com.socialauth.vkontakte;

import com.google.gson.annotations.SerializedName;

/**
 * akarapetov
 * com.socialauth.vkontakte
 * socialauth
 * 13.01.2017
 */
public class VkontakteAuthUser {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("expires_in")
    private Integer expiresIn;

    @SerializedName("email")
    private String email;

    @SerializedName("error")
    private String error;

    public String getAccessToken() {
        return accessToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getError() {
        return error;
    }
}