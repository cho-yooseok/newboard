// src/main/resources/static/js/main.js

document.addEventListener('DOMContentLoaded', () => {
    updateAuthUI(); // 페이지 로드 시 인증 및 관리자 링크 로드
});

// 인증 상태와 사용자 역할에 따라 헤더의 링크를 업데이트하는 함수
async function updateAuthUI() { // 비동기 함수로 유지 (혹시 모를 추가 비동기 로직을 위해)
    const authLinksContainer = document.getElementById('auth-links');
    const writePostLink = document.getElementById('write-post-link');
    const adminPageLink = document.getElementById('admin-page-link'); // admin-page-link ID 사용

    const accessToken = localStorage.getItem('accessToken');
    const username = localStorage.getItem('username');
    const userRole = localStorage.getItem('userRole'); // <-- userRole 가져오기

    if (accessToken && username) {
        // 로그인 상태
        authLinksContainer.innerHTML = `
            <span>환영합니다, <strong>${username}</strong>님!</span>
            <a href="#" id="logout-button">로그아웃</a>
        `;
        // 글쓰기 버튼 표시
        if (writePostLink) {
            writePostLink.style.display = 'block';
            const writePostButton = document.getElementById('write-post-button');
            if(writePostButton) {
                writePostButton.style.display = 'inline-block';
            }
        }

        // 로그아웃 버튼 이벤트 리스너 추가 (동적으로 생성되므로 여기서 추가)
        const logoutButton = document.getElementById('logout-button');
        if (logoutButton) {
            logoutButton.addEventListener('click', (e) => {
                e.preventDefault();
                localStorage.removeItem('accessToken');
                localStorage.removeItem('username');
                localStorage.removeItem('userRole'); // <-- userRole도 삭제
                alert('로그아웃 되었습니다.');
                window.location.href = '/'; // 메인 페이지로 리다이렉트
            });
        }

        // 관리자 권한 확인 및 관리자 링크 표시
        if (adminPageLink) { // adminPageLink 요소가 존재하는지 확인
            if (userRole === 'ADMIN') { // localStorage에서 가져온 역할 정보 확인
                adminPageLink.style.display = 'block';
            } else {
                adminPageLink.style.display = 'none';
            }
        }

    } else {
        // 로그아웃 상태 또는 토큰 없음
        authLinksContainer.innerHTML = `
            <li><a href="/register.html">회원가입</a></li>
            <li><a href="/login.html">로그인</a></li>
        `;
        // 글쓰기 버튼 숨김
        if (writePostLink) {
            writePostLink.style.display = 'none';
            const writePostButton = document.getElementById('write-post-button');
            if(writePostButton) {
                writePostButton.style.display = 'none';
            }
        }
        // 관리자 링크 숨김
        if (adminPageLink) { // adminPageLink 요소가 존재하는지 확인
            adminPageLink.style.display = 'none';
        }
    }
}
