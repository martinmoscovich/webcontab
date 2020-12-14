# Busca la nueva version del server
VERSION=$(mvn help:evaluate -Dexpression=server.version -q -DforceStdout)
NEXT_VERSION=$((VERSION + 1))

# Incrementa la version del server
mvn versions:set-property -DgenerateBackupPoms=false -Dproperty=server.version -DnewVersion=$NEXT_VERSION

# Incrementa la version de la aplicacion
mvn build-helper:parse-version versions:set -DgenerateBackupPoms=false -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}\${parsedVersion.qualifier?}



 
