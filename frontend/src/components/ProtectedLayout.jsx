import {Navigate, Outlet} from "react-router-dom";
import Layout from "./Layout";
import {getToken} from "../api";

export default function ProtectedLayout() {
    if (!getToken()) {
        return <Navigate to="/login" replace/>;
    }

    return (
        <Layout>
            <Outlet/>
        </Layout>
    );
}