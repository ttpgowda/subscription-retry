// utils/auth.js
export function isUserAuthenticated() {
  const token = localStorage.getItem("authToken"); // Or check cookie/sessionStorage
  return token && token !== "expired"; // Add real validation as needed
}
