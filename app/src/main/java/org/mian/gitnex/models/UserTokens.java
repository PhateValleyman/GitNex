package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class UserTokens {

    private int id;
    private String name;
    private String sha1;
    private String token_last_eight;

    public UserTokens(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSha1() {
        return sha1;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken_last_eight() {
        return token_last_eight;
    }
}
