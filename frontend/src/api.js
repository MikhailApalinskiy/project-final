const TOKEN_KEY = 'accessToken';

export function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

export function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

export function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
}

function notifyLogout() {
    window.dispatchEvent(new Event('auth:logout'));
}

function parseJwt(token) {
    try {
        const payload = token.split('.')[1];
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
        const json = decodeURIComponent(
            atob(base64)
                .split('')
                .map(c => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
                .join('')
        );
        return JSON.parse(json);
    } catch {
        return null;
    }
}

export function getRole() {
    const token = getToken();
    const payload = token ? parseJwt(token) : null;
    return payload?.role ?? null;
}

async function request(path, {method = 'GET', body} = {}) {
    const headers = {'Content-Type': 'application/json'};

    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });

    if (res.status === 401) {
        clearToken();
        notifyLogout();
        throw new Error('Unauthorized');
    }

    if (!res.ok) {
        let message = `HTTP ${res.status}`;
        try {
            const err = await res.json();
            message = err.message ?? message;
        } catch (_) {
        }
        throw new Error(message);
    }

    if (res.status === 204) {
        return null;
    }

    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

export const api = {
    login: (email, password) =>
        request('/api/auth/login', {
            method: 'POST',
            body: {email, password},
        }),

    register: (email, password) =>
        request('/api/auth/register', {
            method: 'POST',
            body: {email, password},
        }),

    listTasks: (params = {}) => {
        const qs = new URLSearchParams(params).toString();
        return request(`/api/tasks${qs ? `?${qs}` : ''}`);
    },

    getTask: (id) => request(`/api/tasks/${id}`),

    createTask: (payload) =>
        request('/api/tasks', {
            method: 'POST',
            body: payload,
        }),

    updateTask: (id, payload) =>
        request(`/api/tasks/${id}`, {
            method: 'PUT',
            body: payload,
        }),

    updateTaskStatus: (id, status) =>
        request(`/api/tasks/${id}/status`, {
            method: 'PATCH',
            body: {status},
        }),

    deleteTask: (id) =>
        request(`/api/tasks/${id}`, {
            method: 'DELETE',
        }),

    searchUsers: (q) => request(`/api/users?q=${encodeURIComponent(q)}`),

    listUsers: (params = {}) => {
        const qs = new URLSearchParams(params).toString();
        return request(`/api/admin/users${qs ? `?${qs}` : ''}`);
    },

    updateUserRole: (id, role) =>
        request(`/api/admin/users/${id}/role`, {
            method: 'PATCH',
            body: {role},
        }),

    deleteUser: (id) =>
        request(`/api/admin/users/${id}`, {method: 'DELETE'}),
};

