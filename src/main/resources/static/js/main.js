// src/main/resources/static/js/main.js

// API 서버의 기본 URL입니다. 프론트엔드와 동일한 호스트를 사용하므로 비워둡니다.
const API_BASE_URL = '';

// =================================================================
// 유틸리티 & 인증 관련 함수
// =================================================================

/**
 * 로컬 스토리지에서 JWT 토큰을 가져옵니다.
 * @returns {string|null} 저장된 JWT 토큰 또는 null
 */
function getToken() {
    return localStorage.getItem('jwt');
}

/**
 * API 요청 시 필요한 인증 헤더를 생성합니다.
 * 토큰이 있으면 Authorization 헤더를 포함하고, 없으면 빈 객체를 반환합니다.
 * @returns {object} HTTP 요청 헤더
 */
function getAuthHeaders() {
    const token = getToken();
    if (!token) return { 'Content-Type': 'application/json' }; // 토큰이 없어도 Content-Type은 필요할 수 있음
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };
}

/**
 * 로컬 스토리지에서 현재 로그인된 사용자 정보를 가져옵니다.
 * @returns {{username: string, role: string}|null} 사용자 정보 객체 또는 null
 */
function getLoggedInUser() {
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    if (!username || !role) return null;
    return { username, role };
}

/**
 * 사용자를 로그아웃 처리합니다.
 * 로컬 스토리지에서 사용자 정보를 삭제하고 메인 페이지로 이동합니다.
 */
function handleLogout() {
    localStorage.removeItem('jwt');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    alert('로그아웃 되었습니다.');
    window.location.href = '/index.html';
}

/**
 * 로그인 상태에 따라 페이지 상단의 UI(로그인/로그아웃 버튼 등)를 업데이트합니다.
 */
function updateAuthUI() {
    const authNav = document.getElementById('auth-nav');
    if (!authNav) return; // 해당 요소가 없는 페이지일 경우 중단
    const user = getLoggedInUser();

    if (user) {
        // 로그인된 경우
        let adminButton = '';
        if (user.role === 'ADMIN') {
            // 관리자일 경우 관리자 페이지 버튼 추가
            adminButton = `<a href="/admin.html" class="btn">관리자 페이지</a>`;
        }
        authNav.innerHTML = `
            <span>환영합니다, ${user.username}님!</span>
            ${adminButton}
            <button onclick="handleLogout()">로그아웃</button>
        `;
    } else {
        // 로그아웃된 경우
        authNav.innerHTML = `
            <a href="/login.html" class="btn">로그인</a>
            <a href="/register.html" class="btn">회원가입</a>
        `;
    }
}


// =================================================================
// 인증 (로그인 / 회원가입)
// =================================================================

/**
 * 로그인 폼 제출을 처리합니다.
 * @param {Event} event - 폼 제출 이벤트
 */
async function handleLogin(event) {
    event.preventDefault(); // 폼의 기본 제출 동작을 막음
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            throw new Error('로그인 실패. 아이디 또는 비밀번호를 확인하세요.');
        }

        const data = await response.json();
        // 받은 토큰과 사용자 정보를 로컬 스토리지에 저장
        localStorage.setItem('jwt', data.accessToken);
        localStorage.setItem('username', data.username);
        localStorage.setItem('role', data.role);

        alert('로그인 성공!');
        window.location.href = '/index.html'; // 메인 페이지로 이동
    } catch (error) {
        alert(error.message);
    }
}

/**
 * 회원가입 폼 제출을 처리합니다.
 * @param {Event} event - 폼 제출 이벤트
 */
async function handleRegister(event) {
    event.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const message = await response.text();
        if (!response.ok) {
            throw new Error(message || '회원가입에 실패했습니다.');
        }

        alert('회원가입 성공! 로그인 페이지로 이동합니다.');
        window.location.href = '/login.html'; // 로그인 페이지로 이동
    } catch (error) {
        alert(error.message);
    }
}


// =================================================================
// 게시글 (목록, 상세, 생성, 수정, 삭제, 좋아요)
// =================================================================

