// src/main/resources/static/js/services/admin-post-service.js

import { authenticatedFetch } from '../api-client.js';

export const AdminPostService = {
    /**
     * 관리자용 게시글 목록을 가져옵니다.
     * @param {number} page 페이지 번호 (0-based)
     * @param {number} pageSize 페이지 크기
     * @param {string} searchKeyword 검색 키워드
     * @returns {Promise<Object>} 페이지네이션된 게시글 데이터
     * @throws {Error} API 호출 실패 시 에러
     */
    async getAdminPosts(page, pageSize, searchKeyword) {
        const url = `/api/admin/posts?page=${page}&size=${pageSize}${searchKeyword ? `&search=${encodeURIComponent(searchKeyword)}` : ''}`;
        const response = await authenticatedFetch(url);
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '게시글 로드 실패');
        }
        return response.json();
    },

    /**
     * 게시글을 소프트 삭제합니다.
     * @param {string} postId 게시글 ID
     * @returns {Promise<void>}
     * @throws {Error} API 호출 실패 시 에러
     */
    async softDeletePost(postId) {
        const response = await authenticatedFetch(`/api/admin/posts/${postId}/soft-delete`, {
            method: 'POST',
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '게시글 소프트 삭제 실패');
        }
    },

    /**
     * 게시글을 복원합니다.
     * @param {string} postId 게시글 ID
     * @returns {Promise<void>}
     * @throws {Error} API 호출 실패 시 에러
     */
    async restorePost(postId) {
        const response = await authenticatedFetch(`/api/admin/posts/${postId}/restore`, {
            method: 'POST',
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '게시글 복원 실패');
        }
    },

    /**
     * 게시글을 영구 삭제합니다.
     * @param {string} postId 게시글 ID
     * @returns {Promise<void>}
     * @throws {Error} API 호출 실패 시 에러
     */
    async hardDeletePost(postId) {
        const response = await authenticatedFetch(`/api/admin/posts/${postId}/hard-delete`, {
            method: 'DELETE',
        });
        if (response.status !== 204) { // 204 No Content는 성공으로 간주
            const errorData = await response.json();
            throw new Error(errorData.message || '게시글 영구 삭제 실패');
        }
    }
};