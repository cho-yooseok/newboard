# 자유 게시판 프로젝트 (Freeboard)

##  프로젝트 소개

이 프로젝트는 Spring Boot와 JPA를 사용하여 개발한 자유 게시판 백엔드 API 서버입니다.

백엔드 개발자로서의 역량을 보여주기 위해, 기본적인 CRUD 기능부터 시작하여 JWT를 이용한 인증/인가, 관리자 기능, 데이터 무결성을 고려한 로직 설계 등 실무에서 마주할 수 있는 다양한 기술과 고민을 담아 구현하였습니다.

사용자는 게시글과 댓글을 작성하고, '좋아요'를 통해 상호작용할 수 있으며, 관리자는 모든 사용자, 게시글, 댓글을 관리하는 기능을 제공합니다.

##  주요 기능

### 사용자 기능
- **회원가입 및 로그인**: JWT 토큰 기반의 안전한 인증 시스템
- **게시글 관리**:
    - 게시글 작성, 조회, 수정, 삭제 (CRUD)
    - 페이징 및 검색 기능 (제목, 내용 기반)
    - 조회수 및 좋아요 기능
- **댓글 관리**:
    - 댓글 작성, 조회, 수정, 삭제 (CRUD)
    - 댓글 좋아요 기능

### 관리자 기능
- **사용자 관리**:
    - 전체 사용자 목록 조회 (페이징)
    - 사용자 역할 변경 (USER ↔ ADMIN)
    - 사용자 계정 삭제
- **게시글 관리**:
    - 전체 게시글 목록 조회 (삭제된 글 포함)
    - 게시글 임시 삭제(Soft Delete) 및 복원
    - 게시글 영구 삭제(Hard Delete)
- **댓글 관리**:
    - 전체 댓글 목록 조회 및 검색
    - 댓글 영구 삭제

##  기술 스택

- **Backend**: `Java 17`, `Spring Boot 3.1`
- **Database**: `MySQL`
- **Persistence**: `Spring Data JPA`
- **Security**: `Spring Security`, `JWT (Java Web Token)`
- **Build Tool**: `Gradle`
- **Libraries**: `Lombok`, `JJWT`

##  API 명세

주요 엔드포인트는 다음과 같습니다.

| Method | URL                                       | 설명                          | 권한          |
|--------|-------------------------------------------|-------------------------------|---------------|
| POST   | `/api/auth/register`                      | 회원가입                      | `PermitAll`   |
| POST   | `/api/auth/login`                         | 로그인 (JWT 토큰 발급)        | `PermitAll`   |
| GET    | `/api/auth/me`                            | 내 정보 조회                  | `Authenticated` |
| GET    | `/api/posts`                              | 게시글 목록 조회              | `PermitAll`   |
| GET    | `/api/posts/{id}`                         | 게시글 상세 조회              | `PermitAll`   |
| POST   | `/api/posts`                              | 게시글 작성                   | `Authenticated` |
| PUT    | `/api/posts/{id}`                         | 게시글 수정                   | `Owner`       |
| DELETE | `/api/posts/{id}`                         | 게시글 삭제 (Soft Delete)     | `Owner`       |
| POST   | `/api/posts/{id}/like`                    | 게시글 좋아요 토글            | `Authenticated` |
| GET    | `/api/posts/{postId}/comments`            | 댓글 목록 조회                | `PermitAll`   |
| POST   | `/api/posts/{postId}/comments`            | 댓글 작성                     | `Authenticated` |
| DELETE | `/api/posts/{postId}/comments/{commentId}`| 댓글 삭제                     | `Owner`       |
| GET    | `/api/admin/users`                        | (관리자) 모든 사용자 조회     | `ADMIN`       |
| DELETE | `/api/admin/users/{userId}`               | (관리자) 사용자 삭제          | `ADMIN`       |
| PUT    | `/api/admin/users/{userId}/role`          | (관리자) 사용자 역할 변경     | `ADMIN`       |
| DELETE | `/api/admin/posts/{postId}/hard-delete`   | (관리자) 게시글 영구 삭제     | `ADMIN`       |
| POST   | `/api/admin/posts/{postId}/restore`       | (관리자) 게시글 복원          | `ADMIN`       |

---

##  트러블 슈팅 (Troubleshooting)

프로젝트를 진행하며 발생했던 문제들과 해결 과정을 정리했습니다.

### 1. 사용자 탈퇴 시 연관 데이터 처리 문제

- #### 겪었던 문제
  단순히 `userRepository.delete(user)`를 호출하여 사용자를 삭제하려고 할 때, 해당 사용자가 작성한 게시글(`Post`), 댓글(`Comment`), 좋아요(`PostLike`, `CommentLike`) 데이터가 외래 키(Foreign Key) 제약 조건에 위배되어 `DataIntegrityViolationException`이 발생했습니다. 사용자를 탈퇴시키기 위해서는 이와 관련된 모든 자식 데이터를 먼저 처리해야 했습니다.

