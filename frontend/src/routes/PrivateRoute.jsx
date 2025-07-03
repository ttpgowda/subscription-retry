import React, { useEffect, useState } from "react";
import PropTypes from "prop-types";
import { Navigate } from "react-router-dom";

function PrivateRoute({ children }) {
  const [loading, setLoading] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    (async () => {
      await checkAuth();
    })();
  }, []);

  const checkAuth = async () => {
    const accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    console.log("checkAuth::accessToken", accessToken);
    console.log("checkAuth::refreshToken", refreshToken);
    const isAccessTokenValid = await validateAccessToken(accessToken);

    if (isAccessTokenValid) {
      setAuthenticated(true);
    } else if (refreshToken) {
      const newAccessToken = await tryRefreshToken(refreshToken);
      if (newAccessToken) {
        localStorage.setItem("accessToken", newAccessToken);
        setAuthenticated(true);
      } else {
        localStorage.clear();
      }
    }

    setLoading(false);
  };

  const validateAccessToken = async (token) => {
    if (!token) return false;

    try {
      const res = await fetch("http://localhost:8080/auth/validate", {
        method: "GET",
        headers: { Authorization: `Bearer ${token}` },
      });

      return res.ok;
    } catch {
      return false;
    }
  };

  const tryRefreshToken = async (refreshToken) => {
    try {
      const res = await fetch("http://localhost:8080/auth/refresh", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      });

      if (res.ok) {
        const data = await res.json();
        return data.accessToken;
      }
    } catch {
      return null;
    }
  };

  if (loading) return <div>Loading...</div>;
  if (!authenticated) return <Navigate to="/authentication/sign-in" />;
  return children;
}

PrivateRoute.propTypes = {
  children: PropTypes.node.isRequired,
};

export default PrivateRoute;
