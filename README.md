<details>
<summary>DB-Lock</summary>
  
<img width="1327" alt="동시성 문제 발생" src="https://github.com/user-attachments/assets/5d452b27-2a91-4b61-87fb-12dfe5c975a9" />

<img width="687" alt="동시성 문제 발생 DB" src="https://github.com/user-attachments/assets/99a0e8f8-ff8b-43c6-bf2c-0e06777629fa" />

Lock을 걸지 않으면 발생하는 동시성 문제.
100개의 티켓을 동시에 100개의 요청이 들어오면 동시성 문제가 발생하면서 사진과 같이 13개의 요청만 실행되었다.

  

<details><summary>낙관적 락 (Optimistic-Lock)</summary>
자원에 Lock을 걸지 않고, 동시성 문제가 발생하면 그때 처리하자!

- 충돌이 드물다고 가정, 읽기 작업이 많고 충돌이 적은 경우 사용하기 적합
- 수정할때 버전 확인 후 처리 하는 방법으로 충돌 해결

낙관적 락의 단점은 재시도 로직을 만들어야 한다는 점인데, 이는 충돌이 발생했을 때 DB가 아닌 Application 단에서 처리해야 한다는 특징 때문이다.
낙관적 락은 경합이 많고 충돌이 많을 수록 트랜잭션을 중단할 가능성이 매우 크고,
롤백은 테이블 행과 레코드를 모두 포함할 수 있는 현재의 보류 중인 변경 사항을 모두 되돌려야 하므로 DB 시스템 비용이 많이 들 수 있다.

- 낙관적 락에서의 교착상태(Dead-Lock) 발생
낙관적 락(Optimistic Lock)은 분명 DB의 Lock을 사용하지 않고, 애플리케이션 레벨에서 버전 관리 기능을 사용한다. 그런데도 데드락이 발생할 수 있다.
그렇다는 것은 Lock을 사용하고 있다는 뜻인데, 낙관적 락(DB Lock 사용 x)을 사용했을 뿐 DB Lock을 사용하지 않았다. 근데 왜 Lock이 사용됐을까?

[MySQL 5.6 레퍼런스](https://dev.mysql.com/doc/refman/8.4/en/innodb-locks-set.html)   

요약하면, s-Lock이 사용된 이유는 fk가 있는 테이블에서, fk를 포함한 데이터를 insert, update, delete 하는 쿼리는 제약조건을 확인하기 위해 shared lock(s-Lock)을 설정한다고 한다.
x-Lock이 사용된 이유는 Update 쿼리에 사용되는 모든 레코드에 exclusive lock(x-Lock)을 설정한다고 한다.

즉, fk가 걸려있는 데이터에 s-Lock이 자동으로 설정되기 때문에 서로 다른 트랜잭션이 같은 자원에 대해 Lock을 가지고 있으며, s-Lock과 x-Lock은 호환되지 않으므로 s-Lock이 풀릴때까지 대기한다. 서로 다른 트랜잭션이 Lock을 해제할때까지 둘다 기다리고 있으니, 무한히 대기 상태에 빠지는 데드락이 발생하게 된다.

<img width="867" alt="재시도 로직 없는 낙관적락" src="https://github.com/user-attachments/assets/73166c5c-4f50-4acc-8c0b-dc73def7a318" />

<img width="1333" alt="재시도로직없는 낙관적 락 실패" src="https://github.com/user-attachments/assets/f8fab449-b781-428e-aefc-13045e7854c5" />

<img width="679" alt="재시도로직없는 낙관적락 DB" src="https://github.com/user-attachments/assets/b96262e2-a80e-41fe-9d16-48bc2720fda3" />

이러한 충돌을 재시도 로직을 통해 어느 정도 해결할 수 있다.

<img width="547" alt="스크린샷 2025-02-06 오후 7 57 02" src="https://github.com/user-attachments/assets/2baa82d0-9ac8-4ba3-92b6-d5fe19d72adb" />

<img width="1341" alt="스크린샷 2025-02-06 오후 8 06 45" src="https://github.com/user-attachments/assets/12fd74de-13a8-4c7a-abb6-724c7629b169" />

<img width="681" alt="스크린샷 2025-02-06 오후 8 05 39" src="https://github.com/user-attachments/assets/4e278a4e-ba29-4111-95ed-f4b84530380f" />

재시도 로직을 적용한 결과, 100개의 티켓을 동시에 100개의 요청으로 테스트한 결과 정상적으로 성공하는 것을 확인했다.



</details>
  
<details><summary>비관적 락 (Pessimistic-Lock)</summary>
모든 트랜잭션은 충돌이 발생한다고 가정하고 우선 Lock을 걸자!

- 충돌이 자주 발생한다고 가정, 데이터 수정 경쟁이 심한 경우 사용하기 적합
- 데이터 조회시 락 활용 하는 방법으로 충돌 해결
- 주로 select for update 구문을 사용

s-Lock과 x-Lock은 호환되지 않기 때문에, fk 제약조건과 insert, update, delete 하는 쿼리로 인해 자동적으로 s-Lock과 x-Lock이 걸리면서 데드락이 발생하게 된다.
그러면 처음부터 조회할 때 x-Lock을 걸어버리면, 데드락이 발생하지 않을 것이다.
각 트랜잭션들이 순차적으로 작업을 마칠때까지 다른 트랜잭션이 접근하지 못하므로 미리 x-Lock을 걸어버리는 것으로 데드락을 방지할 수 있는 것이다.

<img width="1339" alt="스크린샷 2025-02-06 오후 8 17 34" src="https://github.com/user-attachments/assets/d8e2b65f-0a90-4328-9d13-64b5e55ec797" />

<img width="671" alt="스크린샷 2025-02-06 오후 8 17 46" src="https://github.com/user-attachments/assets/bcedcae4-fa00-454b-a454-91c6016d0146" />

그렇다면 비관적 락(Pessimistic Lock)은 데드락이 발생하지 않을까?
먼저 접근하는 트랜잭션이 row에 락을 걸어버리니까 데드락 문제가 발생할 수 없지 않을까라고 생각했지만, 특정 상황에 따라 데드락 문제는 발생할 수 있다.
서로 다른 트랜잭션이 각자 자원을 점유하고, 상대방이 가진 자원을 얻기위해 무한히 대기하는 데드락이 발생할 수 있다.

그리고 비관적 락의 단점으로, Lock이 필요하지 않은 상황에서도 모든 트랜잭션에 대해 Lock을 사용하기 때문에 트래픽이 많은 경우에 성능이 저하된다는 문제점이 있다. 즉 다른 요청들이 다 blocking 돼서 타임아웃 날 수 있다.
여전히 발생할 수 있는 데드락의 예시로 선착순 쿠폰 발급 시스템처럼 동시에 많은 트래픽이 몰리거나, 여러 테이블에 Lock을 걸면서 데드락 이슈가 발생하는 경우에는 비관적 락으로도 해결할 수 없다.

그러면 이런 상황에는 또 어떻게 해결할까?
-> Redis Lock 등등


</details>
