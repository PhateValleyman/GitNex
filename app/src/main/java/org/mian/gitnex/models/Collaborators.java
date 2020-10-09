package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class Collaborators {

    private int id;
    private String login;
    private String full_name;
    private String email;
    private String avatar_url;
    private String language;
    private String username;

	public Collaborators(String full_name, String login, String avatar_url) {

		this.full_name =  full_name;
		this.login = login;
		this.avatar_url = avatar_url;
	}

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public String getLanguage() {
        return language;
    }

    public String getUsername() {
        return username;
    }
}
