# redis docker container repl 접속 스크립트

name_codingtest_redis='codingtest-redis'

cnt_codingtest_redis=`docker container ls --filter name=codingtest-redis | wc -l`
cnt_codingtest_redis=$(($cnt_codingtest_redis -1))

if [ $cnt_codingtest_redis -eq 0 ]
then
    echo "'$name_codingtest_redis' 컨테이너가 없습니다. 컨테이너를 구동해주세요."

else
    echo "'$name_codingtest_redis' 컨테이너의 BASH 쉘 접속을 시작합니다."
    docker container exec -it codingtest-redis sh
fi
