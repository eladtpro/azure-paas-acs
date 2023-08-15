package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.azure.communication.common.*;
import com.azure.communication.identity.*;
import com.azure.communication.identity.models.*;
import com.azure.core.credential.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetACSAccessToken {
    /**
     * This function listens at endpoint "/api/GetACSAccessToken". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/GetACSAccessToken
     * 2. curl "{your host}/api/GetACSAccessToken?name=HTTP%20Query"
     */
    @FunctionName("GetACSAccessToken")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.FUNCTION)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request. Azure Communication Services - Access Tokens.");

        // STEP 1: Authenticate the client
        String connectionString = System.getenv("AZURE_ACS_CONNECTION_STRING");
        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // STEP 2: Create an identity and issue a token in one request
        // Create an identity and issue token with a validity of 24 hours in one call
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        CommunicationUserIdentifierAndToken result = communicationIdentityClient.createUserAndToken(scopes);
        CommunicationUserIdentifier user = result.getUser();
        System.out.println("\nCreated a user identity with ID: " + user.getId());
        AccessToken accessToken = result.getUserToken();
        OffsetDateTime expiresAt = accessToken.getExpiresAt();
        String token = accessToken.getToken();
        System.out.println("\nIssued an access token with 'chat' scope that expires at: " + expiresAt + ": " + token);

        return request.createResponseBuilder(HttpStatus.OK).body(token).build();
    }
}
