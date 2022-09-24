#!/bin/bash
#
# Extract server's certificate and store in a trust store for HTTPS client to use.

set -o errexit

KEY_STORE_PASS=secret
TRUST_STORE_PASS=changeit
STORE_TYPE=pkcs12

keystores=(src/main/resources/keystore_server src/test/resources/keystore_server_test src/test/resources/keystore_client_test)
keyaliases=(servercert serverkey clientcert)

i=0
while [[ $i -lt ${#keystores[@]} ]]; do
    echo $i
    store=${keystores[$i]}
    alias=${keyaliases[$i]}
    cert=exported_cert

    rm -f ${cert} "${store}_cert"
    keytool -exportcert -alias "${alias}" -keystore "${store}" -file "${cert}" -storepass "${KEY_STORE_PASS}"
    echo yes | keytool -importcert -keystore "${store}_cert" -file "${cert}" -storepass "${TRUST_STORE_PASS}" -storetype "${STORE_TYPE}"

    i=$((i + 1))
done
