import logo from '/logo.png'
import google from'./assets/google.png'
import './App.css'
import axios from "axios";
import {useEffect, useState} from "react";

type user ={
    name: string,
    email: string,
    id: string,
    picture: string
}

function App() {

    const [user, setUser] = useState<user | null | undefined>(undefined)

    const loadUser = () => {
        axios.get('/api/auth/me')
            .then(response => {
                setUser(response.data)
            })
            .catch(error => {
                setUser(null)
                console.error(error);
            })
    }

    useEffect(() => {
        loadUser()
    }, [])

    function login() {
        const host =
            window.location.host === "localhost:5173"
                ? "http://localhost:8080"
                : window.location.origin;

        window.open(host + "/oauth2/authorization/google", "_self");
    }

    function logout() {
        const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin

        window.open(host + '/logout', '_self')
    }

  return (
      <>
          <h1>SitemapDiff</h1>
          <img src={logo} className="logo" alt="SitemapDiff logo"/>
          {user && <p>Hallo, {user.name}!</p>}
          {user ? <button onClick={logout}>logout</button>  :
              <button onClick={login}>login with <img src={google} alt="Google logo" width={16}/>
              </button>}
      </>
  )
}

export default App
