package io.hanko.plugin.keycloak.questioning;

import io.hanko.plugin.keycloak.common.HankoUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USER_QUESTIONING_REQUEST")
public class UserQuestioningRequestEntity {

    UserQuestioningRequestEntity() {
    }

    UserQuestioningRequestEntity(UserQuestioningRepresentation representation) {
        setClientNotificationToken(representation.getClientNotificationToken());
        setUserId(representation.getUserId());
        setUserIdType(representation.getUserIdType());
        setQuestionToDisplay(representation.getQuestionToDisplay());
        setStatementsToDisplay(representation.getStatementsToDisplay());
        setWishedAmr(representation.getWishedAmr());
        setWishedAcr(representation.getWishedAcr());
        setPending(true);
    }

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "CLIENT_NOTIFICATION_TOKEN", nullable = true)
    private String clientNotificationToken;

    @Column(name = "USER_ID", nullable = true)
    private String userId;

    @Column(name = "USER_ID_TYPE", nullable = true)
    private String userIdType;

    @Column(name = "QUESTION_TO_DISPLAY", nullable = false)
    private String questionToDisplay;

    @Column(name = "STATEMENTS_TO_DISPLAY", nullable = false)
    private String statementsToDisplay;

    @Column(name = "WISHED_AMR", nullable = false)
    private String wishedAmr;

    @Column(name = "WISHED_ACR", nullable = false)
    private String wishedAcr;

    @Column(name = "HANKO_ID", nullable = false)
    private String hankoId;

    @Column(name = "IS_PENDING", nullable = false)
    private boolean isPending;

    @Column(name = "USER_STATEMENT_TOKEN")
    private String userStatementToken;

    public String getId() {
        return id;
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
        return HankoUtils.deserializeArray(statementsToDisplay);
    }

    public String getWishedAmr() {
        return wishedAmr;
    }

    public String[] getWishedAcr() {
        return HankoUtils.deserializeArray(wishedAcr);
    }

    public boolean isPending() {
        return isPending;
    }

    public String getUserStatementToken() {
        return userStatementToken;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setClientNotificationToken(String clientNotificationToken) {
        this.clientNotificationToken = clientNotificationToken;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserIdType(String userIdType) {
        this.userIdType = userIdType;
    }

    public void setQuestionToDisplay(String questionToDisplay) {
        this.questionToDisplay = questionToDisplay;
    }

    public void setStatementsToDisplay(String[] statementsToDisplay) {
        this.statementsToDisplay = HankoUtils.serializeArray(statementsToDisplay);
    }

    public void setWishedAmr(String wishedAmr) {
        this.wishedAmr = wishedAmr;
    }

    public void setWishedAcr(String[] wishedAcr) {
        this.wishedAcr = HankoUtils.serializeArray(wishedAcr);
    }

    public String getHankoId() {
        return hankoId;
    }

    public void setHankoId(String hankoId) {
        this.hankoId = hankoId;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    public void setUserStatementToken(String userStatementToken) {
        this.userStatementToken = userStatementToken;
    }
}
