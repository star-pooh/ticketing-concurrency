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
    
- @Transaction과 synchronized를 함께 사용했기 때문
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

> ❌ 하지만 synchronized는 하나의 프로세스 내에서만 동시성을 제어할 수 있기 때문에 두 개 이상의 프로세스에서는 해결책이 되지 못한다.
> 즉, 다중 서버 환경에서는 동시성을 보장 받기 위해 DB, Redis 등의 레벨에서 동시성 제어가 필요한 것이다.


</details>

</details>