package io.hanko.plugin.keycloak.questioning;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserQuestioningRequestResponseRepresentation {

    public UserQuestioningRequestResponseRepresentation() {}

    public UserQuestioningRequestResponseRepresentation(String questionId) {
        this.questionId = questionId;
    }

    @JsonProperty("questionId")
    private String questionId;

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionId() {
        return questionId;
    }

}
