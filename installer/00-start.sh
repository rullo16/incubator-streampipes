#! /bin/bash
mv -f ./SP-log.log ./SP-log.1
#cat /dev/null > ./SP-log.log
#nohup ./cli/streampipes up 2>&1 1>./SP-log.log &
./cli/streampipes up -d
