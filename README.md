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


    @Trancsaction
    public BookingResponseDto bookTicket(Long concertId, BookingRequestDto dto) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("Concert not found"));

        concert.decreaseRemainTicketAmount();
        ...
    }

![Image](https://github.com/user-attachments/assets/8258caa2-d953-4cea-beaf-50e00a1f6c27)
![Image](https://github.com/user-attachments/assets/9c837162-1c51-46fc-a5ca-494ebd74b9af)

💥 예상한대로 동시성 문제가 발생하여 티켓이 정상적으로 판매되지 않음

<br>

2. synchronized 적용 후

    - 티켓이 공유 자원이기 때문에 티켓을 조회하고 수정하는 bookTicket 메소드를 임계 영역으로 설정


    @Trancsaction
    public synchronized BookingResponseDto bookTicket(Long concertId, BookingRequestDto dto) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("Concert not found"));

        concert.decreaseRemainTicketAmount();
        ...
    }

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


    public synchronized BookingResponseDto bookTicket(Long concertId, BookingRequestDto dto) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("Concert not found"));
    
        concert.decreaseRemainTicketAmount();
        concertRepository.save(concert);
        ...
    }

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


    @Service
    @RequiredArgsConstructor
    public class BookingSynchronizedService {

        private final BookingService bookingService;

    public synchronized void synchronizedBookTicket(Long concertId, BookingRequestDto dto) {
        bookingService.bookTicket(concertId, dto);
    }

<br>

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

![Image](https://github.com/user-attachments/assets/5dc8e994-b2bf-4c66-8f42-6710691c2f36)
![Image](https://github.com/user-attachments/assets/b5acf8fb-f7ab-46c5-97f1-2c5bdab1d4aa)
![Image](https://github.com/user-attachments/assets/3a7eb325-aafb-436c-b0af-9af96b00537d)

synchronized와 @Transactional을 함께 사용해도 동시성이 보장되는 것을 확인하였다. 그런데 @Transactional이 제대로 적용되었는지 확인하고 싶었다. 
그래서 userId가 64번인 경우에는 예외를 던지도록 설정하여 롤백이 제대로 일어나는지 확인을 하려고 한다.
롤백이 제대로 일어난다면 테스트는 실패해야하고, DB에 남은 티켓은 1장이어야하며, 예약 확인 테이블에 userId가 64인 row는 없어야 한다.

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

![Image](https://github.com/user-attachments/assets/f1247696-e29f-414e-8b4b-c8be34645c2a)
![Image](https://github.com/user-attachments/assets/c666f8b2-1726-4c25-ac68-4887a1f360d8)

예상대로 테스트는 실패했고, DB에 티켓은 1장이 남았으며, 예약 확인 테이블에 userId가 64인 row는 존재하지 않았다.
이로써 롤백이 제대로 이루어지는 것을 확인할 수 있었고, @Transactional이 선언된 메소드의 호출을 제대로 한다면 동시성과 JPA의 이점을 모두 챙길 수 있다는 것을 확인할 수 있었다.


> ❌ 하지만 synchronized는 하나의 스레드만 처리할 수 있도록 lock을 거는 것이기 때문에 마구잡이로 사용한다면 성능 저하를 일으킬 수 있다. 
> 또한, synchronized가 제어할 수 있는 동시성은 하나의 프로세스 내에서만 가능하기 때문에 두 개 이상의 프로세스에서는 해결책이 될 수 없다. 
> 즉, 다중 서버 환경에서는 동시성을 보장하기 위해 DB, Redis 등의 레벨에서 동시성 제어가 필요한 것이다.


</details>

</details>