/**
 * 게시글 목록을 서버에서 가져와 화면에 표시합니다.
 * @param {number} page - 조회할 페이지 번호 (0부터 시작)
 * @param {string} search - 검색어
 */
async function fetchPosts(page = 0, search = '') {
    const tableBody = document.querySelector("#posts-table tbody");
    const pagination = document.getElementById("pagination");
    let url = `${API_BASE_URL}/api/posts?page=${page}`;
    if (search) {
        url += `&search=${encodeURIComponent(search)}`;
    }

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error('게시글을 불러올 수 없습니다.');

        const pageData = await response.json();
        tableBody.innerHTML = ''; // 기존 목록을 지움

        // 각 게시글을 테이블의 행으로 추가
        pageData.content.forEach(post => {
            const row = tableBody.insertRow();
            row.innerHTML = `
                <td><a href="/post.html?id=${post.id}">${post.title}</a></td>
                <td>${post.authorUsername}</td>
                <td>${new Date(post.createdAt).toLocaleDateString()}</td>
                <td>${post.viewCount}</td>
                <td>${post.likeCount}</td>
            `;
        });

        // 페이지네이션 버튼 렌더링
        renderPagination(pagination, pageData, fetchPosts, search);

    } catch (error) {
        tableBody.innerHTML = `<tr><td colspan="5">${error.message}</td></tr>`;
    }
}

/**
 * 검색창의 입력값으로 게시글을 검색합니다.
 */
function searchPosts() {
    const search = document.getElementById('search-input').value;
    // 검색어를 URL 파라미터로 하여 페이지를 새로고침
    window.location.href = `/index.html?search=${encodeURIComponent(search)}`;
}

/**
 * 특정 게시글의 상세 정보를 가져와 화면에 표시합니다.
 * @param {number} postId - 조회할 게시글의 ID
 */
async function fetchPostDetails(postId) {
    const container = document.getElementById('post-detail-container');
    try {
        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}`, {
            headers: getAuthHeaders() // 좋아요 상태 확인을 위해 인증 헤더 전송
        });
        if (!response.ok) throw new Error('게시글 정보를 찾을 수 없습니다.');

        const post = await response.json();
        const user = getLoggedInUser();

        let actionButtons = '';
        // 현재 로그인한 사용자가 게시글 작성자일 경우 수정/삭제 버튼을 보여줌
        if (user && user.username === post.authorUsername) {
            actionButtons = `
                <a href="/edit.html?id=${post.id}" class="btn">수정</a>
                <button onclick="handleDeletePost(${post.id})">삭제</button>
            `;
        }

        container.innerHTML = `
            <div class="post-header"><h1>${post.title}</h1></div>
            <div class="post-meta">
                <span>작성자: ${post.authorUsername}</span> | 
                <span>작성일: ${new Date(post.createdAt).toLocaleString()}</span> | 
                <span>조회수: ${post.viewCount}</span>
            </div>
            <div class="post-content">${post.content.replace(/\n/g, '<br>')}</div>
            <div class="post-actions">
                 <button id="post-like-btn" onclick="togglePostLike(${post.id})">
                    👍 좋아요 (${post.likeCount})
                </button>
                ${actionButtons}
            </div>
        `;
    } catch (error) {
        container.innerHTML = `<p>${error.message}</p>`;
    }
}


/**
 * 새 게시글 작성 폼을 처리합니다.
 * @param {Event} event - 폼 제출 이벤트
 */
async function handleCreatePost(event) {
    event.preventDefault();
    const title = document.getElementById('title').value;
    const content = document.getElementById('content').value;

    try {
        const response = await fetch(`${API_BASE_URL}/api/posts`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ title, content })
        });
        if (!response.ok) throw new Error('글 작성에 실패했습니다.');

        const newPost = await response.json();
        alert('글이 성공적으로 작성되었습니다.');
        window.location.href = `/post.html?id=${newPost.id}`; // 작성된 글로 이동

    } catch (error) {
        alert(error.message);
    }
}

/**
 * 수정 페이지에서 기존 게시글의 내용을 불러옵니다.
 * @param {number} postId - 수정할 게시글의 ID
 */
async function loadPostForEdit(postId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}/edit`, {
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('수정할 게시글 정보를 불러올 수 없습니다.');

        const post = await response.json();
        document.getElementById('title').value = post.title;
        document.getElementById('content').value = post.content;

    } catch (error) {
        alert(error.message);
        window.location.href = '/index.html';
    }
}


