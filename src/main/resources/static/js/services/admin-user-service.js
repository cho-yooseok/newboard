// src/main/resources/static/js/services/admin-user-service.js

import { authenticatedFetch } from '../api-client.js';

export const AdminUserService = {
    async getAdminUsers(page, pageSize) {
        const url = `/api/admin/users?page=${page}&size=${pageSize}`;
        const response = await authenticatedFetch(url);
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '사용자 로드 실패');
        }
        return response.json();
    },

    async deleteUser(userId) {
        const response = await authenticatedFetch(`/api/admin/users/${userId}`, {
            method: 'DELETE',
        });
        if (response.status !== 204) {
            const errorData = await response.json();
            throw new Error(errorData.message || '사용자 삭제 실패');
        }
    },

    async updateUserRole(userId, role) {
        const response = await authenticatedFetch(`/api/admin/users/${userId}/role?role=${role}`, {
            method: 'PUT',
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '역할 변경 실패');
        }
    }
};