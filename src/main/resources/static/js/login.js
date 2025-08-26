// C:\Users\admin\Desktop\freeboard\freeboard\src\main\resources\static\js\login.js

import { authenticatedFetch } from './api-client.js';

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');

    const usernameError = document.getElementById('username-error');
    const passwordError = document.getElementById('password-error');
    const generalError = document.getElementById('general-error');
    const successMessage = document.getElementById('success-message');

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        usernameError.textContent = '';
        passwordError.textContent = '';
        generalError.textContent = '';
        successMessage.textContent = '';

        const username = usernameInput.value;
        const password = passwordInput.value;

        let isValid = true;
        if (!username) {
            usernameError.textContent = '사용자 이름을 입력해주세요.';
            isValid = false;
        }
        if (!password) {
            passwordError.textContent = '비밀번호를 입력해주세요.';
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });

            if (response.ok) {
                const data = await response.json();
                const accessToken = data.accessToken;
                const loggedInUsername = data.username;
                const userRole = data.role; // <-- 역할(role) 정보 가져오기

                localStorage.setItem('accessToken', accessToken);
                localStorage.setItem('username', loggedInUsername);
                localStorage.setItem('userRole', userRole); // <-- 역할(role) 정보 저장

                successMessage.textContent = '로그인이 성공적으로 완료되었습니다!';
                loginForm.reset();

                // 관리자 역할에 따라 바로 리다이렉트
                if (userRole === 'ADMIN') { // 백엔드에서 'ADMIN'으로 보낸다고 가정
                    alert('관리자 계정으로 로그인했습니다. 관리자 페이지로 이동합니다.');
                    window.location.href = '/admin.html';
                } else {
                    alert('로그인 성공! 메인 페이지로 이동합니다.');
                    window.location.href = '/';
                }

            } else {
                const errorData = await response.json();
                if (response.status === 401) {
                    generalError.textContent = errorData.message || '사용자 이름 또는 비밀번호가 올바르지 않습니다.';
                } else if (response.status === 400 && errorData.errors) {
                    for (const field in errorData.errors) {
                        if (field === 'username') {
                            usernameError.textContent = errorData.errors[field];
                        } else if (field === 'password') {
                            passwordError.textContent = errorData.errors[field];
                        }
                    }
                } else {
                    generalError.textContent = errorData.message || '로그인 중 오류가 발생했습니다.';
                }
            }
        } catch (error) {
            console.error('Fetch error:', error);
            generalError.textContent = '네트워크 오류가 발생했습니다. 다시 시도해주세요.';
        }
    });
});