/**
 * 게시글 수정 폼을 처리합니다.
 * @param {Event} event - 폼 제출 이벤트
 * @param {number} postId - 수정할 게시글의 ID
 */
async function handleEditPost(event, postId) {
    event.preventDefault();
    const title = document.getElementById('title').value;
    const content = document.getElementById('content').value;

    try {
        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify({ title, content })
        });
        if (!response.ok) throw new Error('글 수정에 실패했습니다.');

        alert('글이 성공적으로 수정되었습니다.');
        window.location.href = `/post.html?id=${postId}`; // 수정된 글로 이동

    } catch (error) {
        alert(error.message);
    }
}


/**
 * 게시글을 삭제합니다.
 * @param {number} postId - 삭제할 게시글의 ID
 */
async function handleDeletePost(postId) {
    if (!confirm('정말로 이 게시글을 삭제하시겠습니까?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('삭제 권한이 없거나, 이미 삭제된 게시글입니다.');

        alert('게시글이 삭제되었습니다.');
        window.location.href = '/index.html';

    } catch (error) {
        alert(error.message);
    }
}

/**
 * 게시글의 '좋아요' 상태를 토글(추가/취소)합니다.
 * @param {number} postId - 좋아요를 토글할 게시글의 ID
 */
async function togglePostLike(postId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}/like`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (response.status === 401) {
            alert('좋아요를 누르려면 로그인이 필요합니다.');
            window.location.href = '/login.html';
            return;
        }
        if (!response.ok) throw new Error('좋아요 처리에 실패했습니다.');

        const updatedPost = await response.json();
        // 좋아요 버튼의 텍스트만 업데이트
        const likeButton = document.getElementById('post-like-btn');
        likeButton.textContent = `👍 좋아요 (${updatedPost.likeCount})`;

        // 좋아요 상태에 따라 알림 메시지 표시
        if (updatedPost.likedByCurrentUser) {
            alert('좋아요를 눌렀습니다.');
        } else {
            alert('좋아요를 취소하였습니다.');
        }

    } catch (error) {
        alert(error.message);
    }
}


// =================================================================
// 댓글 (목록, 생성, 수정, 삭제, 좋아요)
// =================================================================

/**
 * 특정 게시글의 댓글 목록을 가져와 화면에 표시합니다.
 * @param {number} postId - 댓글을 조회할 게시글의 ID
 */
async function fetchComments(postId) {
    const commentsList = document.getElementById('comments-list');
    const commentFormContainer = document.getElementById('comment-form-container');
    const user = getLoggedInUser();

    try {
        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}/comments`, {
            headers: getAuthHeaders() // 좋아요 상태 확인을 위해 인증 헤더 전송
        });
        if (!response.ok) throw new Error('댓글을 불러올 수 없습니다.');

        const comments = await response.json();
        commentsList.innerHTML = '';
        comments.forEach(comment => {
            const commentDiv = document.createElement('div');
            commentDiv.className = 'comment';
            commentDiv.id = `comment-${comment.id}`;

            let actionButtons = '';
            // 현재 로그인 사용자가 댓글 작성자일 경우 수정/삭제 버튼 표시
            if (user && user.username === comment.authorUsername) {
                actionButtons = `
                    <button onclick="showCommentEditForm(${comment.id})">수정</button>
                    <button onclick="handleDeleteComment(${postId}, ${comment.id})">삭제</button>
                `;
            }

            commentDiv.innerHTML = `
                <div class="comment-meta">
                    <strong>${comment.authorUsername}</strong> - 
                    <span>${new Date(comment.createdAt).toLocaleString()}</span>
                </div>

                <!-- 댓글 보기 모드 -->
                <div id="comment-view-${comment.id}">
                    <p>${comment.content.replace(/\n/g, '<br>')}</p>
                    <div class="comment-actions">
                         <button id="comment-like-btn-${comment.id}" onclick="toggleCommentLike(${postId}, ${comment.id})">
                            👍 좋아요 (${comment.likeCount})
                        </button>
                        ${actionButtons}
                    </div>
                </div>

                <!-- 댓글 수정 모드 (초기에는 숨김) -->
                <div id="comment-edit-${comment.id}" style="display: none;">
                    <textarea class="comment-edit-textarea" rows="3">${comment.content}</textarea>
                    <div class="comment-edit-actions">
                        <button onclick="handleEditComment(${postId}, ${comment.id})">저장</button>
                        <button onclick="hideCommentEditForm(${comment.id})">취소</button>
                    </div>
                </div>
            `;
            commentsList.appendChild(commentDiv);
        });

        // 로그인한 경우 댓글 작성 폼을 추가
        if (user) {
            commentFormContainer.innerHTML = `
                <h4>댓글 작성</h4>
                <form id="comment-form">
                    <textarea id="comment-content" rows="3" required></textarea>
                    <button type="submit">등록</button>
                </form>
            `;
            document.getElementById('comment-form').addEventListener('submit', (e) => handleCreateComment(e, postId));
        } else {
            // 로그아웃 상태일 경우 로그인 안내 메시지 표시
            commentFormContainer.innerHTML = `<p><a href="/login.html">로그인</a> 후 댓글을 작성할 수 있습니다.</p>`;
        }
    } catch(error) {
        commentsList.innerHTML = `<p>${error.message}</p>`;
    }
}

