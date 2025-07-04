import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

import Card from "@mui/material/Card";
import MDBox from "components/MDBox";
import MDTypography from "components/MDTypography";
import MDInput from "components/MDInput";
import MDButton from "components/MDButton";
import BasicLayout from "layouts/authentication/components/BasicLayout";
import CustomSnackbar from "../../../assets/theme-dark/components/CustomSnackbar";

// Assets
import bgImage from "assets/images/bg-sign-in-basic.jpeg";

function Basic() {
  const [rememberMe, setRememberMe] = useState(false);
  const [formData, setFormData] = useState({ username: "", password: "" });
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");
  const [snackbarSeverity, setSnackbarSeverity] = useState("info");
  const navigate = useNavigate();

  // 🔐 Check for valid session
  useEffect(() => {
    const checkAuth = async () => {
      const accessToken = localStorage.getItem("accessToken");
      const refreshToken = localStorage.getItem("refreshToken");

      if (await isAccessTokenValid(accessToken)) {
        navigate("/dashboard");
      } else if (refreshToken) {
        const newToken = await refreshAccessToken(refreshToken);
        if (newToken) {
          localStorage.setItem("accessToken", newToken);
          navigate("/dashboard");
        }
      }
    };

    checkAuth();
  }, []);

  const isAccessTokenValid = async (token) => {
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

  const refreshAccessToken = async (refreshToken) => {
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

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Tenant-ID": "dev-tenant",
        },
        body: JSON.stringify(formData),
      });

      const data = await res.json();

      if (res.ok) {
        localStorage.setItem("accessToken", data.accessToken);
        localStorage.setItem("refreshToken", data.refreshToken);
        setSnackbarSeverity("success");
        setSnackbarMessage("Login successful!");
        setSnackbarOpen(true);
        setTimeout(() => navigate("/dashboard"), 1000);
      } else {
        setSnackbarSeverity("error");
        setSnackbarMessage(data.message || "Login failed.");
        setSnackbarOpen(true);
      }
    } catch (err) {
      setSnackbarSeverity("error");
      setSnackbarMessage("Network error. Please try again.");
      setSnackbarOpen(true);
    }
  };

  const handleSetRememberMe = () => setRememberMe(!rememberMe);

  return (
    // BasicLayout: Background set to a very light, warm off-white or subtle texture
    <BasicLayout image={bgImage}>
      <Card>
        {/* Header MDBox: Integrated, no protruding gradient */}
        <MDBox
          // Removed gradient and coloredShadow for a minimalist look.
          // Option 1: Just padding and text color
          mx={2}
          pt={4} // More top padding for breathing room
          pb={2} // Padding below the title
          textAlign="center"
        >
          <MDTypography
            variant="h4"
            fontWeight="bold"
            color="info" // Use info color for the main title
          >
            Sign in
          </MDTypography>
          {/* Optional: A subtle divider */}
          <MDBox
            sx={{
              width: "50px",
              height: "2px",
              backgroundColor: (theme) => theme.palette.info.main, // Use your info color
              margin: "8px auto 0 auto", // Centered below title
              borderRadius: "2px",
            }}
          />
        </MDBox>

        {/* Form Content */}
        <MDBox pt={3} pb={3} px={3}>
          {" "}
          {/* Adjusted padding */}
          <MDBox component="form" role="form">
            <MDBox mb={3}>
              {" "}
              {/* Increased margin bottom for spacing between inputs */}
              <MDInput
                type="email"
                label="Email"
                name="username"
                value={formData.username}
                onChange={handleChange}
                fullWidth
                // Assuming MDInput takes these props for styling
                // variant="outlined" // Explicitly use outlined if available for clear borders
                // focusColor="info" // Color of border/label on focus
              />
            </MDBox>
            <MDBox mb={3}>
              {" "}
              {/* Increased margin bottom */}
              <MDInput
                type="password"
                label="Password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                fullWidth
                // variant="outlined"
                // focusColor="info"
              />
            </MDBox>

            <MDBox mt={5} mb={1}>
              {" "}
              {/* Increased top margin for button */}
              <MDButton
                variant="contained" // Solid fill button
                color="info"
                fullWidth
                onClick={handleLogin}
                borderRadius="md" // Ensure consistent rounded corners
                sx={{ paddingY: 1.5 }} // Slightly increased vertical padding for a more substantial button
              >
                Sign in
              </MDButton>
            </MDBox>
          </MDBox>
        </MDBox>
      </Card>
      <CustomSnackbar
        open={snackbarOpen}
        onClose={() => setSnackbarOpen(false)}
        message={snackbarMessage}
        severity={snackbarSeverity}
      />
    </BasicLayout>
  );
}

export default Basic;
