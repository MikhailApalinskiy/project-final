import {useEffect, useRef, useState} from 'react';

export default function UserSelect({
                                       value,
                                       onChange,
                                       apiSearch,
                                       disabled = false,
                                       label = 'Assignee (email)',
                                       searchPlaceholder = 'Search user by email...',
                                   }) {
    const [open, setOpen] = useState(false);
    const [q, setQ] = useState('');
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [activeIndex, setActiveIndex] = useState(-1);

    const rootRef = useRef(null);
    const searchRef = useRef(null);

    useEffect(() => {
        function onDocMouseDown(e) {
            if (!rootRef.current) return;
            if (!rootRef.current.contains(e.target)) {
                setOpen(false);
                setActiveIndex(-1);
            }
        }

        document.addEventListener('mousedown', onDocMouseDown);
        return () => document.removeEventListener('mousedown', onDocMouseDown);
    }, []);

    useEffect(() => {
        if (open) {
            setTimeout(() => searchRef.current?.focus(), 0);
        } else {
            setQ('');
            setItems([]);
            setLoading(false);
            setActiveIndex(-1);
        }
    }, [open]);

    useEffect(() => {
        let cancelled = false;

        async function run() {
            const query = q.trim();
            if (!open) return;

            if (query.length < 2) {
                setItems([]);
                setLoading(false);
                return;
            }

            setLoading(true);
            try {
                const res = await apiSearch(query);
                if (!cancelled) setItems(Array.isArray(res) ? res : []);
            } catch {
                if (!cancelled) setItems([]);
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        const t = setTimeout(run, 250);
        return () => {
            cancelled = true;
            clearTimeout(t);
        };
    }, [q, open, apiSearch]);

    function selectUser(u) {
        onChange({id: u.id, email: u.email});
        setOpen(false);
    }

    function clearUser() {
        onChange({id: null, email: ''});
        setOpen(false);
    }

    function onSearchKeyDown(e) {
        if (e.key === 'Escape') {
            setOpen(false);
            return;
        }

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            setActiveIndex((i) => Math.min(i + 1, items.length - 1));
            return;
        }

        if (e.key === 'ArrowUp') {
            e.preventDefault();
            setActiveIndex((i) => Math.max(i - 1, 0));
            return;
        }

        if (e.key === 'Enter') {
            if (activeIndex >= 0 && activeIndex < items.length) {
                e.preventDefault();
                selectUser(items[activeIndex]);
            }
        }
    }

    return (
        <div className={`ucombo ${disabled ? 'is-disabled' : ''}`} ref={rootRef}>
            {label && <div className="ucombo-label">{label}</div>}

            <button
                type="button"
                className="ucombo-trigger"
                disabled={disabled}
                onClick={() => setOpen((v) => !v)}
                aria-haspopup="listbox"
                aria-expanded={open}
            >
        <span className={`ucombo-value ${value?.email ? '' : 'is-placeholder'}`}>
          {value?.email ? value.email : 'Unassigned'}
        </span>

                <span className={`ucombo-chevron ${open ? 'is-open' : ''}`} aria-hidden="true">
          ▾
        </span>
            </button>

            {open && (
                <div className="ucombo-menu">
                    <div className="ucombo-searchRow">
                        <input
                            ref={searchRef}
                            className="input ucombo-search"
                            placeholder={searchPlaceholder}
                            value={q}
                            onChange={(e) => {
                                setQ(e.target.value);
                                setActiveIndex(-1);
                            }}
                            onKeyDown={onSearchKeyDown}
                        />

                        <button
                            type="button"
                            className="btn ucombo-clearBtn"
                            onClick={clearUser}
                            title="Unassign"
                        >
                            Unassign
                        </button>
                    </div>

                    {q.trim().length < 2 && (
                        <div className="ucombo-empty">Type at least 2 characters…</div>
                    )}

                    {q.trim().length >= 2 && loading && (
                        <div className="ucombo-empty">Searching…</div>
                    )}

                    {q.trim().length >= 2 && !loading && items.length === 0 && (
                        <div className="ucombo-empty">No users found</div>
                    )}

                    <div className="ucombo-list" role="listbox">
                        {items.map((u, idx) => (
                            <button
                                type="button"
                                key={u.id}
                                className={`ucombo-item ${idx === activeIndex ? 'is-active' : ''}`}
                                onMouseEnter={() => setActiveIndex(idx)}
                                onClick={() => selectUser(u)}
                            >
                                <div className="ucombo-email">{u.email}</div>
                                <div className="ucombo-id">#{u.id}</div>
                            </button>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}