import {useEffect, useMemo, useState} from 'react';
import {api, getRole} from '../api';
import UserSelect from '../components/UserSelect';
import {useNavigate} from 'react-router-dom';

const STATUSES = ['', 'TODO', 'IN_PROGRESS', 'DONE'];
const USER_STATUSES = ['TODO', 'IN_PROGRESS', 'DONE'];

export default function TasksPage() {
    const role = useMemo(() => getRole(), []);
    const isAdmin = role === 'ADMIN';

    const [page, setPage] = useState(0);
    const [size] = useState(10);

    const [statusFilter, setStatusFilter] = useState('');
    const [deadlineFrom, setDeadlineFrom] = useState('');
    const [deadlineTo, setDeadlineTo] = useState('');

    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');

    const [editingId, setEditingId] = useState(null);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [status, setStatus] = useState('TODO');
    const [deadline, setDeadline] = useState('');
    const [assignee, setAssignee] = useState({id: null, email: ''});
    const navigate = useNavigate();

    async function load() {
        setLoading(true);
        setError('');

        try {
            const params = {page: String(page), size: String(size)};
            if (statusFilter) params.status = statusFilter;
            if (deadlineFrom) params.deadlineFrom = deadlineFrom;
            if (deadlineTo) params.deadlineTo = deadlineTo;

            const res = await api.listTasks(params);
            setData(res);
        } catch (e) {
            setError(e?.message ?? 'Failed to load tasks');
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        load();
    }, [page, size, statusFilter, deadlineFrom, deadlineTo]);

    function resetForm() {
        setEditingId(null);
        setTitle('');
        setDescription('');
        setStatus('TODO');
        setDeadline('');
        setAssignee({id: null, email: ''});
    }

    function startEdit(t) {
        if (!isAdmin) return;

        setEditingId(t.id);
        setTitle(t.title ?? '');
        setDescription(t.description ?? '');
        setStatus(t.status ?? 'TODO');
        setDeadline(t.deadline ?? '');

        setAssignee({
            id: t.assigneeId ?? null,
            email: t.assigneeEmail ?? '',
        });

        window.scrollTo({top: 0, behavior: 'smooth'});
    }

    async function onSubmit(e) {
        e.preventDefault();
        if (!isAdmin) return;

        setError('');
        setBusy(true);

        const payload = {
            title: title.trim(),
            description: description.trim() || null,
            status,
            deadline: deadline || null,
            assigneeId: assignee?.id == null ? null : Number(assignee.id),
        };

        try {
            if (editingId) {
                await api.updateTask(editingId, payload);
            } else {
                await api.createTask(payload);
            }
            resetForm();
            await load();
        } catch (e) {
            setError(e?.message ?? 'Save failed');
        } finally {
            setBusy(false);
        }
    }

    async function onDelete(id) {
        if (!isAdmin) return;
        if (!confirm('Delete this task?')) return;

        setError('');
        setBusy(true);

        try {
            await api.deleteTask(id);
            if (editingId === id) resetForm();
            await load();
        } catch (e) {
            setError(e?.message ?? 'Delete failed');
        } finally {
            setBusy(false);
        }
    }

    async function onChangeStatus(t, newStatus) {
        setError('');
        setBusy(true);
        try {
            await api.updateTaskStatus(t.id, newStatus);
            await load();
        } catch (e) {
            setError(e?.message ?? 'Failed to update status');
        } finally {
            setBusy(false);
        }
    }

    function applyFilters(next) {
        setPage(0);
        next();
    }

    const tasks = data?.content ?? [];
    const totalPages = data?.totalPages ?? 0;

    return (
        <div className="tasks-container">
            <div className="tasks-header">
                <h2 className="tasks-title">{isAdmin ? 'All tasks' : 'My tasks'}</h2>
                {isAdmin && (
                    <button className="btn" type="button" onClick={() => navigate('/admin/users')}>
                        Users
                    </button>
                )}
            </div>

            {/* ADMIN PANEL */}
            {isAdmin && (
                <div className="tasks-panel card">
                    <form className="tasks-form" onSubmit={onSubmit}>
                        <div className="tasks-row">
                            <input
                                className="input"
                                placeholder="Title"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                required
                                disabled={busy}
                            />

                            <select
                                className="select"
                                value={status}
                                onChange={(e) => setStatus(e.target.value)}
                                disabled={busy}
                            >
                                <option value="TODO">TODO</option>
                                <option value="IN_PROGRESS">IN_PROGRESS</option>
                                <option value="DONE">DONE</option>
                            </select>

                            <input
                                className="input"
                                type="date"
                                value={deadline}
                                onChange={(e) => setDeadline(e.target.value)}
                                disabled={busy}
                            />
                        </div>

                        <textarea
                            className="textarea"
                            placeholder="Description (optional)"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            disabled={busy}
                        />

                        <div className="tasks-row">
                            <UserSelect
                                value={assignee}
                                onChange={setAssignee}
                                disabled={busy}
                                apiSearch={(q) => api.searchUsers(q)}
                                label="Assignee (email)"
                                searchPlaceholder="Search user by email..."
                            />
                            <div/>
                            <div/>
                        </div>

                        <div className="tasks-actions">
                            <button className="btn btn-primary" type="submit" disabled={busy}>
                                {editingId ? 'Update task' : 'Create task'}
                            </button>

                            {editingId && (
                                <button className="btn" type="button" onClick={resetForm} disabled={busy}>
                                    Cancel edit
                                </button>
                            )}
                        </div>
                    </form>

                    <div className="filters-separator"/>

                    <div className="tasks-actions">
                        <select
                            className="select"
                            value={statusFilter}
                            onChange={(e) => applyFilters(() => setStatusFilter(e.target.value))}
                            disabled={busy}
                        >
                            {STATUSES.map((s) => (
                                <option key={s || 'ALL'} value={s}>
                                    {s ? s : 'All statuses'}
                                </option>
                            ))}
                        </select>

                        <input
                            className="input"
                            type="date"
                            value={deadlineFrom}
                            onChange={(e) => applyFilters(() => setDeadlineFrom(e.target.value))}
                            disabled={busy}
                        />

                        <input
                            className="input"
                            type="date"
                            value={deadlineTo}
                            onChange={(e) => applyFilters(() => setDeadlineTo(e.target.value))}
                            disabled={busy}
                        />

                        <button
                            className="btn"
                            type="button"
                            onClick={() => {
                                setStatusFilter('');
                                setDeadlineFrom('');
                                setDeadlineTo('');
                                setPage(0);
                            }}
                            disabled={busy}
                        >
                            Reset filters
                        </button>
                    </div>
                </div>
            )}

            {/* USER filters only */}
            {!isAdmin && (
                <div className="tasks-panel card">
                    <div className="tasks-actions">
                        <select
                            className="select"
                            value={statusFilter}
                            onChange={(e) => applyFilters(() => setStatusFilter(e.target.value))}
                            disabled={busy}
                        >
                            {STATUSES.map((s) => (
                                <option key={s || 'ALL'} value={s}>
                                    {s ? s : 'All statuses'}
                                </option>
                            ))}
                        </select>

                        <input
                            className="input"
                            type="date"
                            value={deadlineFrom}
                            onChange={(e) => applyFilters(() => setDeadlineFrom(e.target.value))}
                            disabled={busy}
                        />

                        <input
                            className="input"
                            type="date"
                            value={deadlineTo}
                            onChange={(e) => applyFilters(() => setDeadlineTo(e.target.value))}
                            disabled={busy}
                        />

                        <button
                            className="btn"
                            type="button"
                            onClick={() => {
                                setStatusFilter('');
                                setDeadlineFrom('');
                                setDeadlineTo('');
                                setPage(0);
                            }}
                            disabled={busy}
                        >
                            Reset filters
                        </button>
                    </div>
                </div>
            )}

            {loading && <div className="notice notice-muted">Loading...</div>}
            {error && <div className="notice notice-error">{error}</div>}

            {!loading && !error && tasks.length === 0 && (
                <div className="notice notice-muted">{isAdmin ? 'No tasks yet.' : 'No tasks assigned yet.'}</div>
            )}

            {!loading && !error && tasks.length > 0 && (
                <>
                    <div className="tasks-grid">
                        {tasks.map((t) => (
                            <div key={t.id} className="task-card card">
                                <div className="task-top">
                                    <div className="task-title">{t.title}</div>

                                    <div className="task-right">
                                        {!isAdmin ? (
                                            <select
                                                className="select select-compact"
                                                value={t.status ?? 'TODO'}
                                                disabled={busy}
                                                onChange={(e) => onChangeStatus(t, e.target.value)}
                                            >
                                                {USER_STATUSES.map((s) => (
                                                    <option key={s} value={s}>
                                                        {s}
                                                    </option>
                                                ))}
                                            </select>
                                        ) : (
                                            <div
                                                className={`task-status task-status--${t.status ?? 'TODO'}`}>{t.status}</div>
                                        )}

                                        {isAdmin && (
                                            <div className="task-actions">
                                                <button className="btn" type="button" onClick={() => startEdit(t)}
                                                        disabled={busy}>
                                                    Edit
                                                </button>
                                                <button className="btn btn-danger" type="button"
                                                        onClick={() => onDelete(t.id)} disabled={busy}>
                                                    Delete
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                </div>

                                {t.description && <div className="task-desc">{t.description}</div>}

                                <div className="task-meta">
                                    {t.deadline ? (
                                        <span>
                      <strong>Deadline:</strong> {t.deadline}
                    </span>
                                    ) : (
                                        <span className="notice-muted">No deadline</span>
                                    )}

                                    {t.assigneeEmail ? (
                                        <span className="task-meta-sep">
                      <strong>Assignee:</strong> {t.assigneeEmail}
                    </span>
                                    ) : (
                                        <span className="task-meta-sep notice-muted">Unassigned</span>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="pager">
                        <button className="btn" disabled={busy || page <= 0} onClick={() => setPage((p) => p - 1)}>
                            Prev
                        </button>

                        <div className="notice notice-muted pager-pill">
                            Page {page + 1} / {Math.max(totalPages, 1)}
                        </div>

                        <button
                            className="btn"
                            disabled={busy || totalPages === 0 || page >= totalPages - 1}
                            onClick={() => setPage((p) => p + 1)}
                        >
                            Next
                        </button>
                    </div>
                </>
            )}
        </div>
    );
}