import {Routes, Route, Navigate} from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import TasksPage from "./pages/TasksPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import ProtectedLayout from "./components/ProtectedLayout";

export default function App() {
    return (
        <Routes>
            {/* public */}
            <Route path="/login" element={<LoginPage/>}/>
            <Route path="/register" element={<RegisterPage/>}/>

            {/* protected area */}
            <Route element={<ProtectedLayout/>}>
                <Route path="/tasks" element={<TasksPage/>}/>
                <Route path="/admin/users" element={<AdminUsersPage/>}/>
            </Route>

            {/* fallback */}
            <Route path="*" element={<Navigate to="/tasks" replace/>}/>
        </Routes>
    );
}