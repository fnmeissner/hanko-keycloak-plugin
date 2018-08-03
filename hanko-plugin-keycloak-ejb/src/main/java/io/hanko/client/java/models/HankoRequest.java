package io.hanko.client.java.models;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class HankoRequest {
    public String id;
    public String userId;
    public String username;
    public String status;
    public ClientData clientData;
    public String transaction;
    public List<LinkRelation> links;

    public LinkRelation getLink(String name) {
        for(LinkRelation linkRelation : links) {
            if(name.equals(linkRelation.rel)) {
                return linkRelation;
            }
        }

        throw new NoSuchElementException(name);
    }

    public boolean isConfirmed() {
        return Objects.equals(status, "OK");
    }
}
