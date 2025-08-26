// src/main/resources/static/js/utils.js

/**
 * Date 객체를 YYYY-MM-DD HH:MM 형식으로 포매팅합니다.
 * @param {Date} date Date 객체
 * @returns {string} 포매팅된 날짜 문자열
 */
export function formatDateTime(date) {
    if (!date) return '';
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

/**
 * 좋아요 버튼의 텍스트와 클래스를 업데이트합니다.
 * @param {HTMLElement} buttonElement 좋아요 버튼 요소
 * @param {boolean} liked 현재 사용자가 좋아요를 눌렀는지 여부
 */
export function updateLikeButtonState(buttonElement, liked) {
    if (liked) {
        buttonElement.classList.add('liked');
        buttonElement.textContent = '❤️ 좋아요 취소';
    } else {
        buttonElement.classList.remove('liked');
        buttonElement.textContent = '🤍 좋아요';
    }
}

/**
 * 페이지네이션 버튼을 렌더링합니다.
 * @param {HTMLElement} paginationContainer 페이지네이션 버튼을 추가할 컨테이너 요소
 * @param {number} totalPages 전체 페이지 수
 * @param {number} currentPage 현재 페이지 (0-based index)
 * @param {function} loadFunction 페이지 클릭 시 호출할 함수
 * @param {string} [searchKeyword=''] 검색 키워드 (선택 사항)
 */
export function renderPagination(paginationContainer, totalPages, currentPage, loadFunction, searchKeyword = '') {
    paginationContainer.innerHTML = '';
    if (totalPages <= 1) return;

    const maxPagesToShow = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);

    if (endPage - startPage + 1 < maxPagesToShow) {
        startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }

    const createButton = (text, pageIndex, isDisabled = false, isActive = false) => {
        const button = document.createElement('a');
        button.href = '#';
        button.textContent = text;
        if (isDisabled) {
            button.classList.add('disabled');
        } else {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                loadFunction(pageIndex, searchKeyword);
            });
        }
        if (isActive) {
            button.classList.add('current-page');
        }
        return button;
    };

    if (currentPage > 0) {
        paginationContainer.appendChild(createButton('이전', currentPage - 1));
    } else {
        const span = document.createElement('span');
        span.classList.add('disabled');
        span.textContent = '이전';
        paginationContainer.appendChild(span);
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageButton = createButton(i + 1, i, false, i === currentPage);
        if (i === currentPage) {
            const span = document.createElement('span');
            span.classList.add('current-page');
            span.textContent = i + 1;
            paginationContainer.appendChild(span);
        } else {
            paginationContainer.appendChild(pageButton);
        }
    }

    if (currentPage < totalPages - 1) {
        paginationContainer.appendChild(createButton('다음', currentPage + 1));
    } else {
        const span = document.createElement('span');
        span.classList.add('disabled');
        span.textContent = '다음';
        paginationContainer.appendChild(span);
    }
}