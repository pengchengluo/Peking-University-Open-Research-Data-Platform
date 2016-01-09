
while [ 1 ]
  do
    TIME_SUFFIX=$(date +%Y%m%d%H%M%S)
    DIR=backup_$TIME_SUFFIX

    cd /home/postgres/
    echo "create directory: $DIR"
    mkdir $DIR

    cd $DIR
    echo 'Begin backup database'
    pg_dump dvndb_v4_0 > dvndb_v4_0_$TIME_SUFFIX.sql

    cd /usr/local/glassfish4/glassfish/domains/domain1/docroot/
    echo 'Begin backup logos'
    zip -rq /home/postgres/$DIR/logos_$TIME_SUFFIX.zip ./logos

    cd /usr/local/glassfish4/glassfish/domains/domain1/files
    echo 'Begin backup files'
    zip -rq /home/postgres/$DIR/files_$TIME_SUFFIX.zip ./10.18170

    cd /usr/local/glassfish4/glassfish/domains/domain1/logs
    echo 'Begin backup usage log'
    zip -rq /home/postgres/$DIR/usage_$TIME_SUFFIX.zip ./usage

    echo "crate remote directory: $DIR"
    ssh luopc@162.105.138.71 "cd ~/backup;mkdir $DIR"

    cd /home/postgres/$DIR
    echo 'Begin copy database backup to remote server'
    scp dvndb_v4_0_$TIME_SUFFIX.sql luopc@162.105.138.71:/home/luopc/backup/$DIR

    echo 'Begin copy logos to remote server'
    scp logos_$TIME_SUFFIX.zip luopc@162.105.138.71:/home/luopc/backup/$DIR

    echo 'Begin copy files to remote server'
    scp files_$TIME_SUFFIX.zip luopc@162.105.138.71:/home/luopc/backup/$DIR

    echo 'Begin copy logs to remote server'
    scp usage_$TIME_SUFFIX.zip luopc@162.105.138.71:/home/luopc/backup/$DIR

    echo 'wait 24h'
    sleep 24h
  done