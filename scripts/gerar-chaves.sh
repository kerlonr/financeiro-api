#!/bin/sh
# Gera o par de chaves RSA que assina (privada) e valida (pública) os JWT em produção.
# As chaves ficam em ./secrets, que está no .gitignore. Rode uma vez por servidor, com sudo:
#   sudo sh scripts/gerar-chaves.sh
set -eu

# A API roda no contêiner como UID 1000: só esse usuário pode ler a chave privada.
UID_DA_API=1000

cd "$(dirname "$0")/.."
mkdir -p secrets

if [ -f secrets/jwt-privada.pem ]; then
  echo "As chaves já existem em ./secrets. Apague-as antes se quiser gerar novas (todos serão deslogados)."
  exit 1
fi

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt-privada.pem
openssl pkey -in secrets/jwt-privada.pem -pubout -out secrets/jwt-publica.pem
chown "$UID_DA_API:$UID_DA_API" secrets/jwt-privada.pem secrets/jwt-publica.pem
chmod 600 secrets/jwt-privada.pem
chmod 644 secrets/jwt-publica.pem

echo "Chaves geradas em ./secrets"
