// src/main/resources/static/js/services/post-service.js

import { authenticatedFetch } from '../api-client.js';

export const PostService = {
    /**
     * 특정 게시글의 상세 정보를 가져옵니다.
     * @param {string} postId 게시글 ID
     * @returns {Promise<Object>} 게시글 데이터
     * @throws {Error} API 호출 실패 시 에러
     */
    async getPostDetail(postId) {
        const response = await authenticatedFetch(`/api/posts/${postId}`, {
            method: 'GET',
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || response.statusText);
        }
        return response.json();
    },

    /**
     * 게시글을 삭제합니다.
     * @param {string} postId 게시글 ID
     * @returns {Promise<void>}
     * @throws {Error} API 호출 실패 시 에러
     */
    async deletePost(postId) {
        const response = await authenticatedFetch(`/api/posts/${postId}`, {
            method: 'DELETE',
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || response.statusText);
        }
    },

    /**
     * 게시글 좋아요를 토글합니다.
     * @param {string} postId 게시글 ID
     * @returns {Promise<Object>} 업데이트된 게시글 데이터 (좋아요 수, 현재 사용자의 좋아요 상태 포함)
     * @throws {Error} API 호출 실패 시 에러
     */
    async togglePostLike(postId) {
        const response = await authenticatedFetch(`/api/posts/${postId}/like`, {
            method: 'POST',
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '게시글 좋아요 처리 실패');
        }
        return response.json();
    }
};