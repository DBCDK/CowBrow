#!/bin/bash
openssl genrsa -des3 -passout pass:xxxx -out server.pass.key 2048
openssl rsa -passin pass:xxxx -in server.pass.key -out server.key
rm server.pass.key
openssl req -new -key server.key -out server.csr \
  -subj "/C=DK/ST=/L=/O=DBC/OU=IT/CN=dbc.dk"
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
rm server.csr
