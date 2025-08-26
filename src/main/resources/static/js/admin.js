// src/main/resources/static/js/admin.js

import { adminElements } from './dom-elements-admin.js';
import { formatDateTime, renderPagination } from './utils.js';
import { AdminPostService } from './services/admin-post-service.js';
import { AdminUserService } from './services/admin-user-service.js';
import { CommentService } from './services/comment-service.js'; // CommentService import
import { AuthService } from './services/auth-service.js';

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('accessToken');
    const loggedInUsername = localStorage.getItem('username');

    let currentPostPage = 0;
    let currentPostSearchKeyword = '';
    let currentUserPage = 0;
    let currentCommentPage = 0; // 댓글 페이지 상태
    let currentCommentSearchKeyword = ''; // 댓글 검색 키워드 상태
    const pageSize = 10;

    function addEventListeners() {
        adminElements.tabButtons.forEach(button => {
            button.addEventListener('click', handleTabClick);
        });

        // 게시글 검색
        adminElements.adminPostSearchButton.addEventListener('click', handlePostSearch);
        adminElements.adminPostSearchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') handlePostSearch();
        });

        // 댓글 검색
        adminElements.adminCommentSearchButton.addEventListener('click', handleCommentSearch);
        adminElements.adminCommentSearchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') handleCommentSearch();
        });

        // 이벤트 위임
        adminElements.adminPostList.addEventListener('click', handlePostActions);
        adminElements.adminUserList.addEventListener('click', handleUserActions);
        adminElements.adminCommentList.addEventListener('click', handleCommentActions); // 댓글 액션
    }

    function handleTabClick(e) {
        const targetTab = e.target.dataset.tab;

        adminElements.tabButtons.forEach(btn => btn.classList.remove('active'));
        adminElements.tabContents.forEach(content => content.classList.remove('active'));

        e.target.classList.add('active');
        document.getElementById(`${targetTab}-tab`).classList.add('active');

        if (targetTab === 'users') {
            loadAdminUsers(currentUserPage);
        } else if (targetTab === 'posts') {
            loadAdminPosts(currentPostPage, currentPostSearchKeyword);
        } else if (targetTab === 'comments') { // 댓글 탭 로드
            loadAdminComments(currentCommentPage, currentCommentSearchKeyword);
        }
    }

    // --- 검색 핸들러 ---
    function handlePostSearch() {
        const searchKeyword = adminElements.adminPostSearchInput.value.trim();
        loadAdminPosts(0, searchKeyword);
    }

    function handleCommentSearch() {
        const searchKeyword = adminElements.adminCommentSearchInput.value.trim();
        loadAdminComments(0, searchKeyword);
    }

    // --- 액션 핸들러 (이벤트 위임) ---
    function handlePostActions(e) {
        const target = e.target;
        const postId = target.dataset.id;
        if (!postId) return;

        if (target.matches('.btn-soft-delete')) softDeletePost(postId);
        else if (target.matches('.btn-restore')) restorePost(postId);
        else if (target.matches('.btn-hard-delete')) hardDeletePost(postId);
    }

    function handleUserActions(e) {
        const target = e.target;
        const userId = target.dataset.id;
        const username = target.dataset.username;
        if (!userId) return;

        if (target.matches('.btn-delete-user')) deleteUser(userId, username);
        else if (target.matches('.btn-grant-admin')) updateUserRole(userId, username, 'ADMIN');
        else if (target.matches('.btn-revoke-admin')) updateUserRole(userId, username, 'USER');
    }

    function handleCommentActions(e) {
        const target = e.target;
        const commentId = target.dataset.id;
        if (!commentId) return;

        if (target.matches('.btn-delete-comment')) {
            deleteComment(commentId);
        }
    }

    // --- 페이지 초기화 ---
    async function initAdminPage() {
        if (!token) {
            showAdminError('로그인 후 관리자 권한으로 접근해야 합니다.');
            return;
        }
        try {
            const userRole = await AuthService.getCurrentUserRole();
            if (userRole !== 'ADMIN') {
                showAdminError('관리자 권한이 없습니다.');
                return;
            }
            adminElements.adminContent.style.display = 'block';
            adminElements.adminErrorMessage.style.display = 'none';
            addEventListeners();
            await loadAdminPosts(0, '');
        } catch (error) {
            showAdminError(`관리자 페이지 로드 실패: ${error.message}`);
        }
    }

    function showAdminError(message) {
        adminElements.adminErrorMessage.textContent = message;
        adminElements.adminErrorMessage.style.display = 'block';
        adminElements.adminContent.style.display = 'none';
    }

    // --- 데이터 로딩 및 렌더링 함수들 ---
    async function loadAdminPosts(page = 0, searchKeyword = '') {
        currentPostPage = page;
        currentPostSearchKeyword = searchKeyword;
        try {
            const pageData = await AdminPostService.getAdminPosts(page, pageSize, searchKeyword);
            renderTable(adminElements.adminPostList, pageData.content, createPostRow, '게시글이 없습니다.', 7);
            renderPagination(adminElements.adminPostPagination, pageData.totalPages, page, loadAdminPosts, searchKeyword);
        } catch (error) {
            adminElements.adminPostList.innerHTML = `<tr><td colspan="7">게시글 로드 실패: ${error.message}</td></tr>`;
        }
    }

    async function loadAdminUsers(page = 0) {
        currentUserPage = page;
        try {
            const pageData = await AdminUserService.getAdminUsers(page, pageSize);
            renderTable(adminElements.adminUserList, pageData.content, createUserRow, '사용자가 없습니다.', 4);
            renderPagination(adminElements.adminUserPagination, pageData.totalPages, page, loadAdminUsers);
        } catch (error) {
            adminElements.adminUserList.innerHTML = `<tr><td colspan="4">사용자 로드 실패: ${error.message}</td></tr>`;
        }
    }

    async function loadAdminComments(page = 0, searchKeyword = '') {
        currentCommentPage = page;
        currentCommentSearchKeyword = searchKeyword;
        try {
            const pageData = await CommentService.getCommentsAsAdmin(page, pageSize, searchKeyword);
            renderTable(adminElements.adminCommentList, pageData.content, createCommentRow, '댓글이 없습니다.', 6);
            renderPagination(adminElements.adminCommentPagination, pageData.totalPages, page, loadAdminComments, searchKeyword);
        } catch (error) {
            adminElements.adminCommentList.innerHTML = `<tr><td colspan="6">댓글 로드 실패: ${error.message}</td></tr>`;
        }
    }

    // --- 테이블 렌더링 헬퍼 ---
    function renderTable(tbody, data, rowCreator, noDataMessage, colspan) {
        tbody.innerHTML = '';
        if (data.length === 0) {
            tbody.innerHTML = `<tr><td colspan="${colspan}">${noDataMessage}</td></tr>`;
        } else {
            tbody.innerHTML = data.map(rowCreator).join('');
        }
    }

    // --- 테이블 행 생성 함수들 ---
    function createPostRow(post) {
        const formattedDate = formatDateTime(new Date(post.createdAt)).split(' ')[0];
        const status = post.deleted ? '<span class="status-deleted">삭제됨</span>' : '<span class="status-active">활성</span>';
        const actions = post.deleted
            ? `<button class="btn-restore" data-id="${post.id}">복원</button>`
            : `<button class="btn-soft-delete" data-id="${post.id}">임시 삭제</button>
               <button class="btn-hard-delete" data-id="${post.id}">영구 삭제</button>`;
        return `
            <tr>
                <td>${post.id}</td>
                <td class="col-title"><a href="/post-detail.html?id=${post.id}" target="_blank">${post.title}</a></td>
                <td>${post.authorUsername}</td>
                <td>${formattedDate}</td>
                <td>${post.viewCount}</td>
                <td>${status}</td>
                <td class="col-actions">${actions}</td>
            </tr>`;
    }

    function createUserRow(user) {
        const isSelf = user.username === loggedInUsername;
        const actions = isSelf
            ? `(자신)`
            : `<button class="btn-delete-user" data-id="${user.id}" data-username="${user.username}">삭제</button>
               ${user.role === 'USER'
                ? `<button class="btn-grant-admin" data-id="${user.id}" data-username="${user.username}">관리자 부여</button>`
                : `<button class="btn-revoke-admin" data-id="${user.id}" data-username="${user.username}">관리자 회수</button>`
            }`;
        return `
            <tr>
                <td>${user.id}</td>
                <td>${user.username}</td>
                <td>${user.role}</td>
                <td class="col-actions">${actions}</td>
            </tr>`;
    }

    function createCommentRow(comment) {
        const formattedDate = formatDateTime(new Date(comment.createdAt)).split(' ')[0];
        const shortContent = comment.content.length > 50 ? comment.content.substring(0, 50) + '...' : comment.content;
        return `
            <tr>
                <td>${comment.id}</td>
                <td class="col-content">${shortContent}</td>
                <td>${comment.authorUsername}</td>
                <td><a href="/post-detail.html?id=${comment.postId}" target="_blank">게시글 ${comment.postId}</a></td>
                <td>${formattedDate}</td>
                <td class="col-actions">
                    <button class="btn-delete-comment" data-id="${comment.id}">삭제</button>
                </td>
            </tr>`;
    }

    // --- 액션 실행 함수들 ---
    async function softDeletePost(postId) {
        if (!confirm(`게시글 ID ${postId}을(를) 임시 삭제하시겠습니까?`)) return;
        try {
            await AdminPostService.softDeletePost(postId);
            loadAdminPosts(currentPostPage, currentPostSearchKeyword);
        } catch (error) { alert(`실패: ${error.message}`); }
    }

    async function restorePost(postId) {
        if (!confirm(`게시글 ID ${postId}을(를) 복원하시겠습니까?`)) return;
        try {
            await AdminPostService.restorePost(postId);
            loadAdminPosts(currentPostPage, currentPostSearchKeyword);
        } catch (error) { alert(`실패: ${error.message}`); }
    }

    async function hardDeletePost(postId) {
        if (!confirm(`경고: 게시글 ID ${postId}을(를) 영구 삭제합니다. 되돌릴 수 없습니다!`)) return;
        try {
            await AdminPostService.hardDeletePost(postId);
            loadAdminPosts(currentPostPage, currentPostSearchKeyword);
        } catch (error) { alert(`실패: ${error.message}`); }
    }

    async function deleteUser(userId, username) {
        if (!confirm(`사용자 '${username}' (ID: ${userId})을(를) 영구 삭제하시겠습니까?`)) return;
        try {
            await AdminUserService.deleteUser(userId);
            loadAdminUsers(currentUserPage);
        } catch (error) { alert(`실패: ${error.message}`); }
    }

    async function updateUserRole(userId, username, role) {
        if (!confirm(`사용자 '${username}'의 역할을 '${role}'(으)로 변경하시겠습니까?`)) return;
        try {
            await AdminUserService.updateUserRole(userId, role);
            loadAdminUsers(currentUserPage);
        } catch (error) { alert(`실패: ${error.message}`); }
    }

    async function deleteComment(commentId) {
        if (!confirm(`댓글 ID ${commentId}을(를) 영구 삭제하시겠습니까?`)) return;
        try {
            await CommentService.deleteCommentAsAdmin(commentId);
            loadAdminComments(currentCommentPage, currentCommentSearchKeyword);
        } catch (error) { alert(`실패: ${error.message}`); }
    }

    initAdminPage();
});