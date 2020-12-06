# :boxing_glove: Git Rules

## :family_man_woman_girl_girl: Git Brach

### Git Flow 사용

### **git flow 설치**

- wget 설치

  - 7z 압축풀기
  - wget.exe를 C:\Windows\System32 위치로

- git flow 설치

  cmd 관리자 권한으로 실행

  ```
  wget -q -O - --no-check-certificate <https://github.com/nvie/gitflow/raw/develop/contrib/gitflow-installer.sh>
  ```

### **프로젝트 시작**

```
$ git clone <https://lab.ssafy.com/s03-webmobile3-sub2/s03p12b308.git>
$ cd s03p12b308
$ git flow init
```

이후에 나오는 질문들은 모두 enter쳐서 넘어감

### **작업 시작하기 전**

- 작업하는 이슈번호 확인 및 상태 변경 (진행중) [https://jira.ssafy.com/secure/Dashboard.jspa]

- feature 브랜치 생성

  - [기능명]은 Jira의 Epic을 기준

    예) login, survey

```
$ git flow feature start [기능명]
# 자동으로 checkout 됨
$ git flow feature publish [기능명]
# 원격저장소에 feature/[기능명] 브랜치 생성
```

_(feature branch를 공동 작업할 경우)_

```
$ git flow feature pull origin [기능명]
# 원격이랑 연결x
```

### **feature에서의 작업**

```
$ git add .
$ git commit -m "S03P12B308-[이슈번호] [코멘트]"
```

- 코멘트는 동사로 시작

  예) add [Readme.md](http://readme.md/) , implement category UI

- 이슈번호는 Jira의 Story/Task 이슈번호를 기준

- commit의 단위, 횟수는 작을 수록 좋음 (같은 이슈번호에 대해서 여러개의 커밋해도 됨)

```
$ git push --set-upstream origin feature/기능명

# git publish 했으면 git push만 해도 됨
```

### **feature의 기능 구현을 마치면**

```
$ git flow feature finish [기능명]
# merge feature into develop (로컬)
# feature branch 삭제
# checkout develop
```

vim 나오면 :wq! 입력 (commit작업을 한 개만 했을 때는 안 나옴)

```
$ git push
# develop(local) -> develop(원격저장소)
```

- merge requests

https://lab.ssafy.com/s03-webmobile3-sub2/s03p12b308/merge_requests

여기서 feature/[기능명] → develop으로 merge request

팀장이 merge하고 원격 브랜치 삭제할 것

local 브랜치는 각자 삭제.

- Jira에서 issue 상태 변경해줄 것(완료)

### **\*Maintainer\***

- 새로운 저장소 만들어지면

```
$ git clone [git주소]
$ cd [폴더이름]
$ touch Readme.md
$ git add Readme.md
$ git commit -m "first commit"
$ git push -u origin master
$ git push origin develop
# 원격저장소에 develop 브랜치 생성
$ git flow init
$ git branch --set-upstream-to=origin/develop develop
# 원격 - local develop 브랜치 추적
```

- develop into master

  - Sprint 마다의 최종 개발분을 반영

- 권한 설정

  - develop branch에 merge는 Maintainer만 가능
  - (설정했는데 developers도 merge되버림............코치님한테 답변 대기중)

  ### https://www.holaxprogramming.com/2018/11/01/git-commands/ 참고URL

## :call_me_hand: Comit message

Commit 예시 :fire: S03P31B201 start project

1. Jira 이슈 번호 붙이기

2. 명령형으로 작성하기

3. (Option) 중요 커밋 메시지나 Jira 이슈가 없는 커밋 메세지는 앞에 emoji 붙이기(https://gitmoji.carloscuesta.me/)

### :fountain_pen: gitmoji 종류

1. 프로젝트 및 문서

\- :fire:(fire): 프로젝트 시작

\- :memo:(memo): 텍스트 문서 수정

\- :pushpin:(pushpin): .gitignore 추가/수정

2. 디펜던시

\- ➕(heavy_plus_sign): 디펜던시 추가

\- ➖(heavy_minus_sign): 디펜던시 삭제

3. 코드 관련

\- :building_construction:(building_construction): 브랜치 생성

\- :twisted_rightwards_arrows:(twisted_rightwards_arrows): 브랜치 머지

\- :bulb:(bulb): 기능 추가

\-:hammer:(hammer): 코드 수정

\- :arrow_up:(arrow_up): push

\- :arrow_down:(arrow_down): pull

\- :wastebasket:(wastebasket): 코드나 파일 삭제

\- :boom:(boom): 긴급수정, hotfix

4. QA 및 테스트

\- :white_check_mark:(white_check_mark): 테스트 추가 및 수정

\- :bug:(bug): 버그 감지 및 해결

\- :mag:(mag): 코드 리뷰 후 코드 수정

\- :alien:(alien): 에일리언 코드

\- :wrench:(wrench): 충돌해결
