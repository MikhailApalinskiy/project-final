import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {api, setToken} from '../api';

export default function RegisterPage() {
    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    async function onSubmit(e) {
        e.preventDefault();
        setError('');

        try {
            const res = await api.register(email, password);
            setToken(res.accessToken);
            navigate('/tasks');
        } catch (e) {
            setError(e.message);
        }
    }

    return (
        <div className="auth-container">
            <h2 className="auth-title">Register</h2>

            <form className="auth-form" onSubmit={onSubmit}>
                <input
                    className="auth-input"
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    required
                />

                <input
                    className="auth-input"
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                />

                <button className="auth-button" type="submit">
                    Create account
                </button>
            </form>

            {error && <div className="auth-error">{error}</div>}

            <div className="auth-footer">
                Already have an account? <a href="/login">Login</a>
            </div>
        </div>
    );
}