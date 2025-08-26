// src/main/resources/static/js/post-detail.js

import { elements } from './dom-elements.js';
import { formatDateTime, updateLikeButtonState } from './utils.js';
import { PostService } from './services/post-service.js';
import { CommentService } from './services/comment-service.js';
import { AuthService } from './services/auth-service.js';

document.addEventListener('DOMContentLoaded', async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const postId = urlParams.get('id');
    const loggedInUsername = localStorage.getItem('username');
    const token = localStorage.getItem('accessToken');

    let currentUserRole = null;

    if (!postId) {
        elements.postErrorMessage.textContent = '잘못된 게시글 ID입니다.';
        elements.postErrorMessage.style.display = 'block';
        return;
    }

    async function initializePage() {
        if (token) {
            currentUserRole = await AuthService.getCurrentUserRole();
        }
        await loadPostDetail();
        addEventListeners();
    }

    function addEventListeners() {
        if (token) {
            elements.likeButton.addEventListener('click', handlePostLike);
        } else {
            elements.likeButton.disabled = true;
            elements.likeButton.title = '로그인해야 좋아요를 누를 수 있습니다.';
        }
        elements.submitCommentButton.addEventListener('click', handleCommentSubmit);
        elements.commentList.addEventListener('click', handleCommentActions);
    }

    async function loadPostDetail() {
        try {
            const post = await PostService.getPostDetail(postId);

            elements.postTitle.textContent = post.title;
            elements.detailPostTitle.textContent = `${post.title} - 자유 게시판`;
            elements.postAuthor.textContent = post.authorUsername;
            elements.postDate.textContent = formatDateTime(new Date(post.createdAt));
            elements.postViews.textContent = post.viewCount;
            elements.postContent.textContent = post.content;

            // KEY POINT: 페이지 로드 시, API 응답 값으로 게시글 '좋아요' 버튼 상태를 초기화합니다.
            elements.postLikeCount.textContent = post.likeCount;
            updateLikeButtonState(elements.likeButton, post.likedByCurrentUser);

            if (loggedInUsername && loggedInUsername === post.authorUsername) {
                elements.editButton.style.display = 'inline-block';
                elements.deleteButton.style.display = 'inline-block';
                elements.editButton.onclick = () => window.location.href = `/edit-post.html?id=${post.id}`;
                elements.deleteButton.onclick = () => deletePost(post.id);
            }

            if (token) {
                elements.commentFormContainer.style.display = 'block';
            } else {
                elements.commentFormContainer.style.display = 'none';
                showCommentError('로그인해야 댓글을 작성할 수 있습니다.');
            }

            await loadComments();
        } catch (error) {
            elements.postErrorMessage.textContent = `게시글 로드 실패: ${error.message}`;
            elements.postErrorMessage.style.display = 'block';
        }
    }

    async function loadComments() {
        try {
            const comments = await CommentService.getComments(postId);
            if (comments.length === 0) {
                elements.commentList.innerHTML = '<p class="no-comments">아직 댓글이 없습니다.</p>';
            } else {
                elements.commentList.innerHTML = comments.map(createCommentHtml).join('');
            }
        } catch (error) {
            elements.commentList.innerHTML = `<p class="error-message">댓글 로드 실패: ${error.message}</p>`;
        }
    }

    function createCommentHtml(comment) {
        const isAuthor = loggedInUsername && loggedInUsername === comment.authorUsername;
        const isAdmin = currentUserRole === 'ADMIN';
        const editButton = isAuthor ? `<button class="btn-comment-edit">수정</button>` : '';
        const deleteButton = isAuthor ? `<button class="btn-comment-delete">삭제</button>` : '';
        const adminDeleteButton = isAdmin && !isAuthor ? `<button class="btn-admin-comment-delete">관리자 삭제</button>` : '';

        // KEY POINT: API 응답 값으로 각 댓글의 '좋아요' 버튼 상태(클래스, 텍스트)를 초기화합니다.
        const likeButtonText = comment.likedByCurrentUser ? '❤️ 좋아요 취소' : '🤍 좋아요';
        const likedClass = comment.likedByCurrentUser ? 'liked' : '';

        return `
            <div class="comment-item" data-comment-id="${comment.id}">
                <div class="comment-meta">
                    <div>
                        <span class="comment-author">${comment.authorUsername}</span>
                        <span class="comment-date">${formatDateTime(new Date(comment.createdAt))}</span>
                    </div>
                    <span class="comment-likes">
                        <button class="btn-comment-like ${likedClass}">${likeButtonText}</button>
                        <span class="comment-like-count">${comment.likeCount}</span>
                    </span>
                </div>
                <p class="comment-content">${comment.content}</p>
                <div class="comment-actions">
                    ${editButton}
                    ${deleteButton}
                    ${adminDeleteButton}
                </div>
            </div>`;
    }

    async function handlePostLike() {
        try {
            const updatedPost = await PostService.togglePostLike(postId);
            elements.postLikeCount.textContent = updatedPost.likeCount;
            updateLikeButtonState(elements.likeButton, updatedPost.likedByCurrentUser);
        } catch (error) {
            alert(`좋아요 처리 실패: ${error.message}`);
        }
    }

    async function handleCommentSubmit() {
        const content = elements.commentContentInput.value.trim();
        if (!content) return showCommentError('댓글 내용을 입력해주세요.');

        try {
            await CommentService.createComment(postId, content);
            elements.commentContentInput.value = '';
            hideCommentError();
            loadComments();
        } catch (error) {
            showCommentError(`댓글 작성 실패: ${error.message}`);
        }
    }

    function handleCommentActions(e) {
        const target = e.target;
        const commentItem = target.closest('.comment-item');
        if (!commentItem) return;
        const commentId = commentItem.dataset.commentId;

        if (target.matches('.btn-comment-like')) {
            if (token) toggleCommentLike(commentId, target);
            else alert('로그인해야 좋아요를 누를 수 있습니다.');
        } else if (target.matches('.btn-comment-edit')) {
            openEditCommentForm(commentId);
        } else if (target.matches('.btn-comment-delete')) {
            deleteComment(commentId);
        } else if (target.matches('.btn-admin-comment-delete')) {
            deleteCommentAsAdmin(commentId);
        }
    }

    async function toggleCommentLike(commentId, buttonElement) {
        try {
            const updatedComment = await CommentService.toggleCommentLike(postId, commentId);
            const commentItem = buttonElement.closest('.comment-item');
            if (commentItem) {
                commentItem.querySelector('.comment-like-count').textContent = updatedComment.likeCount;
                updateLikeButtonState(buttonElement, updatedComment.likedByCurrentUser);
            }
        } catch (error) {
            alert(`댓글 좋아요 처리 실패: ${error.message}`);
        }
    }

    function openEditCommentForm(commentId) {
        const commentItem = document.querySelector(`.comment-item[data-comment-id="${commentId}"]`);
        if (!commentItem || commentItem.querySelector('.comment-edit-form')) return;

        const contentElement = commentItem.querySelector('.comment-content');
        const actionsElement = commentItem.querySelector('.comment-actions');
        const currentContent = contentElement.textContent;

        contentElement.style.display = 'none';
        actionsElement.style.display = 'none';

        const editForm = document.createElement('div');
        editForm.className = 'comment-edit-form';
        editForm.innerHTML = `
            <textarea class="edit-comment-content" rows="3">${currentContent}</textarea>
            <div style="text-align: right; margin-top: 10px;">
                <button class="btn-comment-save">저장</button>
                <button class="btn-comment-cancel">취소</button>
            </div>
        `;

        commentItem.appendChild(editForm);

        editForm.querySelector('.btn-comment-save').onclick = () => {
            const newContent = editForm.querySelector('.edit-comment-content').value.trim();
            updateComment(commentId, newContent);
        };
        editForm.querySelector('.btn-comment-cancel').onclick = () => closeEditCommentForm(commentItem);
    }

    function closeEditCommentForm(commentItem) {
        const editForm = commentItem.querySelector('.comment-edit-form');
        if (editForm) editForm.remove();
        commentItem.querySelector('.comment-content').style.display = 'block';
        commentItem.querySelector('.comment-actions').style.display = 'block';
    }

    async function updateComment(commentId, newContent) {
        if (!newContent) return alert('수정할 내용을 입력해주세요.');
        try {
            await CommentService.updateComment(postId, commentId, newContent);
            alert('댓글이 성공적으로 수정되었습니다.');
            loadComments();
        } catch (error) {
            alert(`댓글 수정 실패: ${error.message}`);
        }
    }

    async function deletePost(postId) {
        if (confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
            try {
                await PostService.deletePost(postId);
                alert('게시글이 삭제되었습니다.');
                window.location.href = '/';
            } catch (error) { alert(`삭제 실패: ${error.message}`); }
        }
    }

    async function deleteComment(commentId) {
        if (confirm('정말로 이 댓글을 삭제하시겠습니까?')) {
            try {
                await CommentService.deleteComment(postId, commentId);
                loadComments();
            } catch (error) { alert(`삭제 실패: ${error.message}`); }
        }
    }

    async function deleteCommentAsAdmin(commentId) {
        if (confirm(`관리자 권한으로 댓글(ID: ${commentId})을 영구 삭제합니다.`)) {
            try {
                await CommentService.deleteCommentAsAdmin(commentId);
                loadComments();
            } catch (error) { alert(`삭제 실패: ${error.message}`); }
        }
    }

    function showCommentError(message) {
        elements.commentErrorMessage.textContent = message;
        elements.commentErrorMessage.style.display = 'block';
    }

    function hideCommentError() {
        elements.commentErrorMessage.style.display = 'none';
    }

    initializePage();
});