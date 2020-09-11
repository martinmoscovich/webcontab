if [ -z $1 ]; then
    echo "Defina la nueva version"
    exit 1;
fi

# Incrementa la version del server
mvn versions:set-property -DgenerateBackupPoms=false -Dproperty=server.version -DnewVersion=$1

# Incrementa la version de la aplicacion
mvn build-helper:parse-version versions:set -DgenerateBackupPoms=false -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}\${parsedVersion.qualifier?}



 
