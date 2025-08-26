// src/main/resources/static/js/index.js

import { renderPagination } from './utils.js'; // utils.js에서 renderPagination 임포트

document.addEventListener('DOMContentLoaded', async () => {
    const postListElem = document.getElementById('post-list');
    const paginationElem = document.getElementById('pagination');
    const searchInput = document.getElementById('search-input');
    const searchButton = document.getElementById('search-button');

    let currentPage = 0;
    const pageSize = 10;
    let currentSearchKeyword = '';

    // 게시글 목록을 로드하는 함수
    async function loadPosts(page = 0, searchKeyword = '') {
        // colspan을 7로 변경
        postListElem.innerHTML = '<tr><td colspan="7" class="no-posts">게시글을 불러오는 중...</td></tr>';
        paginationElem.innerHTML = '';

        currentPage = page;
        currentSearchKeyword = searchKeyword;

        try {
            const url = `/api/posts?page=${page}&size=${pageSize}${searchKeyword ? `&search=${encodeURIComponent(searchKeyword)}` : ''}`;
            const response = await fetch(url);

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '게시글 로드 실패');
            }

            const pageData = await response.json();
            const posts = pageData.content;
            const totalPages = pageData.totalPages;

            postListElem.innerHTML = '';

            if (posts.length === 0) {
                // colspan을 7로 변경
                postListElem.innerHTML = '<tr><td colspan="7" class="no-posts">게시글이 없습니다.</td></tr>';
            } else {
                posts.forEach(post => {
                    const row = document.createElement('tr');
                    const createdAt = new Date(post.createdAt);
                    const formattedDate = `${createdAt.getFullYear()}-${(createdAt.getMonth() + 1).toString().padStart(2, '0')}-${createdAt.getDate().toString().padStart(2, '0')}`;

                    row.innerHTML = `
                        <td class="col-id">${post.id}</td>
                        <td class="col-title"><a href="/post-detail.html?id=${post.id}">${post.title}</a></td>
                        <td class="col-author">${post.authorUsername}</td>
                        <td class="col-date">${formattedDate}</td>
                        <td class="col-views">${post.viewCount}</td>
                        <td class="col-likes">❤️ ${post.likeCount}</td> <td class="col-comments">💬 ${post.commentCount}</td> `;
                    postListElem.appendChild(row);
                });
            }

            renderPagination(paginationElem, totalPages, currentPage, loadPosts, currentSearchKeyword);

        } catch (error) {
            console.error('게시글 로드 중 오류 발생:', error);
            // colspan을 7로 변경
            postListElem.innerHTML = `<tr><td colspan="7" class="no-posts">게시글을 불러오는데 실패했습니다: ${error.message}</td></tr>`;
        }
    }

    // 검색 버튼 클릭 이벤트 리스너
    searchButton.addEventListener('click', () => {
        const searchKeyword = searchInput.value.trim();
        loadPosts(0, searchKeyword);
    });

    // 엔터 키로 검색 실행
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            searchButton.click();
        }
    });

    // 초기 게시글 로드
    loadPosts(currentPage, currentSearchKeyword);
});