import {useEffect} from "react";
import {useNavigate} from "react-router-dom";

export default function AuthEvents() {
    const navigate = useNavigate();

    useEffect(() => {
        function onLogout() {
            navigate("/login", {replace: true});
        }

        window.addEventListener("auth:logout", onLogout);
        return () => window.removeEventListener("auth:logout", onLogout);
    }, [navigate]);

    return null;
}