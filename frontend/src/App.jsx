import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import AuthProvider from './AuthProvider'
import RequireLogin from './components/RequireLogin'
import RequireRole from './components/RequireRole'
import Layout from './Layout'
import AdminLayout from './components/AdminLayout'
import AdminAnfragen from './pages/admin/AdminAnfragen'
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminStellen from './pages/admin/AdminStellen'
import Home from './pages/Home'
import Account from './pages/Account'
import Bewerbungshelfer from './pages/Bewerbungshelfer'
import Firmenkontakt from './pages/Firmenkontakt'
import Initiativ from './pages/Initiativ'
import Login from './pages/Login'
import Partnerfirmen from './pages/Partnerfirmen'
import Schulkontakt from './pages/Schulkontakt'
import Stelle from './pages/Stelle'
import StellenProvider from './StellenProvider'
import './App.css'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <StellenProvider>
          <Routes>
            <Route element={<Layout />}>
              <Route path="/" element={<Home />} />
              <Route path="/stelle/:id" element={<Stelle />} />
              <Route path="/initiativ" element={<Initiativ />} />
              <Route
                path="/bewerbungshelfer"
                element={
                  <RequireLogin>
                    <Bewerbungshelfer />
                  </RequireLogin>
                }
              />
              <Route path="/firmenkontakt" element={<Firmenkontakt />} />
              <Route path="/schulen" element={<Schulkontakt />} />
              <Route
                path="/admin"
                element={
                  <RequireRole role="admin">
                    <AdminLayout />
                  </RequireRole>
                }
              >
                <Route index element={<AdminDashboard />} />
                <Route path="stellen" element={<AdminStellen />} />
                <Route path="partnerfirmen" element={<Partnerfirmen />} />
                <Route path="anfragen" element={<AdminAnfragen />} />
              </Route>
              <Route path="/login" element={<Login />} />
              <Route
                path="/konto"
                element={
                  <RequireLogin>
                    <Account />
                  </RequireLogin>
                }
              />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Route>
          </Routes>
        </StellenProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
