/**
 * 校内BBS 公共 JavaScript 工具
 */
(function () {
    'use strict';

    function getToken() {
        return localStorage.getItem('token') || null;
    }

    function parseJwtPayload(token) {
        try {
            var parts = token.split('.');
            if (parts.length !== 3) return null;
            var base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
            var json = decodeURIComponent(atob(base64).split('').map(function (c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(json);
        } catch (e) { return null; }
    }

    function getCurrentUserId() {
        var token = getToken();
        if (!token) return null;
        var payload = parseJwtPayload(token);
        return payload ? payload.userId : null;
    }

    function getCurrentRole() {
        var token = getToken();
        if (!token) return null;
        var payload = parseJwtPayload(token);
        return payload ? payload.role : null;
    }

    function isLoggedIn() {
        return !!getToken();
    }

    function buildHeaders(extraHeaders) {
        var headers = extraHeaders || {};
        var token = getToken();
        if (token) headers['Authorization'] = 'Bearer ' + token;
        return headers;
    }

    function handleResponse(response) {
        if (response.status === 401) {
            localStorage.clear();
            if (window.location.pathname !== '/login') {
                showToast('登录已过期，请重新登录', 'error');
                setTimeout(function () { window.location.href = '/login'; }, 1000);
            }
            return Promise.reject(new Error('未认证'));
        }
        if (response.status === 403) {
            showToast('无权限执行此操作', 'error');
            return Promise.reject(new Error('无权限'));
        }
        return response.json();
    }

    function apiGet(url) {
        return fetch(url, { method: 'GET', headers: buildHeaders() }).then(handleResponse);
    }

    function apiPost(url, data) {
        return fetch(url, {
            method: 'POST',
            headers: buildHeaders({ 'Content-Type': 'application/json' }),
            body: JSON.stringify(data)
        }).then(handleResponse);
    }

    function apiPut(url, data) {
        var options = { method: 'PUT', headers: buildHeaders({ 'Content-Type': 'application/json' }) };
        if (data !== undefined && data !== null) options.body = JSON.stringify(data);
        return fetch(url, options).then(handleResponse);
    }

    function apiDel(url) {
        return fetch(url, { method: 'DELETE', headers: buildHeaders() }).then(handleResponse);
    }

    function showToast(message, type) {
        var container = document.getElementById('globalToastContainer');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container position-fixed top-0 end-0 p-3';
            container.id = 'globalToastContainer';
            document.body.appendChild(container);
        }
        var bgClass = type === 'success' ? 'bg-success' :
            type === 'error' || type === 'danger' ? 'bg-danger' :
                type === 'warning' ? 'bg-warning text-dark' : 'bg-info';
        var toastId = 'toast-' + Date.now();
        var html = '<div class="toast align-items-center ' + bgClass + ' border-0" id="' + toastId + '" role="alert">' +
            '<div class="d-flex"><div class="toast-body">' + escapeHtml(String(message)) + '</div>' +
            '<button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast"></button></div></div>';
        container.insertAdjacentHTML('beforeend', html);
        var toastEl = document.getElementById(toastId);
        var toast = new bootstrap.Toast(toastEl, { delay: 3000 });
        toast.show();
        toastEl.addEventListener('hidden.bs.toast', function () { toastEl.remove(); });
    }

    function escapeHtml(text) {
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function renderPagination(current, pages, callback) {
        if (pages <= 1) return '';
        var html = '<ul class="pagination pagination-sm justify-content-center">';
        html += '<li class="page-item' + (current <= 1 ? ' disabled' : '') + '">';
        html += '<a class="page-link" href="javascript:void(0)" data-page="' + (current - 1) + '">上一页</a></li>';
        var start = Math.max(1, current - 2);
        var end = Math.min(pages, current + 2);
        if (start > 1) {
            html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="1">1</a></li>';
            if (start > 2) html += '<li class="page-item disabled"><span class="page-link">...</span></li>';
        }
        for (var i = start; i <= end; i++) {
            html += '<li class="page-item' + (i === current ? ' active' : '') + '">';
            html += '<a class="page-link" href="javascript:void(0)" data-page="' + i + '">' + i + '</a></li>';
        }
        if (end < pages) {
            if (end < pages - 1) html += '<li class="page-item disabled"><span class="page-link">...</span></li>';
            html += '<li class="page-item"><a class="page-link" href="javascript:void(0)" data-page="' + pages + '">' + pages + '</a></li>';
        }
        html += '<li class="page-item' + (current >= pages ? ' disabled' : '') + '">';
        html += '<a class="page-link" href="javascript:void(0)" data-page="' + (current + 1) + '">下一页</a></li>';
        html += '</ul>';
        return html;
    }

    function bindPagination(container, callback) {
        if (!container) return;
        container.addEventListener('click', function (e) {
            var link = e.target.closest('.page-link');
            if (!link) return;
            var page = parseInt(link.getAttribute('data-page'));
            if (page && callback) { e.preventDefault(); callback(page); }
        });
    }

    function formatTime(dateStr) {
        if (!dateStr) return '';
        var date = new Date(dateStr);
        if (isNaN(date.getTime())) return dateStr;
        var now = new Date();
        var diff = now - date;
        var minute = 60 * 1000, hour = 60 * minute, day = 24 * hour;
        if (diff < minute) return '刚刚';
        if (diff < hour) return Math.floor(diff / minute) + '分钟前';
        if (diff < day) return Math.floor(diff / hour) + '小时前';
        if (diff < 7 * day) return Math.floor(diff / day) + '天前';
        var y = date.getFullYear();
        var m = ('0' + (date.getMonth() + 1)).slice(-2);
        var d = ('0' + date.getDate()).slice(-2);
        return y + '-' + m + '-' + d;
    }

    window.BbsApi = {
        get: apiGet, post: apiPost, put: apiPut, del: apiDel
    };
    window.BbsToast = {
        success: function (msg) { showToast(msg, 'success'); },
        error: function (msg) { showToast(msg, 'error'); },
        warning: function (msg) { showToast(msg, 'warning'); },
        info: function (msg) { showToast(msg, 'info'); }
    };
    window.BbsAuth = {
        getToken: getToken,
        getUserId: getCurrentUserId,
        getRole: getCurrentRole,
        parsePayload: function () { return parseJwtPayload(getToken()); },
        isLoggedIn: isLoggedIn,
        logout: function () {
            localStorage.removeItem('token');
            localStorage.removeItem('expiresIn');
            window.location.href = '/login';
        }
    };
    window.BbsUtil = {
        renderPagination: renderPagination,
        bindPagination: bindPagination,
        formatTime: formatTime,
        escapeHtml: escapeHtml
    };

    // ==================== 导航栏初始化 ====================

    function initNavbar() {
        var token = getToken();
        var beforeLogin = document.getElementById('navBeforeLogin');
        var afterLogin = document.getElementById('navAfterLogin');

        if (!beforeLogin || !afterLogin) {
            if (!initNavbar._retryCount) initNavbar._retryCount = 0;
            if (initNavbar._retryCount++ < 30) {
                // 递增延迟重试：第1次100ms，第10次500ms
                var delay = Math.min(100 + initNavbar._retryCount * 20, 500);
                setTimeout(initNavbar, delay);
            } else {
                console.warn('导航栏初始化失败：超过最大重试次数');
            }
            return;
        }

        if (token) {
            beforeLogin.style.display = 'none';
            afterLogin.style.display = '';

            var payload = parseJwtPayload(token);
            if (payload) {
                var usernameEl = document.getElementById('navUsername');
                if (usernameEl) usernameEl.textContent = payload.username || '用户';

                if (payload.role === 1) {
                    ['adminMenuEntry', 'adminPostMenuEntry', 'adminBoardMenuEntry', 'adminDivider'].forEach(function(id) {
                        var el = document.getElementById(id);
                        if (el) el.style.display = '';
                    });
                }
            }

            // 异步获取积分（带超时）
            var controller = new AbortController();
            var timeoutId = setTimeout(function() { controller.abort(); }, 3000);

            fetch('/api/v1/user/profile', {
                headers: { 'Authorization': 'Bearer ' + token },
                signal: controller.signal
            })
                .then(function(res) { clearTimeout(timeoutId); return res.json(); })
                .then(function(result) {
                    if (result && result.code === 200 && result.data) {
                        var scoreEl = document.getElementById('navScore');
                        if (scoreEl) scoreEl.textContent = '🪙 ' + (result.data.score || 0);
                        var usernameEl = document.getElementById('navUsername');
                        if (usernameEl) {
                            usernameEl.textContent = result.data.realName || result.data.username || '用户';
                        }
                    }
                })
                .catch(function(err) {
                    if (err.name === 'AbortError') {
                        console.warn('获取用户积分超时');
                    }
                });
        }

        // 退出登录
        var logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', function() {
                localStorage.removeItem('token');
                localStorage.removeItem('expiresIn');
                window.location.href = '/login';
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initNavbar);
    } else {
        initNavbar();
    }

})();