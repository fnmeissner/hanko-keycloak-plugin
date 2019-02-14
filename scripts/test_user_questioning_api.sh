#!/bin/bash

export DIRECT_GRANT_RESPONSE=$(curl -i --request POST http://localhost:8080/auth/realms/master/protocol/openid-connect/token --header "Accept: application/json" --header "Content-Type: application/x-www-form-urlencoded" --data "grant_type=password&username=admin&password=admin&client_id=admin-cli")

echo $DIRECT_GRANT_RESPONSE

#
#echo -e "\n\nSENT RESOURCE-OWNER-PASSWORD-CREDENTIALS-REQUEST. OUTPUT IS:\n\n";
#echo $DIRECT_GRANT_RESPONSE;
#
export ACCESS_TOKEN=$(echo $DIRECT_GRANT_RESPONSE | grep "access_token" | sed 's/.*\"access_token\":\"\([^\"]*\)\".*/\1/g');
#echo -e "\n\nACCESS TOKEN IS \"$ACCESS_TOKEN\"";
#
#echo -e "\n\nSENDING AUTHENTICATED REQUEST. THIS SHOULD SUCCESSFULY CREATE COMPANY AND SUCCESS WITH 201: ";
http POST http://localhost:8080/auth/realms/master/hanko-questioning-provider-factory/question Authorization:"Bearer $ACCESS_TOKEN" question_to_display="transfer 100,00 EUR to fme?" statements_to_display:='["yes","no"]'
export QUESTIONING_REQUEST_RESULT=$(http POST http://localhost:8080/auth/realms/master/hanko-questioning-provider-factory/question Authorization:"Bearer $ACCESS_TOKEN" question_to_display="transfer 120,00 EUR to fme?" statements_to_display:='["yes","no"]')
echo $QUESTIONING_REQUEST_RESULT
#
#
QUESTIONING_ID=$(echo $QUESTIONING_REQUEST_RESULT | jq -r .questionId)
echo -e "Questioning ID: $QUESTIONING_ID";

echo -e "\n\nGET REQUEST: http://localhost:8080/auth/realms/master/hanko-questioning-provider-factory/question/${QUESTIONING_ID}";

until [ http --check-status http://localhost:8080/auth/realms/master/hanko-questioning-provider-factory/question/${QUESTIONING_ID} Authorization:"Bearer $ACCESS_TOKEN" &> /dev/null ]; do
    http http://localhost:8080/auth/realms/master/hanko-questioning-provider-factory/question/${QUESTIONING_ID} Authorization:"Bearer $ACCESS_TOKEN"
    sleep 1
done

