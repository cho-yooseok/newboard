// src/main/resources/static/js/services/auth-service.js

import { authenticatedFetch } from '../api-client.js';

// 이 모듈은 현재 로그인된 사용자의 정보를 가져오고 관리하는 역할을 합니다.
export const AuthService = {
    /**
     * 현재 로그인된 사용자의 상세 정보를 백엔드에서 가져옵니다.
     * @returns {Promise<Object|null>} 사용자 정보 객체 (username, role 등) 또는 null
     */
    async getCurrentUserInfo() {
        const token = localStorage.getItem('accessToken');
        if (!token) {
            return null;
        }

        try {
            // SecurityConfig에서 /api/auth/me는 인증된 사용자만 접근 가능하도록 설정되어 있어야 합니다.
            const response = await authenticatedFetch('/api/auth/me');
            if (response.ok) {
                return await response.json();
            }
            return null;
        } catch (error) {
            console.error('사용자 정보를 가져오는 데 실패했습니다.', error);
            // 토큰이 유효하지 않은 경우 등의 이유로 실패할 수 있습니다.
            return null;
        }
    },

    /**
     * 현재 로그인된 사용자의 역할을 반환합니다.
     * @returns {Promise<string|null>} 사용자 역할 (예: "ADMIN", "USER") 또는 null
     */
    async getCurrentUserRole() {
        const userInfo = await this.getCurrentUserInfo();
        return userInfo ? userInfo.role : null;
    }
};