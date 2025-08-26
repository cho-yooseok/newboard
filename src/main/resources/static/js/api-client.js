// src/main/resources/static/js/api-client.js

/**
 * JWT 토큰을 포함하여 인증된 API 요청을 보냅니다.
 * @param {string} url 요청 URL
 * @param {RequestInit} options fetch 요청 옵션
 * @returns {Promise<Response>} fetch 응답 객체
 */
export async function authenticatedFetch(url, options = {}) {
    const token = localStorage.getItem('accessToken');
    const headers = options.headers || {};

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
        ...options,
        headers: headers,
    });

    // 401 Unauthorized 응답 시 로그인 페이지로 리다이렉트 (선택 사항)
    if (response.status === 401) {
        alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
        window.location.href = '/login.html';
        throw new Error('Unauthorized');
    }

    return response;
}