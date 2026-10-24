import { NavLink, Outlet } from 'react-router-dom'

export default function AdminLayout() {
  return (
    <div className="admin-area">
      <header className="admin-header">
        <div className="admin-header-copy">
          <p className="helper-overline">Verwaltung</p>
          <h1>Adminbereich</h1>
          <p>Stellen, Partnerfirmen und Kontaktanfragen an einem Ort.</p>
        </div>
        <span className="admin-demo-badge">Änderungen werden zentral gespeichert</span>
      </header>
      <nav className="admin-nav" aria-label="Admin-Navigation">
        <NavLink to="/admin" end>Übersicht</NavLink>
        <NavLink to="/admin/stellen">Stellen</NavLink>
        <NavLink to="/admin/partnerfirmen">Partnerfirmen</NavLink>
        <NavLink to="/admin/anfragen">Anfragen</NavLink>
      </nav>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  )
}