/**
 * 새 댓글을 작성합니다.
 * @param {Event} event - 폼 제출 이벤트
 * @param {number} postId - 댓글을 작성할 게시글의 ID
 */
async function handleCreateComment(event, postId) {
    event.preventDefault();
    const content = document.getElementById('comment-content').value;
    if (!content.trim()) return;

    try {
        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}/comments`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ content })
        });
        if (!response.ok) throw new Error('댓글 작성에 실패했습니다.');

        // 댓글 작성 후 댓글 목록 새로고침 및 입력창 비우기
        document.getElementById('comment-content').value = '';
        fetchComments(postId);

    } catch (error) {
        alert(error.message);
    }
}

/**
 * 댓글 수정을 위한 입력 폼을 표시합니다.
 * @param {number} commentId - 수정할 댓글 ID
 */
function showCommentEditForm(commentId) {
    document.getElementById(`comment-view-${commentId}`).style.display = 'none';
    document.getElementById(`comment-edit-${commentId}`).style.display = 'block';
}

/**
 * 댓글 수정 폼을 숨기고 원래 내용으로 되돌립니다.
 * @param {number} commentId - 취소할 댓글 ID
 */
function hideCommentEditForm(commentId) {
    document.getElementById(`comment-view-${commentId}`).style.display = 'block';
    document.getElementById(`comment-edit-${commentId}`).style.display = 'none';
}


/**
 * 댓글 수정을 처리합니다. (서버에 전송)
 * @param {number} postId - 현재 게시글 ID
 * @param {number} commentId - 수정할 댓글 ID
 */
async function handleEditComment(postId, commentId) {
    const content = document.querySelector(`#comment-edit-${commentId} textarea`).value;

    try {
        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}/comments/${commentId}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify({ content })
        });

        if (!response.ok) {
            throw new Error('댓글 수정에 실패했습니다. 권한을 확인하세요.');
        }

        alert('댓글이 성공적으로 수정되었습니다.');
        fetchComments(postId); // 댓글 목록을 새로고침하여 변경사항을 반영

    } catch (error) {
        alert(error.message);
    }
}


/**
 * 댓글을 삭제합니다.
 * @param {number} postId - 현재 게시글 ID
 * @param {number} commentId - 삭제할 댓글 ID
 */
