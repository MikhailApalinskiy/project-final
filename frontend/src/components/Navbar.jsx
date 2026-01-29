import {Link, NavLink, useNavigate} from "react-router-dom";
import {clearToken, getToken} from "../api";

export default function Navbar() {
    const navigate = useNavigate();
    const token = getToken();

    function logout() {
        clearToken();
        navigate("/login");
    }

    return (
        <header className="navbar">
            <div className="navbar-inner">
                <Link to={token ? "/tasks" : "/login"} className="brand">
                    <span className="brand-badge"/>
                    Task Manager
                </Link>

                <nav className="nav-links">
                    {token ? (
                        <>
                            <NavLink to="/tasks" className="nav-link">Tasks</NavLink>
                            <button className="btn btn-primary" onClick={logout}>Logout</button>
                        </>
                    ) : (
                        <>
                            <NavLink to="/login" className="nav-link">Login</NavLink>
                            <NavLink to="/register" className="nav-link">Register</NavLink>
                        </>
                    )}
                </nav>
            </div>
        </header>
    );
}