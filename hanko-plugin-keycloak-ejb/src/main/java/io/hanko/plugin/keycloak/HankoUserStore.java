package io.hanko.plugin.keycloak;

import org.keycloak.models.UserModel;

import java.util.LinkedList;
import java.util.List;

public class HankoUserStore {
    private static String HANKO_REGISTRATION_ID = "HANKO_REGISTRATION_ID";
    private static String HANKO_REQUEST_ID = "HANKO_REQUEST_ID";

    public String getHankoUserId(UserModel userModel) {
        return getUserAttribute(userModel, HANKO_REGISTRATION_ID);
    }

    public void setHankoUserId(UserModel userModel, String userId) {
        setUserAttribute(userModel, HANKO_REGISTRATION_ID, userId);
    }

    public String getHankoRequestId(UserModel userModel) {
        return getUserAttribute(userModel, HANKO_REQUEST_ID);
    }

    public void setHankoRequestId(UserModel userModel, String hankoRequestId) {
        setUserAttribute(userModel, HANKO_REQUEST_ID, hankoRequestId);
    }

    private void setUserAttribute(UserModel userModel, String name, String value) {
        List<String> attributes = new LinkedList<String>();
        if(value != null) {
            attributes.add(value);
        }
        userModel.setAttribute(name, attributes);
    }

    private String getUserAttribute(UserModel userModel, String name) {
        List<String> registrationId = userModel.getAttribute(name);

        if(registrationId == null || registrationId.isEmpty()) {
            return null;
        } else {
            return registrationId.get(0);
        }
    }
}
