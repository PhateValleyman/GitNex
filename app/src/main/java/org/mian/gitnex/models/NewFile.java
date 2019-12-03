package org.mian.gitnex.models;

/**
 * Author M M Arif
 */

public class NewFile {

    private String branch;
    private String content;
    private String message;
    private String new_branch;

    private authorObject author;
    private committerObject committer;

    public NewFile(String branch, String content, String message, String new_branch) {
        this.branch = branch;
        this.content = content;
        this.message = message;
        this.new_branch = new_branch;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getContents() {
        return content;
    }

    public void setContents(String contents) {
        this.content = contents;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNew_branch() {
        return new_branch;
    }

    public void setNew_branch(String new_branch) {
        this.new_branch = new_branch;
    }

    public class authorObject {

        private String email;
        private String name;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public class committerObject {

        private String email;
        private String name;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}