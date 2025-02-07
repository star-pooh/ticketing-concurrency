## 동시성 문제 테스트 프로젝트

---

### 프로젝트 소개

스레드, DB, Redis 레벨에서 어떤 방법을 사용하여 동시성 문제를 해결하였는지 확인할 수 있는 프로젝트입니다.

구현 방법, 동시성 제어 방법 적용 전/후 결과, 해당 내용이 가질 수 있는 문제점 등에 대해 다룹니다.

---

### 기술 스택

![Spring Boot](https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
<br>
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

---

### 동시성 문제 해결 방법

<details>
<summary>스레드</summary>

<details>

<summary>synchronized</summary>

- 공유 자원이 사용되어 동기화가 필요한 부분을 `임계 영역` 이라고 칭함
- `임계 영역`에 `synchronized` 를 사용하면 여러 스레드가 동시에 접근하는 것을 막을 수 있음
    - 하나의 스레드가 임계 영역에 접근하여 사용할 때 lock이 걸려서 다른 스레드가 접근하지 못함

<br>

📢 테스트 내용은 100장의 티켓을 100개의 요청이 동시에 구매하는 경우

1. synchronized 적용 전

```
@Trancsaction
public BookingResponseDto bookTicket(Long concertId, BookingRequestDto dto) {
    Concert concert = concertRepository.findById(concertId)
            .orElseThrow(() -> new IllegalArgumentException("Concert not found"));

    concert.decreaseRemainTicketAmount();
    ...
}
```

![Image](https://github.com/user-attachments/assets/8258caa2-d953-4cea-beaf-50e00a1f6c27)
![Image](https://github.com/user-attachments/assets/9c837162-1c51-46fc-a5ca-494ebd74b9af)

💥 예상한대로 동시성 문제가 발생하여 티켓이 정상적으로 판매되지 않음

<br>

2. synchronized 적용 후

    - 티켓이 공유 자원이기 때문에 티켓을 조회하고 수정하는 bookTicket 메소드를 임계 영역으로 설정

```
@Trancsaction
public synchronized BookingResponseDto bookTicket(Long concertId, BookingRequestDto dto) {
    Concert concert = concertRepository.findById(concertId)
            .orElseThrow(() -> new IllegalArgumentException("Concert not found"));

    concert.decreaseRemainTicketAmount();
    ...
}
```

![Image](https://github.com/user-attachments/assets/922c61e3-b151-43ac-9a49-911f48c4d8f9)
![Image](https://github.com/user-attachments/assets/79ad1540-9df7-4bb8-ac37-340dd89f759d)

💥 synchronized를 사용했음에도 동시성 문제가 발생
    
- @Transactional과 synchronized를 함께 사용했기 때문
- 선언적 Tx는 AOP를 활용하여 프록시 객체를 사용하는 이 때, Advisor쪽에서 메소드의 시그니처 정보(메소드 이름, 파라미터 타입)을 사용
- synchronized는 메소드 시그니처에 포함되지 않음
- 따라서, 선언적 Tx가 만드는 프록시 객체에 synchronized가 존재하지 않기 때문에 동시성 문제가 발생

[Defining Methods (The Java™ Tutorials)](https://docs.oracle.com/javase/tutorial/java/javaOO/methods.html)

> **Definition:** Two of the components of a method declaration comprise the *method signature*—the method's name and the parameter types.

![Image](https://github.com/user-attachments/assets/4a457b1d-8fd1-4034-865d-fd2d72eced9b)

<br>

3. 선언적 Tx 제거, synchronized 적용 후

    - 티켓 수량이 줄어드는 것을 Dirty Checking으로 했기 때문에 save() 메소드 호출로 직접 저장

```
public synchronized BookingResponseDto bookTicket(Long concertId, BookingRequestDto dto) {
    Concert concert = concertRepository.findById(concertId)
            .orElseThrow(() -> new IllegalArgumentException("Concert not found"));

    concert.decreaseRemainTicketAmount();
    concertRepository.save(concert);
    ...
}
```

![Image](https://github.com/user-attachments/assets/5e99a5d7-4070-41a4-90a2-90ebe9c4c558)
![Image](https://github.com/user-attachments/assets/1827ce5a-c284-4960-85c2-6e52c46bc3fb)
테스트도 성공 했고 DB에서도 남은 티켓 수량이 0이며, 한 명이 하나의 예약 번호를 가지고 있는 것을 확인할 수 있으므로 synchronize로 동시성 문제가 해결되었다.

<br>

📢 테스트 내용은 30장의 티켓을 100개의 요청이 동시에 구매하는 경우

![Image](https://github.com/user-attachments/assets/162aef98-bb20-41ae-8907-9cc54d3b6743)
![Image](https://github.com/user-attachments/assets/29f3d384-201f-4aee-be67-08adc561d590)
또한 요청보다 적은 수량의 티켓의 경우에도 티켓은 동시성 문제가 발생하지 않고 전부 팔렸으며, 티켓이 전부 팔린 이후의 예외 처리도 문제 없이 동작하는 것을 확인 할 수 있었다.

<br>

4. 외부 서비스에 synchronized 적용 후 @Transactional이 선언된 메소드 호출 

   - 외부 서비스에 synchronized를 적용 후, @Transactional이 선언된 메소드를 호출한다면 프록시 객체를 각각 호출하게 되어 동시성과 JPA의 이점 모두 얻을 수 있을 것 같았다.

```
@Service
@RequiredArgsConstructor
public class BookingSynchronizedService {

    private final BookingService bookingService;

    public synchronized void synchronizedBookTicket(Long concertId, BookingRequestDto dto) {
        bookingService.bookTicket(concertId, dto);
    }
}
```

<br>

```
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ConcertRepository concertRepository;

    @Transactional
    public BookingResponseDto bookTicket(Long concertId, BookingRequestDto dto) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("Concert not found"));

        concert.decreaseRemainTicketAmount();

        Booking booking = Booking.builder()
                .concert(concert)
                .userId(dto.getUserId())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        return BookingResponseDto.builder()
                .concertId(savedBooking.getConcert().getId())
                .bookingOrder(savedBooking.getBookingOrder())
                .build();
    }
}
```

![Image](https://github.com/user-attachments/assets/5dc8e994-b2bf-4c66-8f42-6710691c2f36)
![Image](https://github.com/user-attachments/assets/b5acf8fb-f7ab-46c5-97f1-2c5bdab1d4aa)
![Image](https://github.com/user-attachments/assets/3a7eb325-aafb-436c-b0af-9af96b00537d)

synchronized와 @Transactional을 함께 사용해도 동시성이 보장되는 것을 확인하였다. 그런데 @Transactional이 제대로 적용되었는지 확인하고 싶었다. 
그래서 userId가 64번인 경우에는 예외를 던지도록 설정하여 롤백이 제대로 일어나는지 확인을 하려고 한다.
롤백이 제대로 일어난다면 테스트는 실패해야하고, DB에 남은 티켓은 1장이어야하며, 예약 확인 테이블에 userId가 64인 row는 없어야 한다.

```
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ConcertRepository concertRepository;

    @Transactional
    public BookingResponseDto bookTicket(Long concertId, BookingRequestDto dto) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("Concert not found"));

        concert.decreaseRemainTicketAmount();

        Booking booking = Booking.builder()
                .concert(concert)
                .userId(dto.getUserId())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        
        if (dto.getUserId() == 64) {
            throw new RuntimeException("RuntimeException CheckPoint");
        }

        return BookingResponseDto.builder()
                .concertId(savedBooking.getConcert().getId())
                .bookingOrder(savedBooking.getBookingOrder())
                .build();
    }
}
```

![Image](https://github.com/user-attachments/assets/f1247696-e29f-414e-8b4b-c8be34645c2a)
![Image](https://github.com/user-attachments/assets/c666f8b2-1726-4c25-ac68-4887a1f360d8)

예상대로 테스트는 실패했고, DB에 티켓은 1장이 남았으며, 예약 확인 테이블에 userId가 64인 row는 존재하지 않았다.
이로써 롤백이 제대로 이루어지는 것을 확인할 수 있었고, @Transactional이 선언된 메소드의 호출을 제대로 한다면 동시성과 JPA의 이점을 모두 챙길 수 있다는 것을 확인할 수 있었다.


> ❌ 하지만 synchronized는 하나의 스레드만 처리할 수 있도록 lock을 거는 것이기 때문에 마구잡이로 사용한다면 성능 저하를 일으킬 수 있다. 
> 또한, synchronized가 제어할 수 있는 동시성은 하나의 프로세스 내에서만 가능하기 때문에 두 개 이상의 프로세스에서는 해결책이 될 수 없다. 
> 즉, 다중 서버 환경에서는 동시성을 보장하기 위해 DB, Redis 등의 레벨에서 동시성 제어가 필요한 것이다.


</details>

</details>





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

낙관적 락의 특징은 락이 아니라는 것이다. 경쟁 상태가 발생한다면 해당 트랜잭션을 취소(롤백)하는 것이다. </br>
따라서, 경합이 많고 충돌이 많을 수록 트랜잭션을 중단할 가능성이 매우 크다.</br>
이를 위해 애플리케이션에서 진행될 때까지 재시도하는 로직을 구현할 수 도 있지만, 기다리는 시간이 늘어나기 때문에 서버로 요청을 보낸 클라이언트에서 재시도를 하도록 기획할 수 있다.</br>
또 주의할 것은, 롤백은 테이블 행과 레코드를 모두 포함할 수 있는 현재의 보류 중인 변경 사항을 모두 되돌려야 하므로 DB 시스템 비용이 많이 들 수 있다.</br>


- 낙관적 락에서의 교착상태(Dead-Lock) 발생 </br>
낙관적 락(Optimistic Lock)은 분명 명시적으로 DB의 Lock을 사용하지 않고, 버전 관리 기능을 사용한다. 이때 Lost Update문제는 해결 될 수 있으나 데드락이 발생할 수 있다.</br>
그렇다는 것은 Lock을 사용하고 있다는 뜻인데, 낙관적 락(DB Lock 사용 x)을 사용했을 뿐 명시적으로 DB Lock을 사용하지 않았다. 근데 왜 Lock이 사용됐을까?</br>

[MySQL 5.6 레퍼런스](https://dev.mysql.com/doc/refman/8.4/en/innodb-locks-set.html)   
- SELECT ... FROM is a consistent read, reading a snapshot of the database and setting no locks unless the transaction isolation level is set to SERIALIZABLE. 
- If a FOREIGN KEY constraint is defined on a table, any insert, update, or delete that requires the constraint condition to be checked sets shared record-level locks on the records that it looks at to check the constraint. InnoDB also sets these locks in the case where the constraint fails.

이유는 위에서 찾아볼 수 있었다.</br>

기존의 코드를 보면 쿼리는 </br>
1) concertId를 통해 예매할 콘서트를 select하고, 
2) booking 테이블에 데이터를 insert한 후,
3) concert의 남은 티켓 수를 update한다.
```
@Transactional
public BookingResponseDto bookTicketWithOptimisticLock(BookingRequestDto dto) {
    Concert concert = concertRepository.findByIdWithOptimisticLock(dto.getConcertId())
        .orElseThrow(() -> new IllegalArgumentException("Concert not found"));

    concert.decreaseRemainTicketAmount();

    Booking booking = Booking.builder()
        .concert(concert)
        .userId(dto.getUserId())
        .build();

    Booking savedBooking = bookingRepository.save(booking);

    return BookingResponseDto.builder()
        .concertId(savedBooking.getConcert().getId())
        .bookingOrder(savedBooking.getBookingOrder())
        .build();
}
```
Tx A와 Tx B가 있다고 하자.</br>
select를 할 때는 db내부에서 lock을 걸지 않는다. </br>
따라서, Tx A와 Tx B는 동시에 concert를 select하는 쿼리를 완료하고 booking에 insert하는 쿼리를 실행할 수 있다.</br>
그런데, booking에 insert를 할 때 concertId를 concert 테이블을 참조하는 외래키이기 때문에 해당 concert 레코드에 s-lock을 걸게 된다.</br>
그리고 s-lock은 개별적으로 동시에 걸 수 있다.</br>
즉, Tx A와 Tx B 모두 같은 concert 레코드에 s-lock을 걸게된다.</br>
결과적으로, Tx A와 Tx B는 해당 concert의 남은 티켓 수를 업데이트를 해야 하는 데, 서로 s-lock이 해제될 때까지 기다리며 데드락이 발생하게 된다.</br>


즉, fk가 걸려있는 데이터에 s-Lock이 자동으로 설정되기 때문에 서로 다른 트랜잭션이 같은 자원에 대해 Lock을 가지고 있으며, </br>
s-Lock과 x-Lock은 호환되지 않으므로 s-Lock이 풀릴때까지 대기한다. </br>
서로 다른 트랜잭션이 Lock을 해제할때까지 둘다 기다리고 있으니, 무한히 대기 상태에 빠지는 데드락이 발생하게 된다.</br>


이를 해결하기 위해서 쿼리 순서만 바꿔주면 된다.
```
concert.decreaseRemainTicketAmount();
concertRepository.flush();
```
이렇게 콘서트의 남은 티켓 수를 예매하고 바로 업데이트 쿼리를 날린다면,
1) concertId를 통해 예매할 콘서트를 select하고, 
2) concert의 남은 티켓 수를 updateg한 후
3) booking 테이블에 데이터를 insert한다.

2)에서 1)에서 얻은 version을 참고하여 충돌이 난다면 롤백을 시키기 때문에 데드락이 발생하지 않는다. </br>

추가로, concert를 조회할 때부터 락을 거는 방법도 있다.




</details>
  
<details><summary>비관적 락 (Pessimistic-Lock)</summary>
모든 트랜잭션은 충돌이 발생한다고 가정하고 우선 Lock을 걸자!

- 충돌이 자주 발생한다고 가정, 데이터 수정 경쟁이 심한 경우 사용하기 적합
- 데이터 조회시 락 활용 하는 방법으로 충돌 해결
- 주로 select for update 구문을 사용

s-Lock과 x-Lock은 호환되지 않기 때문에, fk 제약조건과 insert, update, delete 하는 쿼리로 인해 자동적으로 s-Lock과 x-Lock이 걸리면서 데드락이 발생하게 된다. </br>
그러면 처음부터 조회할 때 x-Lock을 걸어버리면, 데드락이 발생하지 않을 것이다.</br>
각 트랜잭션들이 순차적으로 작업을 마칠때까지 다른 트랜잭션이 접근하지 못하므로 미리 x-Lock을 걸어버리는 것으로 데드락을 방지할 수 있는 것이다.


<img width="1339" alt="스크린샷 2025-02-06 오후 8 17 34" src="https://github.com/user-attachments/assets/d8e2b65f-0a90-4328-9d13-64b5e55ec797" />

<img width="671" alt="스크린샷 2025-02-06 오후 8 17 46" src="https://github.com/user-attachments/assets/bcedcae4-fa00-454b-a454-91c6016d0146" />

그렇다면 비관적 락(Pessimistic Lock)은 데드락이 발생하지 않을까?</br>
어떻게 쿼리가 나가고 어떻게 테이블이 구현되는 지에 따라 다르다.</br>
서로 다른 트랜잭션이 각자 자원을 점유하고, 상대방이 가진 자원을 얻기위해 무한히 대기하는 데드락이 발생할 수 있다.</br>
근본적으로 어떤 락이든 락이 있으면 상황에 따라 데드락이 발생할 수 있다는 것에 유의해야 한다.</br>

비관적 락의 단점으로, 근본적인 락의 문제와 마찬가지로 불필요한 동기화가 발생할 수 있다.
이에 따라, blocking이 지속되고 타임아웃이 발생하거나 하나의 데이터베이스에서 많은 요청이 대기하게 되므로 과부하가 걸릴 수 있다.

또한, DB 락은 DB가 여러 개일 때 락이 작동하지 않는다.

그러면 이런 상황에는 또 어떻게 해결할까?
Redis 등 DB 밖에서 락을 걸 수 있는 기술을 사용할 수 있다.

</details>

</details>

<details>
<summary>Redis</summary>

<details>
   <summary>Lettuce</summary>
   Redis 레벨에서 lettues를 사용하여 동시성 문제를 해결하고자 하고자 했다.
   구현방법, 동시성 제어 방법 적용 전/후 결과, 해당 내용이 가질 수 있는 문제점 등에 대해 다룹니다.
   
   -----------------------------------------
   <details>
   <summary>Redis의 동시성</summary>
   <div markdown="1">
   
   Redis는 동시성 문제 해결을 위한 분산 락, 캐시 등으로 높은 동시 접속 및 빠른 응답이 필요한 환경에서 주로 쓰인다. Lettuce는 Java 애플리케이션에서 안전하게 스레드를 보호하며 비동기나 반응형 api를 지원해준다.
   
   </div>
   </details>
   
   📢 테스트 : 100명이 동시에 100개의 티켓을 예매하기
   
   ![Image](https://github.com/user-attachments/assets/f5738115-7127-40f6-a291-68176bc71b9b)
   
   100명의 유저 = 동시 요청 스레드 수
   초기 티켓 수 100장
   
   ![Image](https://github.com/user-attachments/assets/7e65a07b-3072-4117-9592-0bda0b2f8de1)
   
   결과가 64에서 끊기는 모습을 확인할 수 있다. 하여 콘솔에 로그에서 상태를 확인하고자 했다.
   
   ![Image](https://github.com/user-attachments/assets/6e711326-ba0a-463f-9268-97fa62651145)
   
   무언가가 롤백이 된 것을 볼 수 있었다.
   
   ![Image](https://github.com/user-attachments/assets/d132f75e-eaab-46a4-a76b-f3e151f6c233)
   
   그리고 롤백이 되기 이전의 로그에서 재시도 요청이 실패하는 과정이 로그에 보이는데. 
   이 로그 이후 롤백이 발생하는 것이 한 군데만 있지는 않았다는 점이다.
   
   ![Image](https://github.com/user-attachments/assets/48afb861-d22a-403d-b4d5-03e279630b85)
   
   이전의 로그는 콘솔의 맨 아래에서 확인했다면 이 로그의 스크롤 바가 상단에 위치해있음이 눈에 보인다.
   
   또한, 같은 커넥션의 숫자가 롤백되었단 것이며, 일정 재시도 숫자까지 락을 얻는 것을 실패한 이후란 것이다.
   
   ![Image](https://github.com/user-attachments/assets/9bcd70a3-501e-4a8b-aba9-89bf71b0d7b6)
   
   어느 한 쪽이 실패했을 때, 롤백이 되는 현상. 우린 그것을 db레벨에서 움직이는 트랜잭셔널에서 이미 본 적이 있었다.
   
   ![Image](https://github.com/user-attachments/assets/2d25a4e1-0676-4068-b4c1-f2c6ae97c7a3)
   
   물론 ttl을 충분히 길게 잡아준다거나, 재시도 횟수를 늘려준다면 아예 모두 성공을 해버려 그러한 상황이 일어나지 않겠지만...
   항상 그런 상황이 보장되리란 법은 없다.
   
   그렇다면 트랜잭셔널을 서비스 딴에서 걸었다고 왜 이러한 상황이 된 것이냐?
   
   두 가지 가능성을 떠올렸다.
   
   1. Redis 락과 트랜잭션 충돌
      Redis 락을 획득하는 부분과 트랜잭션을 사용하는 부분이 충돌하면서, 특정 스레드가 락을 얻었지만 이후 트랜잭션 롤백이 발생하며 정상적인 예약 처리가 이루어지지 않았을 가능성이 크다.
      Spring의 @Transactional 어노테이션을 사용하면 예외가 발생하면 자동으로 롤백되므로, Redis 락을 해제하기 전에 예외가 발생하면 락이 풀리지 않고 다른 스레드가 계속해서 대기한다.
   
   
   2. 재시도 전략과 트랜잭션이 동시에 동작하면서 충돌
      Lettuce 기반의 재시도 전략을 사용했지만, 트랜잭션 내부에서 락을 획득하면 롤백될 경우 락도 함께 해제될 수 있다.
      그러나 Redis 락을 관리하는 코드가 트랜잭션 외부에 있다면, 롤백과는 별개로 락이 해제되지 않아서 Deadlock이 발생할 수 있다.
   
   우연히 레디스 락을 관리하는 코드는 다른 클래스로 따로 빼낸 상태에서 호출하는 형식이었다.
   
   결국, DB 트랜잭션과 Redis 락 간 문제가 발생했다고 보여지며,
   Spring의 트랜잭션은 기본적으로 DB에 대한 롤백을 수행하지만, Redis 락은 별도의 외부시스템이므로
   결과만을 롤백될 뿐, 레디스쪽에서 이에 대해 특별한 조치를 취하진 않는다.
   
   
   그래서 롤백 로그만 뜰 뿐이며 결국 같은 조건 하에 db에 들어가는 값은 똑같은 경우가 있지 않냐는 궁금증이 생길만도 하다.
   트랜잭셔널을 제거한 이후 시도한 결과도 ttl와 재시도 횟수, 재시도를 기다리는 시간에 의해 같은 결과는 내긴 했지만...
   
   여기선 발생하진 않았지만 이미 예매에 성공한 유저의 티켓까지 트랜잭셔널에 묶여서
   로그상으로는 예매에 성공했으나, db에는 남지않는 결과를 예상할 수도 있다.
   
   이는 외부 시스템이 트랜잭션 커밋 후의 결과를 기반으로 동작한다면, 롤백이 발생할 경우 외부시스템과의 데이터 불일치가 발생할 수 있다는 뜻이다.
   
   ![Image](https://github.com/user-attachments/assets/8a9b7a00-46ea-405e-99d7-2663acf85ab6)
   
   마지막으로 성공한 사진을 올리며 마치겠다.
   
   </details>

<details>
    <summary>Redisson</summary>
      
      상황: 선착순 티켓 예매
      티켓 수 4개
      스레드 8개 실행
      원하는 결과: 4개까지 티켓 예매 후 나머지 예매 실패, 중복되지 않는 티켓 예매 등수

<img width="851" alt="image" src="https://github.com/user-attachments/assets/de206bcc-94de-4535-86e1-350d3c83e628" /></br>
userId = 3 이 먼저 락 획득 성공</br>
남은 티켓 수 감소 후 락 해제 </br>
</br>
<img width="200" alt="image" src="https://github.com/user-attachments/assets/d730b103-2eb7-49ee-ae09-a1bb3c5513e5" />

나머지 user들도 순차적으로 락을 획득하고 실행</br>
티켓 수가 0이 된 이후 user들은 예매 불가
   ```
   @Service
   @Slf4j
   @RequiredArgsConstructor
   public class RedissonService {
       private final RedissonClient redissonClient;
       private final BookingService bookingService;
   
   
       public BookingResponseDto executeWithLock(Long concertId, BookingRequestDto dto) {
           String lockKey = "concert_lock_" + concertId;
           RLock lock = redissonClient.getLock(lockKey);
   
           try {
               log.info("락 획득 시도: userId = {}", dto.getUserId());
               if (lock.tryLock(10, 3, TimeUnit.SECONDS)) {
                   try {
                       log.info("락 획득 성공: userId = {}", dto.getUserId());
                       return bookingService.bookWithRedissonLock(concertId, dto);
   
                   } catch (Exception e) {
                       RuntimeException noStackTraceException = new RuntimeException("예매 실패: " + e.getMessage());
                       noStackTraceException.setStackTrace(new StackTraceElement[0]); // 스택 트레이스 제거
                       throw noStackTraceException;
                   } finally {
                       if (lock.isHeldByCurrentThread()) {
                           log.info("락 해제: userId = {}", dto.getUserId());
                           lock.unlock();
                       }
                   }
               } else {
                   log.info("락 획득 실패: userId = {}", dto.getUserId());
                   throw new RuntimeException("현재 이용자가 많아 대기 중입니다. 잠시 후 다시 시도하세요.");
               }
           } catch (InterruptedException e) {
               throw new RuntimeException("예매 처리 중 오류 발생", e);
           }
       }
   }
   ```
레튜스와 다르 게 락 획득 대기하는 코드를 직접 짤 필요없이</br>
lock.tryLock(10, 3, TimeUnit.SECONDS) 이런 식으로 미리 구현된 인터페이스를 사용하면 된다.</br>
<details>
<summary>Lettuce는 락 획든 대기 코드를 직접 짜야한다. 보통 스핀 락으로</summary>
      
```
@Slf4j
@RequiredArgsConstructor
@Service
public class LettuceLockBookingService {
    private final BookingService bookingService;
    private final LockRedisRepository lockRedisRepository;

    private static final long SPIN_INTERVAL_MS = 100;

    public BookingResDto bookWithSpinLock(BookingReqDto bookingReqDto) {
        String lockKey = generateLockKey(bookingReqDto.getConcertId());

        while (!lockRedisRepository.acquireLock(lockKey, Duration.ofMillis(30000))) {
            try {
                Thread.sleep(SPIN_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("예매 프로세스가 중단되었습니다.", e);
            }
        }

        try {
            BookingResDto result = bookingService.bookWithLettuceLock(bookingReqDto);
            return result;
        } catch (Exception e) {
            log.error("예매 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Error e) {
            log.error("예매 중 치명적 에러 발생: {}", e.getMessage(), e);
            throw e;
        } finally {
            lockRedisRepository.releaseLock(lockKey);
        }
    }


    private String generateLockKey(Long concertId) {
        return "booking:lock:" + concertId;
    }
}
 ```

</details>
redisson은 내부적으로 pub/sub 방식으로 구현되어 있다.</br>
그리고 redisson은 watchdog이라는 모니터로 락을 물고 서버가 죽어도 락이 해제되게 하는 게 가능하다 (기본 30초 간격으로 확인). </br>
그러나 보통 redisson이나 lettuce나 비즈니스 로직이 정상적으로 작동하는 시간을 고려하여 ttl을 설정할 수 있다. </br>

| **비교 항목** | **Spin Lock** | **Pub/Sub 기반 락 (Redisson)** |
| --- | --- | --- |
| **CPU 사용량** | ❌ 높음 | ✅ 낮음 |
| **네트워크 부하** | ❌ 많음 (계속 Redis 요청) | ✅ 적음 (락 해제 시 알림만 전송) |
| **Redis 성능 영향** | ❌ 심함 (다중 요청) | ✅ 낮음 (Pub/Sub 이벤트만 전송) |

</br>
추가로 락을 적용할 때, 비즈니스 로직이 감싸져 있는 형태이기 때문에 </br>
AOP를 사용할 수 있다.

```
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(0)
public class RedissonLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(redissonLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs(); // 메서드의 실제 인자 값들
        Parameter[] parameters = method.getParameters(); // 메서드의 매개변수 정보

        // 락 키를 찾아 설정
        String lockKey = null;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RedissonLockParam.class)) {
                lockKey = redissonLock.prefix() + args[i]; // prefix + 실제 파라미터 값
                break;
            }
        }

        if (lockKey == null) {
            throw new IllegalArgumentException("RedissonLock이 선언된 메서드에는 @RedissonLockParam이 있는 파라미터가 필요합니다.");
        }

        long threadId = Thread.currentThread().getId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            log.info("락 획득 시도: threadId = {}", threadId);
            if (lock.tryLock(10, 3, TimeUnit.SECONDS)) {
                try {
                    log.info("락 획득 성공: threadId = {}", threadId);
                    return joinPoint.proceed(); // 원래 메서드 실행
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        log.info("락 해제: threadId = {}", threadId);
                        lock.unlock();
                    }
                }
            } else {
                log.info("락 획득 실패: threadId = {}", threadId);
                throw new RuntimeException("현재 이용자가 많아 대기 중입니다. 잠시 후 다시 시도하세요.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("예매 처리 중 오류 발생", e);
        }
    }
}
```

주의할점. 트랜잭션이 적용되는 범위 밖에서 먼저 실행되도록 설정해야 한다.

왜냐, Transactional이 적용되는 범위가 끝날 때 commit이 되고 데이터베이스에 실제로 적용이 되기 때문에.
위 코드에서는 @Order(0)을 이용해 @RedissonLock이 항상 가장 바깥에서 실행되도록 한다.

@RedissonLock 사용 사례
```
@RedissonLock
@Transactional
public BookingResponseDto bookWithRedissonLock(@RedissonLockParam Long concertId, BookingRequestDto bookingReqDto) {
    Concert concert = concertRepository.findById(concertId)
            .orElseThrow(() -> new IllegalArgumentException("Concert not found"));

    concert.decreaseRemainTicketAmount();

    Booking booking = Booking.builder()
            .concert(concert)
            .userId(bookingReqDto.getUserId())
            .build();

    Booking savedBooking = bookingRepository.save(booking);

    return BookingResponseDto.builder()
            .concertId(savedBooking.getConcert().getId())
            .bookingOrder(savedBooking.getBookingOrder())
            .build();
	}
}
```

1000개도 문제 없이 작동한다
<img width="851" alt="image" src="https://github.com/user-attachments/assets/f837c7ca-efac-45a6-9526-d31f06034429" />
<img width="847" alt="image" src="https://github.com/user-attachments/assets/dd39a0f5-3409-4221-8b4a-a84e8fec80b6" />

<details>
<summary>동기화로 작동하다 보니 요청 수가 많아질수록 모든 요청이 완료되는 걸 기다리기 까지 오래 걸린다.</summary>
하지만 먼저 완료된 요청은 먼저 나가서 선착순 티겟팅의 특성상 큰 문제가 발생할 것 같진 않다. 그래도 이미 티켓 수가 남지않았는데 락 획득을 기다리는 것이 충분히 비효율적인 상황이라면 배치단위로 받아서 끊어내는 방법이 있을 것 같기도 하다.

</details>

비교

1000개 요청</br>
<img width="844" alt="image" src="https://github.com/user-attachments/assets/559eaf25-5efe-4666-8584-5335d79c37be" /></br>
10개 요청</br>
<img width="847" alt="image" src="https://github.com/user-attachments/assets/67ce81c8-110f-46d9-847d-92039ebcf696" />

</details>
    
</details>