async function handleDeleteComment(postId, commentId) {
    if (!confirm('정말로 이 댓글을 삭제하시겠습니까?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}/comments/${commentId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('댓글 삭제에 실패했습니다.');

        fetchComments(postId); // 댓글 목록 새로고침

    } catch (error) {
        alert(error.message);
    }
}

/**
 * 댓글 '좋아요' 상태를 토글합니다.
 * @param {number} postId - 현재 게시글 ID
 * @param {number} commentId - 좋아요를 토글할 댓글 ID
 */
async function toggleCommentLike(postId, commentId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/posts/${postId}/comments/${commentId}/like`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        if (response.status === 401) {
            alert('좋아요를 누르려면 로그인이 필요합니다.');
            window.location.href = '/login.html';
            return;
        }
        if (!response.ok) throw new Error('좋아요 처리에 실패했습니다.');

        const updatedComment = await response.json();
        // 좋아요 버튼의 텍스트만 업데이트
        const likeButton = document.getElementById(`comment-like-btn-${commentId}`);
        likeButton.textContent = `👍 좋아요 (${updatedComment.likeCount})`;

        // 좋아요 상태에 따라 알림 메시지 표시
        if (updatedComment.likedByCurrentUser) {
            alert('좋아요를 눌렀습니다.');
        } else {
            alert('좋아요를 취소하였습니다.');
        }

    } catch (error) {
        alert(error.message);
    }
}

// =================================================================
// 관리자 기능
// =================================================================

/**
 * 현재 사용자가 관리자인지 확인하고, 아니면 메인 페이지로 보냅니다.
 */
function checkAdminAccess() {
    const user = getLoggedInUser();
    if (!user || user.role !== 'ADMIN') {
        alert('접근 권한이 없습니다.');
        window.location.href = '/index.html';
    }
}

// --- 관리자: 사용자 관리 ---
/**
 * 관리자 페이지에서 모든 사용자 목록을 가져옵니다.
 * @param {number} page - 조회할 페이지 번호
 */
async function fetchAdminUsers(page = 0) {
    const tableBody = document.querySelector("#admin-users-table tbody");
    const paginationContainer = document.getElementById("users-pagination");
    try {
        const response = await fetch(`${API_BASE_URL}/api/admin/users?page=${page}`, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('사용자 정보를 불러올 수 없습니다.');

        const pageData = await response.json();
        tableBody.innerHTML = '';
        pageData.content.forEach(user => {
            const row = tableBody.insertRow();
            row.innerHTML = `
                <td>${user.id}</td>
                <td>${user.username}</td>
                <td>
                    <select id="role-select-${user.id}" onchange="updateUserRole(${user.id})">
                        <option value="USER" ${user.role === 'USER' ? 'selected' : ''}>USER</option>
                        <option value="ADMIN" ${user.role === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                    </select>
                </td>
                <td>${new Date(user.createdAt).toLocaleDateString()}</td>
                <td><button onclick="deleteUser(${user.id})">삭제</button></td>
            `;
        });
        renderPagination(paginationContainer, pageData, fetchAdminUsers, '');

    } catch(error) {
        tableBody.innerHTML = `<tr><td colspan="5">${error.message}</td></tr>`;
        paginationContainer.innerHTML = ''; // 에러 발생 시 페이지네이션 비우기
    }
}

/**
 * 사용자의 역할을 변경합니다.
 * @param {number} userId - 역할을 변경할 사용자 ID
 */
async function updateUserRole(userId) {
    const role = document.getElementById(`role-select-${userId}`).value;
    if (!confirm(`${userId}번 사용자의 역할을 ${role}(으)로 변경하시겠습니까?`)) {
        fetchAdminUsers(0); // 취소 시 선택을 원래대로 되돌리기 위해 첫 페이지를 다시 로드
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/admin/users/${userId}/role?role=${role}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('역할 변경에 실패했습니다.');
        alert('역할이 성공적으로 변경되었습니다.');
    } catch (error) {
        alert(error.message);
        fetchAdminUsers(0); // 실패 시 목록 새로고침
    }
}

/**
 * 사용자를 영구적으로 삭제합니다.
 * @param {number} userId - 삭제할 사용자 ID
 */
async function deleteUser(userId) {
    if (!confirm(`${userId}번 사용자를 영구적으로 삭제하시겠습니까?`)) return;
    try {
        const response = await fetch(`${API_BASE_URL}/api/admin/users/${userId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            alert('사용자가 삭제되었습니다.');
            fetchAdminUsers(0); // 목록 새로고침
        } else {
            const errorData = await response.json();
            throw new Error(errorData.message || '사용자 삭제에 실패했습니다.');
        }
    } catch(error) {
        alert(error.message);
    }
}


// --- 관리자: 게시글 관리 ---
/**
 * 관리자 페이지에서 모든 게시글 목록을 가져옵니다 (삭제된 글 포함).
 * @param {number} page - 조회할 페이지 번호
 * @param {string} search - 검색어
 */
async function fetchAdminPosts(page = 0, search = '') {
    const tableBody = document.querySelector("#admin-posts-table tbody");
    const paginationContainer = document.getElementById("posts-pagination");
    let url = `${API_BASE_URL}/api/admin/posts?page=${page}`;
    if (search) {
        url += `&search=${encodeURIComponent(search)}`;
    }

    try {
        const response = await fetch(url, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('게시글 정보를 불러올 수 없습니다.');

        const pageData = await response.json();
        tableBody.innerHTML = '';
        pageData.content.forEach(post => {
            const row = tableBody.insertRow();
            row.innerHTML = `
                <td>${post.id}</td>
                <td>${post.title}</td>
                <td>${post.authorUsername}</td>
                <td>${post.deleted ? '임시 삭제' : '활성'}</td>
                <td>
                    <button onclick="softDeletePost(${post.id})">임시삭제</button>
                    <button onclick="restorePost(${post.id})">복원</button>
                    <button onclick="hardDeletePost(${post.id})">영구삭제</button>
                </td>
            `;
        });
        renderPagination(paginationContainer, pageData, fetchAdminPosts, search);

    } catch(error) {
        tableBody.innerHTML = `<tr><td colspan="5">${error.message}</td></tr>`;
        paginationContainer.innerHTML = ''; // 에러 발생 시 페이지네이션 비우기
    }
}

/**
 * 관리자 페이지에서 게시글을 검색합니다.
 */
function searchAdminPosts() {
    const search = document.getElementById('admin-post-search').value;
    fetchAdminPosts(0, search);
}

/**
 * 게시글을 임시 삭제(soft delete)합니다.
 * @param {number} postId - 임시 삭제할 게시글 ID
 */
async function softDeletePost(postId) {
    if (!confirm(`${postId}번 글을 임시 삭제하시겠습니까?`)) return;
    await fetch(`${API_BASE_URL}/api/admin/posts/${postId}/soft-delete`, { method: 'POST', headers: getAuthHeaders() });
    const search = document.getElementById('admin-post-search').value;
    fetchAdminPosts(0, search); // 목록 새로고침
}

/**
 * 임시 삭제된 게시글을 복원합니다.
 * @param {number} postId - 복원할 게시글 ID
 */
async function restorePost(postId) {
    if (!confirm(`${postId}번 글을 복원하시겠습니까?`)) return;
    await fetch(`${API_BASE_URL}/api/admin/posts/${postId}/restore`, { method: 'POST', headers: getAuthHeaders() });
    const search = document.getElementById('admin-post-search').value;
    fetchAdminPosts(0, search); // 목록 새로고침
}

/**
 * 게시글을 영구 삭제(hard delete)합니다.
 * @param {number} postId - 영구 삭제할 게시글 ID
 */
async function hardDeletePost(postId) {
    if (!confirm(`${postId}번 글을 영구 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) return;
    await fetch(`${API_BASE_URL}/api/admin/posts/${postId}/hard-delete`, { method: 'DELETE', headers: getAuthHeaders() });
    const search = document.getElementById('admin-post-search').value;
    fetchAdminPosts(0, search); // 목록 새로고침
}


// --- 관리자: 댓글 관리 ---
/**
 * 관리자 페이지에서 모든 댓글 목록을 가져옵니다.
 * @param {number} page - 조회할 페이지 번호
 * @param {string} search - 검색어
 */
async function fetchAdminComments(page = 0, search = '') {
    const tableBody = document.querySelector("#admin-comments-table tbody");
    const paginationContainer = document.getElementById("comments-pagination");
    let url = `${API_BASE_URL}/api/admin/comments?page=${page}`;
    if (search) {
        url += `&search=${encodeURIComponent(search)}`;
    }

    try {
        const response = await fetch(url, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('댓글 정보를 불러올 수 없습니다.');

        const pageData = await response.json();
        tableBody.innerHTML = '';
        pageData.content.forEach(comment => {
            const row = tableBody.insertRow();
            row.innerHTML = `
                <td>${comment.id}</td>
                <td>${comment.content.substring(0, 50)}...</td>
                <td>${comment.authorUsername}</td>
                <td><a href="/post.html?id=${comment.postId}" target="_blank">${comment.postTitle}</a></td>
                <td>
                    <button onclick="deleteCommentAsAdmin(${comment.id})">영구삭제</button>
                </td>
            `;
        });
        renderPagination(paginationContainer, pageData, fetchAdminComments, search);

    } catch(error) {
        tableBody.innerHTML = `<tr><td colspan="5">${error.message}</td></tr>`;
        paginationContainer.innerHTML = ''; // 에러 발생 시 페이지네이션 비우기
    }
}

/**
 * 관리자 페이지에서 댓글을 검색합니다.
 */
function searchAdminComments() {
    const search = document.getElementById('admin-comment-search').value;
    fetchAdminComments(0, search);
}

/**
 * 댓글을 관리자 권한으로 영구 삭제합니다.
 * @param {number} commentId - 삭제할 댓글 ID
 */
async function deleteCommentAsAdmin(commentId) {
    if (!confirm(`${commentId}번 댓글을 영구 삭제하시겠습니까?`)) return;
    await fetch(`${API_BASE_URL}/api/admin/comments/${commentId}`, { method: 'DELETE', headers: getAuthHeaders() });
    const search = document.getElementById('admin-comment-search').value;
    fetchAdminComments(0, search); // 목록 새로고침
}


// =================================================================
// 페이지네이션
// =================================================================

/**
 * 페이지네이션 버튼을 생성하여 화면에 표시합니다.
 * @param {HTMLElement} container - 페이지네이션 버튼이 들어갈 부모 요소
 * @param {object} pageData - 서버에서 받은 페이지 정보 (totalPages, number 등)
 * @param {Function} fetchFunction - 페이지 이동 시 호출할 함수
 * @param {string} search - 현재 검색어 (페이지 이동 시 유지하기 위함)
 */
function renderPagination(container, pageData, fetchFunction, search) {
    if (!container) return; // 컨테이너가 없으면 종료
    container.innerHTML = '';
    const { totalPages, number: currentPage } = pageData;

    if (totalPages <= 1) return; // 페이지가 1개 이하면 표시 안 함

    // '이전' 버튼
    if (currentPage > 0) {
        const prevButton = document.createElement('button');
        prevButton.innerText = '이전';
        prevButton.onclick = () => fetchFunction(currentPage - 1, search);
        container.appendChild(prevButton);
    }

    // 페이지 번호 버튼들
    for (let i = 0; i < totalPages; i++) {
        const pageButton = document.createElement('button');
        pageButton.innerText = i + 1;
        if (i === currentPage) {
            pageButton.classList.add('active'); // 현재 페이지는 활성화 스타일 적용
        }
        pageButton.onclick = () => fetchFunction(i, search);
        container.appendChild(pageButton);
    }

    // '다음' 버튼
    if (currentPage < totalPages - 1) {
        const nextButton = document.createElement('button');
        nextButton.innerText = '다음';
        nextButton.onclick = () => fetchFunction(currentPage + 1, search);
        container.appendChild(nextButton);
    }
}