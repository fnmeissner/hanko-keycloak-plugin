package io.hanko.plugin.keycloak.questioning;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.keycloak.json.StringOrArrayDeserializer;
import org.keycloak.json.StringOrArraySerializer;

public class UserStatementTokenRepresentation {

    @JsonProperty("question_id")
    private String questionId;

    @JsonProperty("iss")
    private String issuer;

    @JsonProperty("sub")
    private String sub;

    @JsonProperty("aud")
    @JsonSerialize(using = StringOrArraySerializer.class)
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private String[] audience;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_id_type")
    private String userIdType;

    @JsonProperty("question_displayed")
    private String questionDisplayed;

    @JsonProperty("displayed_statements")
    @JsonSerialize(using = StringOrArraySerializer.class)
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private String[] displayedStatements;

    @JsonProperty("statement")
    private String statement;

    @JsonProperty("statement_date")
    private int statementDate;

    @JsonProperty("used_amr")
    @JsonSerialize(using = StringOrArraySerializer.class)
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private String[] usedAmr;

    @JsonProperty("used_acr")
    private String usedAcr;

    public String getQuestionId() {
        return questionId;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getSub() {
        return sub;
    }

    public String[] getAudience() {
        return audience;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserIdType() {
        return userIdType;
    }

    public String getQuestionDisplayed() {
        return questionDisplayed;
    }

    public String[] getDisplayedStatements() {
        return displayedStatements;
    }

    public String getStatement() {
        return statement;
    }

    public int getStatementDate() {
        return statementDate;
    }

    public String[] getUsedAmr() {
        return usedAmr;
    }

    public String getUsedAcr() {
        return usedAcr;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public void setAudience(String[] audience) {
        this.audience = audience;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserIdType(String userIdType) {
        this.userIdType = userIdType;
    }

    public void setQuestionDisplayed(String questionDisplayed) {
        this.questionDisplayed = questionDisplayed;
    }

    public void setDisplayedStatements(String[] displayedStatements) {
        this.displayedStatements = displayedStatements;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public void setStatementDate(int statementDate) {
        this.statementDate = statementDate;
    }

    public void setUsedAmr(String[] usedAmr) {
        this.usedAmr = usedAmr;
    }

    public void setUsedAcr(String usedAcr) {
        this.usedAcr = usedAcr;
    }
}
