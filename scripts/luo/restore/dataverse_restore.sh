DIR=$(pwd)
TIME=${DIR:0-14:14}

echo "stop glassfish"
/usr/local/glassfish4/bin/asadmin stop-domain domain1

echo "restore database"
su - postgres -c "dropdb -U postgres -e dvndb_v4_0; \
createdb -U postgres --encoding=UTF-8 --owner=dvnapp_v4_0 --echo dvndb_v4_0; \
psql dvndb_v4_0 < $DIR/dvndb_v4_0_$TIME.sql;"

echo "restore logs"
rm -rf /usr/local/glassfish4/glassfish/domains/domain1/docroot/logos
unzip $DIR/logos_$TIME.zip -d /usr/local/glassfish4/glassfish/domains/domain1/docroot

echo "restore files"
rm -rf /usr/local/glassfish4/glassfish/domains/domain1/files/10.18170
unzip $DIR/files_$TIME.zip -d /usr/local/glassfish4/glassfish/domains/domain1/files

echo "restore usages"
rm -rf /usr/local/glassfish4/glassfish/domains/domain1/logs/usage
unzip $DIR/usage_$TIME.zip -d /usr/local/glassfish4/glassfish/domains/domain1/logs

export LC_ALL=zh_CN.UTF-8
echo "start glassfish"
/usr/local/glassfish4/bin/asadmin start-domain domain1