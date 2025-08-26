// src/main/resources/static/js/dom-elements-admin.js

export const adminElements = {
    adminErrorMessage: document.getElementById('admin-error-message'),
    adminContent: document.getElementById('admin-content'),

    tabButtons: document.querySelectorAll('.admin-tabs .tab-button'),
    tabContents: document.querySelectorAll('.tab-content'),

    // 게시글
    adminPostList: document.getElementById('admin-post-list'),
    adminPostPagination: document.getElementById('admin-post-pagination'),
    adminPostSearchInput: document.getElementById('admin-post-search-input'),
    adminPostSearchButton: document.getElementById('admin-post-search-button'),

    // 사용자
    adminUserList: document.getElementById('admin-user-list'),
    adminUserPagination: document.getElementById('admin-user-pagination'),

    // 댓글 (추가)
    adminCommentList: document.getElementById('admin-comment-list'),
    adminCommentPagination: document.getElementById('admin-comment-pagination'),
    adminCommentSearchInput: document.getElementById('admin-comment-search-input'),
    adminCommentSearchButton: document.getElementById('admin-comment-search-button'),
};