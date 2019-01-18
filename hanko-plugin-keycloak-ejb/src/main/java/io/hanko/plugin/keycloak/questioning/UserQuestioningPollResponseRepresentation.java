package io.hanko.plugin.keycloak.questioning;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserQuestioningPollResponseRepresentation {

    public UserQuestioningPollResponseRepresentation() {}

    public UserQuestioningPollResponseRepresentation(String userStatementToken) {
        this.userStatementToken = userStatementToken;
    }

    @JsonProperty("user_statement_token")
    private String userStatementToken;

    public String getUserStatementToken() { return userStatementToken; }

    public void setUserStatementToken(String userStatementToken) { this.userStatementToken = userStatementToken; }
}
