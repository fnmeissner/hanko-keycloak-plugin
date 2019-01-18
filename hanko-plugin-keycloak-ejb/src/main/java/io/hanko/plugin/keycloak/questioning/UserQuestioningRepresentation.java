package io.hanko.plugin.keycloak.questioning;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.hanko.plugin.keycloak.common.ValidationException;
import org.keycloak.json.StringOrArrayDeserializer;
import org.keycloak.json.StringOrArraySerializer;

import java.util.Arrays;

public class UserQuestioningRepresentation {

    public UserQuestioningRepresentation() {}

    public UserQuestioningRepresentation(UserQuestioningRequestEntity entity) {
        this.clientNotificationToken = entity.getClientNotificationToken();
        this.userId = entity.getUserId();
        this.userIdType = entity.getUserIdType();
        this.questionToDisplay = entity.getQuestionToDisplay();
        this.statementsToDisplay = entity.getStatementsToDisplay();
        this.wishedAmr = entity.getWishedAmr();
        this.wishedAcr = entity.getWishedAcr();
    }

    @JsonProperty("client_notification_token")
    private String clientNotificationToken;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_id_type")
    private String userIdType;

    @JsonProperty("question_to_display")
    private String questionToDisplay;

    @JsonProperty("statements_to_display")
    @JsonSerialize(using = StringOrArraySerializer.class)
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private String[] statementsToDisplay;

    @JsonProperty("wished_amr")
    private String wishedAmr;

    @JsonProperty("wished_acr")
    @JsonSerialize(using = StringOrArraySerializer.class)
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private String[] wishedAcr;

    public void validate() throws ValidationException {
        if(statementsToDisplay == null || statementsToDisplay.length != 2) {
            throw new ValidationException("statements_to_display must contain 'yes' and 'no'");
        }

        if(Arrays.stream(statementsToDisplay).noneMatch("yes"::equals)) {
            throw new ValidationException("statements_to_display must contain 'yes' and 'no'");
        }

        if(Arrays.stream(statementsToDisplay).noneMatch("no"::equals)) {
            throw new ValidationException("statements_to_display must contain 'yes' and 'no'");
        }
    }

    public String getClientNotificationToken() {
        return clientNotificationToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserIdType() {
        return userIdType;
    }

    public String getQuestionToDisplay() {
        return questionToDisplay;
    }

    public String[] getStatementsToDisplay() {
        return statementsToDisplay;
    }

    public String getWishedAmr() {
        return wishedAmr;
    }

    public String[] getWishedAcr() {
        return wishedAcr;
    }
}
