package io.hanko.plugin.keycloak.questioning;

import io.hanko.client.java.HankoClient;
import io.hanko.client.java.HankoClientConfig;
import io.hanko.client.java.models.HankoRequest;
import io.hanko.plugin.keycloak.authentication.HankoCredentialProvider;
import io.hanko.plugin.keycloak.common.HankoResourceProvider;
import io.hanko.plugin.keycloak.common.HankoUtils;
import io.hanko.plugin.keycloak.common.ValidationException;
import io.hanko.plugin.keycloak.serialization.HankoStatus;
import org.keycloak.TokenCategory;
import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.resources.Cors;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class UserQuestioningProvider extends HankoResourceProvider {

    UserQuestioningProvider(KeycloakSession session, HankoClient hankoClient) {
        super(session, hankoClient);
    }

    @OPTIONS
    @Path("{any:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVersionPreflight() {
        if (request == null) {
            logger.error("Request null");
        }
        return Cors.add(request, Response.ok())
                .allowedMethods("GET", "POST", "DELETE")
                .allowedOrigins(uriInfo, context.getClient())
                .preflight()
                .auth()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        ensureIsAuthenticatedUser();

        boolean isConfiguredForHanko = session.userCredentialManager().isConfiguredFor(
                context.getRealm(), auth.getUser(), HankoCredentialProvider.TYPE);

        try {
            HankoClientConfig config = HankoUtils.createConfig(session);

            boolean hasRegisteredDevices = false;

            String hankoUserId = userStore.getHankoUserId(currentUser());
            if(hankoUserId != null) {
                hasRegisteredDevices = hankoClient.hasRegisteredDevices(config, hankoUserId);
            }

            HankoStatus status = new HankoStatus(isConfiguredForHanko && hasRegisteredDevices);
            Response.ResponseBuilder responseBuilder = Response.ok(status);
            return HankoUtils.withCorsNoCache(responseBuilder, "GET", this);

        } catch(Exception ex) {
            String response = logAndFail("Could not fetch user devices from Hanko.", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return HankoUtils.withCorsNoCache(responseBuilder, "GET", this);
        }
    }

    @POST
    @Path("question")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postUserQuestion(UserQuestioningRepresentation questioningRequest) {
        ensureIsAuthenticatedUser();

        String hankoUserId = userStore.getHankoUserId(currentUser());
        String username = auth.getUser().getUsername();

        UserQuestioningRequestEntity entity = new UserQuestioningRequestEntity(questioningRequest);

        try {
            questioningRequest.validate();
        } catch (ValidationException ex) {
            String response = logAndFail("Invalid user questioning request.", ex);
            Response.ResponseBuilder responseBuilder = Response.status(400, "Bad Request").entity(response);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        }

        try {
            HankoClientConfig config = HankoUtils.createConfig(session);
            String remoteAddress = context.getConnection().getRemoteAddr();

            HankoRequest hankoRequest = hankoClient.requestTransaction(config, hankoUserId, username, remoteAddress, HankoClient.FidoType.FIDO_UAF, questioningRequest.getQuestionToDisplay());
            entity.setHankoId(hankoRequest.id);

            String id = entity.getId() == null ? KeycloakModelUtils.generateId() : entity.getId();
            entity.setId(id);

            getEntityManager().persist(entity);

            UserQuestioningRequestResponseRepresentation responseRepresentation =
                    new UserQuestioningRequestResponseRepresentation(id);

            Response.ResponseBuilder responseBuilder = Response.ok(responseRepresentation);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        } catch (Exception ex) {
            String response = logAndFail("Could not fetch user devices from Hanko.", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        }
    }

    @GET
    @Path("question/{questionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserQuestioningRequest(@PathParam("questionId") String questionId) {
        ensureIsAuthenticatedUser();
        UserQuestioningRequestEntity entity = getEntityManager().find(UserQuestioningRequestEntity.class, questionId);
        if (entity.isPending()) {
            try {
                HankoClientConfig config = HankoUtils.createConfig(session);
                HankoRequest hankoRequest = hankoClient.awaitConfirmation(config, entity.getHankoId());
                if(!hankoRequest.status.equals("PENDING")) {
                    String algorithm = session.tokens().signatureAlgorithm(TokenCategory.INTERNAL);
                    SignatureSignerContext signer = session.getProvider(SignatureProvider.class, algorithm).signer();
                    UserStatementTokenRepresentation token = new UserStatementTokenRepresentation();
                    token.setAudience(auth.getToken().getAudience());
                    token.setDisplayedStatements(entity.getStatementsToDisplay());
                    token.setIssuer(auth.getToken().getIssuer());
                    token.setQuestionDisplayed(entity.getQuestionToDisplay());
                    token.setQuestionId(entity.getId());
                    if(hankoRequest.status.equals("OK")) {
                        token.setStatement("yes");
                    } else {
                        token.setStatement("no");
                    }
                    token.setStatementDate(Time.currentTime());
                    token.setSub(auth.getToken().getSubject());
                    token.setUsedAcr("");
                    token.setUserId(entity.getUserId());
                    token.setUserIdType(entity.getUserIdType());

                    String jwt = new JWSBuilder().jsonContent(token).sign(signer);
                    entity.setUserStatementToken(jwt);
                    entity.setPending(false);
                    getEntityManager().persist(entity);
                }
            } catch (Exception ex) {
                String response = logAndFail("Could not fetch user devices from Hanko.", ex);
                Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
                return HankoUtils.withCorsNoCache(responseBuilder, "GET", this);
            }
        }

        if (entity.isPending()) {
            Response.ResponseBuilder responseBuilder = Response.notModified();
            return HankoUtils.withCorsNoCache(responseBuilder, "GET", this);
        } else {
            UserQuestioningPollResponseRepresentation pollResponse =
                    new UserQuestioningPollResponseRepresentation(entity.getUserStatementToken());
            Response.ResponseBuilder responseBuilder = Response.ok(pollResponse);
            return HankoUtils.withCorsNoCache(responseBuilder, "GET", this);
        }
    }

    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }
}
