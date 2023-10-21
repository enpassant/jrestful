#openssl genrsa -out jrestful.pem 2048
##openssl pkcs12 -export -in jrestful.pem -out jrestful.pkcs12 -name jrestful -noiter -nomaciter
#openssl pkcs8 -topk8 -inform PEM -in jrestful.pem -out jrestful_private.pem -nocrypt
#openssl rsa -in jrestful.pem -outform PEM -pubout -out jrestful_public.pem
keytool -genkey -keystore keystore.pkcs12 -storetype pkcs12 -storepass jrestful_secret_pass -keyalg RSA -keysize 2048 -alias RS256 -keypass jrestful_secret_pass -sigalg SHA256withRSA -dname "CN=jrestful,OU=dev,O=enpassant,L=Szeged,ST=CSONGRAD,C=HU" -validity 360
