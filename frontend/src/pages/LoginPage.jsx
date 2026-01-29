import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {api, setToken} from '../api';

export default function LoginPage() {
    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    async function onSubmit(e) {
        e.preventDefault();
        setError('');

        try {
            const res = await api.login(email, password);
            setToken(res.accessToken);
            navigate('/');
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div className="auth-container">
            <h2 className="auth-title">Login</h2>

            <form className="auth-form" onSubmit={onSubmit}>
                <input
                    className="auth-input"
                    placeholder="Email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                />

                <input
                    className="auth-input"
                    placeholder="Password"
                    type="password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                />

                <button className="auth-button" type="submit">
                    Login
                </button>
            </form>

            {error && <div className="auth-error">{error}</div>}

            <div className="auth-footer">
                No account? <a href="/register">Register</a>
            </div>
        </div>
    );
}