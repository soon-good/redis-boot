# Spring Boot 에서 Redis 사용해보기 (spring-data-redis)

# 참고자료

## 메인 자료

> 메인으로 참고한 자료

- [사바라다는 차곡차곡](https://sabarada.tistory.com)
  - 메인으로 참고한 자료. 기본적인 컨셉을 참고할 수 있어서 유용하게 참고했다.
  - 이분 블로그를 자주 보다보면 공식 문서에서 필요한 부분을 어떻게 찾아서 적용하는지 조금씩 적응이 되는 것 같음
  - 블로그 글을 먼저 보지 않았다면 공식 문서를 처음부터 끝까지 정독하다가 지쳤을 듯.

- [사바라다는 차곡차곡 - RedisTemplate](https://sabarada.tistory.com/105)
- [사바라다는 차곡차곡 - RedisRepository](https://sabarada.tistory.com/106)



## 공식문서

> Spring Data Redis 의 공식문서 버전은 2.4.6 을 참고했다. spring boot starter parent 의 내부동작으로 잡아주는 spring data redis 의 버전은 한단계 아래이지 않을까 싶기는 하다. 

- Spring Data Redis
  - [docs.spring.io/spring-data/Spring Data Redis - learn (2.4.6 GA)](https://docs.spring.io/spring-data/redis/docs/2.4.6/reference/html/#reference)
- RedisTemplate 
  - [docs.spring.io/spring-data/Spring Data Redis - learn (2.4.6 GA #Working with Objects through RedisTemplate)](https://docs.spring.io/spring-data/redis/docs/2.4.6/reference/html/#redis:template)
- Redis Repository
  - [docs.spring.io/spring-data/Spring Data Redis - learn (2.4.6.GA) #Redis Repositories](https://docs.spring.io/spring-data/redis/docs/2.4.6/reference/html/#reference)
- RedisTemplate 클래스 명세 
  - [Spring Data Redis DOCS - API Guide](https://docs.spring.io/spring-data/redis/docs/2.4.6/api/org/springframework/data/redis/core/RedisTemplate.html)



## Redis 개념 관련된 자료들

- [Redis Sorted Set](https://jupiny.com/2020/03/28/redis-sorted-set/)
  - ZSet 의 개념에 대해서 설명해주고 있다.



# Spring Data Redis

Redis 를 스프링에서 사용할 때 보통은 먼저 아래의 두가지 방식 중 하나를 선택한다.

- RedisTemplate 를 사용하는 간단한 key/value store 방식
  - RedisTemplate 을 이용한 방식이다. 
  - 이 방식에 대해서는 추후에 세부 문서로 따로 정리할 예정이다.
- RedisRepository 를 사용한 key/value store 방식
  - 객체 모델링을 활용한 방식이다.
  - 이 방식에 대해서는 추후에 세부 문서에 따로 정리할 예정이다.



# docker 기반 redis 구동하기

이전에 만들어둔 redis docker 구동/정지/삭제/조회/접속 용도의 쉘 스크립트가 있어서 이것을 활용했다.([리포지터리](https://github.com/soongujung/docker-scripts))

## docker redis 구동 스크립트

쉘 스크립트를 구동시키면 redis docker 컨테이너가 구동되도록 docker-redis-start.sh 라는 이름의 쉘 스크립트를 만들어 두었다. 한번 더 실행시키면 이미 돌아가고 있는 컨테이너와 볼륨을 삭제하고 재기동한다.

```bash
source docker-redis-start.sh
```



redis docker 컨테이너를 구동시키는 명령어를 정리해둔 쉘 스크립트인 docker-redis-start.sh 파일의 내용은 아래와 같다.  

**docker-redis-start.sh**

```bash
# redis docker container 구동 스크립트

name_codingtest_redis='codingtest-redis'
cnt_codingtest_redis=`docker container ls --filter name=codingtest-redis | wc -l`
cnt_codingtest_redis=$(($cnt_codingtest_redis -1))

if [ $cnt_codingtest_redis -eq 0 ]
then
    echo "'$name_codingtest_redis' 컨테이너를 구동시킵니다.\n"

    # 디렉터리 존재 여부 체크 후 없으면 새로 생성
    DIRECTORY=~$USER/env/docker/codingtest/volumes/codingtest-redis
    test -f $DIRECTORY && echo "볼륨 디렉터리가 존재하지 않으므로 새로 생성합니다.\n"

    if [ $? -lt 1 ]; then
      mkdir -p ~$USER/env/docker/codingtest/volumes/codingtest-redis
    fi

    # mariadb 컨테이너 구동 & 볼륨 마운트
    docker container run --rm -d -p 6379:6379 --name codingtest-redis \
                -v ~/env/docker/codingtest/volumes/codingtest-redis:/usr/local/etc/redis \
                -d redis:latest

else
    echo "'$name_codingtest_redis' 컨테이너가 존재합니다. 기존 컨테이너를 중지하고 삭제합니다."
    # 컨테이너 중지 & 삭제
    docker container stop codingtest-redis

    # 컨테이너 볼륨 삭제
    rm -rf ~/env/docker/codingtest/volumes/codingtest-redis/*
    echo "\n'$name_codingtest_redis' 컨테이너 삭제를 완료했습니다.\n"

    # 디렉터리 존재 여부 체크 후 없으면 새로 생성
    DIRECTORY=~$USER/env/docker/codingtest/volumes/codingtest-redis
    test -f $DIRECTORY && echo "볼륨 디렉터리가 존재하지 않으므로 새로 생성합니다.\n"

    if [ $? -lt 1 ]; then
      mkdir -p ~$USER/env/docker/codingtest/volumes/codingtest-redis
    fi

    # redis 컨테이너 구동 & 볼륨 마운트
    echo "'$name_codingtest_redis' 컨테이너를 구동시킵니다."
    docker container run --rm -d -p 6379:6379 --name codingtest-redis \
                -v ~/env/docker/codingtest/volumes/codingtest-redis:/usr/local/etc/redis \
                -d redis:latest
fi
```



## docker redis 정지/삭제 스크립트

쉘 스크립트를 구동시키면 redis docker 컨테이너를 정지시키도록 docker-redis-stop.sh 라는 이름의 쉘 스크립트를 만들어 두었다. 삭제시에 볼륨도 함께 삭제된다.

```bash
source docker-redis-stop.sh
```



redis docker 컨테이너를 정지시키는 명령어를 정리해둔 쉘 스크립트인 docker-redis-stop.sh 파일의 내용은 아래와 같다.  

```bash
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

```



## docker redis 조회/접속 스크립트

redis docker 컨테이너를 조회하거나, 인스턴스에 접속하는 도커 컨테이너 명령어를 쉘 스크립트에 따로 모아두었다. 

### 도커 컨테이너 상태 조회 스크립트

```bash
# redis docker container 상태 확인
docker container ls --filter name=codingtest-redis
```



위의 스크립트는 docker-redis-ls.sh 라는 파일로 저장해두었고, 이것은 아래의 명령어로 실행하능하다.

```bash
source docker-redis-ls.sh
```





### 도커 컨테이너 인스턴스 접속 스크립트

```bash
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
```



위의 스크립트는 docker-redis-repl.sh 라는 파일로 저장해두었고, 이것은 아래의 명령어로 실행하능하다.

```bash
source docker-redis-ls.sh
```

