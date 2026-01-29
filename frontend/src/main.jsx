import React from 'react'
import ReactDOM from 'react-dom/client'
import {BrowserRouter} from 'react-router-dom'
import App from './App'
import AuthEvents from './components/AuthEvents'

import './styles/base.css';
import './styles/ui.css';
import './styles/auth.css';
import './styles/users.css';
import './styles/tasks.css';
import './styles/layout.css';

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <BrowserRouter>
            <AuthEvents/>
            <App/>
        </BrowserRouter>
    </React.StrictMode>
)