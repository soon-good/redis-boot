# mariadb docker container 중지 및 볼륨 삭제 스크립트

name_codingtest_redis='codingtest-redis'

cnt_codingtest_redis=`docker container ls --filter name=codingtest-redis | wc -l`
cnt_codingtest_redis=$(($cnt_codingtest_redis -1))

if [ $cnt_codingtest_redis -eq 0 ]
then
    echo "'$name_codingtest_redis' 컨테이너가 없습니다. 삭제를 진행하지 않습니다."

else
    echo "'$name_codingtest_redis' 컨테이너가 존재합니다. 기존 컨테이너를 중지하고 삭제합니다."
    docker container stop codingtest-redis
    rm -rf ~/env/docker/codingtest/volumes/codingtest-redis/*
    echo "\n'$name_codingtest_redis' 컨테이너 삭제를 완료했습니다.\n"
fi
