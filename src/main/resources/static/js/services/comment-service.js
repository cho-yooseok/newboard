// src/main/resources/static/js/services/comment-service.js

import { authenticatedFetch } from '../api-client.js';

export const CommentService = {
    // 사용자가 게시글 상세 페이지에서 댓글 목록을 볼 때 사용
    async getComments(postId) {
        // 이 API는 토큰이 없어도 호출될 수 있으므로, 일반 fetch나, 에러 처리가 조정된 authenticatedFetch를 사용
        const response = await authenticatedFetch(`/api/posts/${postId}/comments`);
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '댓글 로드 실패');
        }
        return response.json();
    },

    // 관리자가 관리자 페이지에서 댓글 목록을 볼 때 사용 (추가)
    async getCommentsAsAdmin(page, pageSize, searchKeyword) {
        const url = `/api/admin/comments?page=${page}&size=${pageSize}${searchKeyword ? `&search=${encodeURIComponent(searchKeyword)}` : ''}`;
        const response = await authenticatedFetch(url);
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '관리자 댓글 로드 실패');
        }
        return response.json();
    },

    async createComment(postId, content) {
        const response = await authenticatedFetch(`/api/posts/${postId}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content })
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '댓글 작성 실패');
        }
        return response.json();
    },

    async toggleCommentLike(postId, commentId) {
        const response = await authenticatedFetch(`/api/posts/${postId}/comments/${commentId}/like`, {
            method: 'POST',
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '댓글 좋아요 실패');
        }
        return response.json();
    },

    async updateComment(postId, commentId, newContent) {
        const response = await authenticatedFetch(`/api/posts/${postId}/comments/${commentId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: newContent })
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '댓글 수정 실패');
        }
    },

    async deleteComment(postId, commentId) {
        const response = await authenticatedFetch(`/api/posts/${postId}/comments/${commentId}`, {
            method: 'DELETE'
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '댓글 삭제 실패');
        }
    },

    async deleteCommentAsAdmin(commentId) {
        const response = await authenticatedFetch(`/api/admin/comments/${commentId}`, {
            method: 'DELETE'
        });
        if (response.status !== 204) {
            const errorData = await response.json();
            throw new Error(errorData.message || '관리자 댓글 삭제 실패');
        }
    }
};