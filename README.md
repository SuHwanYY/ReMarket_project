# ReMarket

Android와 Node.js 기반으로 개발한 중고거래 플랫폼 애플리케이션입니다.

Firebase와 같은 BaaS를 사용하지 않고 Android 앱, REST API 서버, MySQL 데이터베이스를 직접 구축하여 실제 서비스와 유사한 구조를 경험하는 것을 목표로 개발했습니다.

---

# 프로젝트 소개

ReMarket는 사용자가 상품을 등록하고, 조회하고, 찜할 수 있는 중고거래 플랫폼입니다.

클라이언트(Android)와 서버(Node.js)를 분리하여 개발하였으며, REST API를 통해 데이터를 주고받도록 설계하였습니다.

회원 인증에는 JWT를 적용하였고, 상품 관리 및 찜 기능을 구현하여 CRUD와 인증 기반 서비스 구조를 학습하였습니다.

---

# 개발 목표

* MVVM 아키텍처 적용
* Retrofit 기반 REST API 통신 구현
* JWT 인증 방식 이해
* MySQL 데이터베이스 설계 경험
* Android와 Backend 간 역할 분리 경험
* 실제 서비스와 유사한 구조 설계

---

# 기술 스택

## Android

* Kotlin
* MVVM Architecture
* ViewModel
* ViewBinding
* Retrofit2
* Navigation Component
* RecyclerView
* ViewPager2

## Backend

* Node.js
* Express

## Database

* MySQL
* Prisma ORM

## Authentication

* JWT(Json Web Token)

## Version Control

* Git
* GitHub

---

# 시스템 구조

```text
Android App
    │
Retrofit
    │
REST API
    │
Node.js + Express
    │
Prisma ORM
    │
MySQL
```

---

# 주요 기능

## 회원 관리

* 회원가입
* 로그인
* 자동 로그인
* 로그아웃
* JWT 기반 사용자 인증

## 상품 관리

* 상품 등록
* 상품 조회
* 상품 수정
* 상품 삭제
* 상품 상세 조회

## 상품 이미지

* 다중 이미지 등록
* ViewPager2 기반 이미지 슬라이드

## 찜 기능

* 상품 찜 등록
* 찜 목록 조회
* 찜 해제

## 마이페이지

* 프로필 조회
* 프로필 수정
* 내가 등록한 상품 조회

## 판매 관리

* 판매중
* 예약중
* 판매완료

상태 변경 기능 제공

## 검색

* 상품 검색 기능

---

# 프로젝트 구조

```text
app
 ├── auth
 ├── model
 ├── network
 ├── repository
 ├── ui
 └── viewmodel
```

### auth

회원가입, 로그인, 세션 관리

### network

Retrofit API 통신

### repository

데이터 처리 및 네트워크 로직 분리

### viewmodel

UI 상태 관리

### ui

화면 구성 및 사용자 인터랙션 처리

---

# 데이터베이스 설계

## User

* 이메일
* 비밀번호
* 닉네임
* 성별
* 지역

## Product

* 상품명
* 설명
* 가격
* 카테고리
* 판매 상태

## ProductImage

* 상품 이미지 관리

## Wishlist

* 사용자 찜 목록 관리

---

# 아키텍처

MVVM 패턴을 적용하여 UI와 비즈니스 로직을 분리하였습니다.

ViewModel을 통해 화면 상태를 관리하고 Repository 계층을 통해 네트워크 통신을 처리하여 유지보수성과 확장성을 높였습니다.

---

# 트러블 슈팅

## Firebase 대신 직접 서버 구축

초기에는 Firebase를 사용할 수도 있었지만 서버 개발 경험을 쌓기 위해 Node.js와 MySQL을 활용하여 별도의 백엔드 서버를 구축하였습니다.

이를 통해 클라이언트와 서버 간 역할 분리와 REST API 구조를 이해할 수 있었습니다.

---

## JWT 인증 처리

로그인 후 발급받은 토큰을 저장하고 인증이 필요한 API 요청 시 헤더에 포함하도록 구현하였습니다.

이를 통해 상태 기반 인증 방식과 토큰 인증 방식의 차이를 학습하였습니다.

---

## MVVM 적용

UI 코드와 데이터 처리 로직을 분리하기 위해 MVVM 패턴을 적용하였습니다.

ViewModel과 Repository를 통해 화면과 비즈니스 로직의 의존성을 낮추고 유지보수성을 향상시켰습니다.

---

# 프로젝트를 통해 배운 점

* Android MVVM 구조 설계 경험
* Retrofit 기반 API 통신 경험
* JWT 인증 방식 이해
* MySQL 데이터베이스 설계 경험
* Prisma ORM 활용 경험
* Node.js 기반 REST API 서버 구축 경험
* Android와 Backend를 함께 개발하는 Full Stack 프로젝트 경험

---

# 실행 화면

## 로그인

<img width="388" height="837" alt="image" src="https://github.com/user-attachments/assets/5a92de86-ca52-46e6-a565-f86ebaaca045" />


## 회원가입

<img width="388" height="829" alt="image" src="https://github.com/user-attachments/assets/0addd339-35bc-4b55-bed6-9d30d2338bd3" />


## 홈 화면

<img width="383" height="837" alt="image" src="https://github.com/user-attachments/assets/d193ec18-2bcc-433e-b714-14337c22f507" />


## 홈 화면 메뉴

<img width="380" height="834" alt="image" src="https://github.com/user-attachments/assets/da8932d6-0ee5-452b-b95a-8cb76896ce4b" />


## 상품 상세

<img width="383" height="839" alt="image" src="https://github.com/user-attachments/assets/8f18136a-74c8-4458-a5b5-1bbe34ea9f1d" />


## 검색
<img width="384" height="837" alt="image" src="https://github.com/user-attachments/assets/a5bb3f66-5832-466f-b53b-9dfcab8a4713" />


## 찜 목록
<img width="387" height="839" alt="image" src="https://github.com/user-attachments/assets/94059b5d-a444-4ada-a134-2ab582bbb3a8" />


## 상품 등록

<img width="377" height="829" alt="image" src="https://github.com/user-attachments/assets/ecf8daa8-7ed7-4eb5-8c7a-12c57af3c45a" />


## 마이페이지

<img width="380" height="832" alt="image" src="https://github.com/user-attachments/assets/6b0f7ac1-723d-4f83-a0a0-4e1c0bf3cbe9" />

