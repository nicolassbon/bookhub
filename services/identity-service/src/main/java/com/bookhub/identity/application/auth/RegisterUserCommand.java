package com.bookhub.identity.application.auth;

public record RegisterUserCommand(
        String username,
        String email,
        String password,
        String displayName) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String username;
        private String email;
        private String password;
        private String displayName;

        private Builder() {
        }

        public Builder username(final String username) {
            this.username = username;
            return this;
        }

        public Builder email(final String email) {
            this.email = email;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public RegisterUserCommand build() {
            return new RegisterUserCommand(username, email, password, displayName);
        }
    }
}