- #### 해결 과정
  **1차 시도: `@OnDelete(action = OnDeleteAction.CASCADE)`**
  JPA의 어노테이션을 사용하여 데이터베이스 레벨에서 연쇄 삭제를 시도했습니다. `Post` 엔티티의 `author` 필드에 이 어노테이션을 적용하면, User가 삭제될 때 해당 User가 작성한 모든 Post가 자동으로 삭제됩니다. 이 방식은 간단하지만, 애플리케이션 로직 외부(DB)에서 데이터 삭제가 일어나므로 동작을 예측하고 제어하기 어렵다는 단점이 있었습니다. 예를 들어, 삭제 전 특정 로직을 수행해야 할 경우 적용하기 어렵습니다.

  **2차 시도: 서비스 계층(Service Layer)에서 명시적 삭제 로직 구현 (채택)**
  데이터의 흐름을 명확하게 제어하고 비즈니스 로직의 확장성을 위해, `UserService`의 `deleteUser` 메서드 내에서 연관된 데이터를 직접 삭제하는 방식을 선택했습니다.

  **삭제 순서:**
    1.  사용자가 누른 모든 `PostLike`, `CommentLike`를 먼저 삭제합니다. (가장 말단에 있는 자식 데이터)
    2.  사용자가 작성한 모든 `Comment`를 삭제합니다.
    3.  사용자가 작성한 모든 `Post`를 삭제합니다. (Post 삭제 시 Post에 달린 댓글과 좋아요도 Cascade 설정에 의해 함께 삭제됩니다.)
    4.  마지막으로 `User`를 삭제합니다.

  이러한 순서로 의존성을 해결하며 데이터를 삭제함으로써, 무결성을 유지하고 서비스 로직의 응집도를 높일 수 있었습니다.

  ```java
  // UserService.java
  @Transactional
  public void deleteUser(Long userId) {
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

      // 1. 사용자가 누른 '좋아요' 기록 삭제
      postLikeRepository.deleteByUser(user);
      commentLikeRepository.deleteByUser(user);

      // 2. 사용자가 작성한 댓글 삭제
      commentRepository.deleteByAuthor(user);

      // 3. 사용자가 작성한 게시글 삭제
      postRepository.deleteByAuthor(user);

      // 4. 마지막으로 사용자 삭제
      userRepository.delete(user);
  }
##
##
##  실제 실행 스크린 샷
![1첫화면.png](src/main/resources/image/1%EC%B2%AB%ED%99%94%EB%A9%B4.png)

![2회원가입.png](src/main/resources/image/2%ED%9A%8C%EC%9B%90%EA%B0%80%EC%9E%85.png)

![3회원가입 후.png](src/main/resources/image/3%ED%9A%8C%EC%9B%90%EA%B0%80%EC%9E%85%20%ED%9B%84.png)

![4로그인 성공.png](src/main/resources/image/4%EB%A1%9C%EA%B7%B8%EC%9D%B8%20%EC%84%B1%EA%B3%B5.png)

![5로그인 후 메인.png](src/main/resources/image/5%EB%A1%9C%EA%B7%B8%EC%9D%B8%20%ED%9B%84%20%EB%A9%94%EC%9D%B8.png)

![6새글작성.png](src/main/resources/image/6%EC%83%88%EA%B8%80%EC%9E%91%EC%84%B1.png)

![7댓글이있는 게시글.png](src/main/resources/image/7%EB%8C%93%EA%B8%80%EC%9D%B4%EC%9E%88%EB%8A%94%20%EA%B2%8C%EC%8B%9C%EA%B8%80.png)

![8댓글작성.png](src/main/resources/image/8%EB%8C%93%EA%B8%80%EC%9E%91%EC%84%B1.png)

![9댓글수정.png](src/main/resources/image/9%EB%8C%93%EA%B8%80%EC%88%98%EC%A0%95.png)

![10 관리자 메인.png](src/main/resources/image/10%20%EA%B4%80%EB%A6%AC%EC%9E%90%20%EB%A9%94%EC%9D%B8.png)

![11 관리자 페이지.png](src/main/resources/image/11%20%EA%B4%80%EB%A6%AC%EC%9E%90%20%ED%8E%98%EC%9D%B4%EC%A7%80.png)

![12 관리자 게시글 관리.png](src/main/resources/image/12%20%EA%B4%80%EB%A6%AC%EC%9E%90%20%EA%B2%8C%EC%8B%9C%EA%B8%80%20%EA%B4%80%EB%A6%AC.png)

![13 관리자 댓글.png](src/main/resources/image/13%20%EA%B4%80%EB%A6%AC%EC%9E%90%20%EB%8C%93%EA%B8%80.png)




