import {useEffect, useMemo, useState} from 'react';
import {api, getRole} from '../api';

const ROLES = ['USER', 'ADMIN'];

export default function AdminUsersPage() {
    const isAdmin = useMemo(() => getRole() === 'ADMIN', []);
    const [q, setQ] = useState('');
    const [page, setPage] = useState(0);
    const [size] = useState(10);

    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');

    async function load() {
        setLoading(true);
        setError('');
        try {
            const params = {page: String(page), size: String(size)};
            if (q.trim()) params.q = q.trim();
            const res = await api.listUsers(params);
            setData(res);
        } catch (e) {
            setError(e?.message ?? 'Failed to load users');
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        load();
    }, [page, size]);

    if (!isAdmin) {
        return (
            <div className="tasks-container">
                <div className="notice notice-error">Access denied</div>
            </div>
        );
    }

    const users = data?.content ?? [];
    const totalPages = data?.totalPages ?? 0;

    async function onChangeRole(userId, role) {
        setBusy(true);
        setError('');
        try {
            await api.updateUserRole(userId, role);
            await load();
        } catch (e) {
            setError(e?.message ?? 'Failed to update role');
        } finally {
            setBusy(false);
        }
    }

    async function onDelete(userId) {
        if (!confirm('Delete this user?')) return;
        setBusy(true);
        setError('');
        try {
            await api.deleteUser(userId);
            await load();
        } catch (e) {
            setError(e?.message ?? 'Failed to delete user');
        } finally {
            setBusy(false);
        }
    }

    return (
        <div className="tasks-container">
            <div className="tasks-header">
                <h2 className="tasks-title">Users</h2>
            </div>

            <div className="tasks-panel card">
                <div className="tasks-actions">
                    <input
                        className="input"
                        placeholder="Search by email..."
                        value={q}
                        onChange={(e) => setQ(e.target.value)}
                        disabled={busy}
                    />
                    <button
                        className="btn"
                        type="button"
                        disabled={busy}
                        onClick={() => {
                            setPage(0);
                            load();
                        }}
                    >
                        Search
                    </button>
                    <button
                        className="btn"
                        type="button"
                        disabled={busy}
                        onClick={() => {
                            setQ('');
                            setPage(0);
                            setTimeout(load, 0);
                        }}
                    >
                        Reset
                    </button>
                </div>
            </div>

            {loading && <div className="notice notice-muted">Loading...</div>}
            {error && <div className="notice notice-error">{error}</div>}

            {!loading && !error && (
                <div className="card" style={{padding: 0, overflow: 'hidden'}}>
                    <div style={{display: 'grid', gridTemplateColumns: '1fr 160px 140px', gap: 0}}>
                        <div className="table-head">Email</div>
                        <div className="table-head">Role</div>
                        <div className="table-head">Actions</div>

                        {users.map(u => (
                            <>
                                <div className="table-cell">{u.email}</div>

                                <div className="table-cell">
                                    <select
                                        className="select select-compact"
                                        value={u.role}
                                        disabled={busy}
                                        onChange={(e) => onChangeRole(u.id, e.target.value)}
                                    >
                                        {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                                    </select>
                                </div>

                                <div className="table-cell">
                                    <button className="btn btn-danger" disabled={busy} onClick={() => onDelete(u.id)}>
                                        Delete
                                    </button>
                                </div>
                            </>
                        ))}
                    </div>
                </div>
            )}

            <div className="pager">
                <button className="btn" disabled={busy || page <= 0} onClick={() => setPage(p => p - 1)}>Prev</button>
                <div className="notice notice-muted pager-pill">Page {page + 1} / {Math.max(totalPages, 1)}</div>
                <button className="btn" disabled={busy || totalPages === 0 || page >= totalPages - 1}
                        onClick={() => setPage(p => p + 1)}>Next
                </button>
            </div>
        </div>
    );
}