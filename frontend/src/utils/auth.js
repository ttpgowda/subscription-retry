// utils/auth.js
import { jwtDecode } from "jwt-decode";

export function isUserAuthenticated() {
  const token = localStorage.getItem("authToken"); // Or check cookie/sessionStorage
  return token && token !== "expired"; // Add real validation as needed
}

export const decodeJwt = (token) => {
  try {
    return jwtDecode(token);
  } catch (error) {
    console.error("Error decoding JWT:", error);
    return null;
  }
};

export const hasRole = (token, roleToCheck) => {
  if (!token) {
    return false;
  }
  const decoded = decodeJwt(token);
  if (!decoded || !decoded.roles) {
    // Assuming 'roles' is the key for roles in your JWT payload
    return false;
  }
  // Assuming roles are an array of strings like ['SUPER_ADMIN', 'COMPANY_ADMIN']
  return decoded.roles.includes(roleToCheck);
};

export const getCurrentUserRoles = (token) => {
  if (!token) {
    return [];
  }
  const decoded = decodeJwt(token);
  return decoded?.roles || [];
};
