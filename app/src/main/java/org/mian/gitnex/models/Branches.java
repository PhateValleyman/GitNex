package org.mian.gitnex.models;

import java.util.Date;

/**
 * Author M M Arif
 */

public class Branches {

    private String name;

    private commitObject commit;

    public Branches(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public commitObject getCommit() {
        return commit;
    }

    @Override
    public String toString() {
        return name;
    }

    public class commitObject {

        private String id;
        private String message;
        private String url;
        private Date timestamp;

        private authorObject author;

        public String getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }

        public authorObject getAuthor() {
            return author;
        }

        public String getUrl() {
            return url;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public class authorObject {

            private String name;
            private String email;
            private String username;

            public String getName() {
                return name;
            }

            public String getEmail() {
                return email;
            }

            public String getUsername() {
                return username;
            }
        }
    }
}
