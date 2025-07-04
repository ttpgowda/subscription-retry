import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";

// @mui material components
import Grid from "@mui/material/Grid"; // Import Grid for layout
import Card from "@mui/material/Card";
import Icon from "@mui/material/Icon"; // Ensure Icon is imported

// Material Dashboard 2 React components
import MDBox from "components/MDBox";
import MDTypography from "components/MDTypography";
import MDInput from "components/MDInput";
import MDButton from "components/MDButton";

// Material Dashboard 2 React example components
import DashboardLayout from "examples/LayoutContainers/DashboardLayout";
import DashboardNavbar from "examples/Navbars/DashboardNavbar";
import Footer from "examples/Footer";

import CustomSnackbar from "../../../assets/theme-dark/components/CustomSnackbar";

function CreateTenant() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    tenantId: "",
    tenantName: "",
    contactEmail: "",
    phone: "",
    subDomain: "",
    username: "",
    password: "",
    userEmail: "",
    fullName: "",
  });

  const [errors, setErrors] = useState({});
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");
  const [snackbarSeverity, setSnackbarSeverity] = useState("success");

  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const accessToken = localStorage.getItem("accessToken");
    if (!accessToken) {
      navigate("/authentication/sign-in");
      return;
    }
    try {
      const decodedToken = jwtDecode(accessToken);
      if (!decodedToken.roles || !decodedToken.roles.includes("ROLE_SUPER_ADMIN")) {
        navigate("/dashboard");
        setSnackbarMessage("You do not have permission to access this page.");
        setSnackbarSeverity("error");
        setSnackbarOpen(true);
      }
    } catch (error) {
      console.error("Error decoding token:", error);
      navigate("/authentication/sign-in");
    }
  }, [navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const validate = () => {
    let tempErrors = {};
    let isValid = true;

    if (!formData.tenantId.trim()) {
      tempErrors.tenantId = "Tenant ID is required.";
      isValid = false;
    } else if (!/^[a-zA-Z0-9_-]+$/.test(formData.tenantId.trim())) {
      tempErrors.tenantId =
        "Tenant ID can only contain letters, numbers, hyphens, and underscores.";
      isValid = false;
    }
    if (formData.tenantId.trim().length < 3 || formData.tenantId.trim().length > 50) {
      tempErrors.tenantId = "Tenant ID must be between 3 and 50 characters.";
      isValid = false;
    }

    if (!formData.tenantName.trim()) {
      tempErrors.tenantName = "Tenant Name is required.";
      isValid = false;
    }
    if (formData.tenantName.trim().length < 2 || formData.tenantName.trim().length > 100) {
      tempErrors.tenantName = "Tenant name must be between 2 and 100 characters.";
      isValid = false;
    }

    if (!formData.contactEmail.trim()) {
      tempErrors.contactEmail = "Contact Email is required.";
      isValid = false;
    } else if (!/\S+@\S+\.\S+/.test(formData.contactEmail)) {
      tempErrors.contactEmail = "Email is not valid.";
      isValid = false;
    }

    if (!formData.username.trim()) {
      tempErrors.username = "User Username is required.";
      isValid = false;
    }
    if (formData.username.trim().length < 3 || formData.username.trim().length > 50) {
      tempErrors.username = "Username must be between 3 and 50 characters.";
      isValid = false;
    }

    if (!formData.password.trim()) {
      tempErrors.password = "User Password is required.";
      isValid = false;
    } else if (formData.password.length < 8) {
      tempErrors.password = "Password must be at least 8 characters long.";
      isValid = false;
    } else if (
      !/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\S+$).{8,}$/.test(formData.password)
    ) {
      tempErrors.password =
        "Password must contain at least one digit, one lowercase, one uppercase, and one special character.";
      isValid = false;
    }

    if (!formData.userEmail.trim()) {
      tempErrors.userEmail = "User Email is required.";
      isValid = false;
    } else if (!/\S+@\S+\.\S+/.test(formData.userEmail)) {
      tempErrors.userEmail = "User Email is not valid.";
      isValid = false;
    }

    if (!formData.fullName.trim()) {
      tempErrors.fullName = "User Full Name is required.";
      isValid = false;
    }
    if (formData.fullName.trim().length < 2 || formData.fullName.trim().length > 100) {
      tempErrors.fullName = "Full name must be between 2 and 100 characters.";
      isValid = false;
    }

    setErrors(tempErrors);
    return isValid;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (validate()) {
      const accessToken = localStorage.getItem("accessToken");
      if (!accessToken) {
        setSnackbarMessage("Authentication token not found.");
        setSnackbarSeverity("error");
        setSnackbarOpen(true);
        return;
      }

      setIsLoading(true);

      const requestBody = {
        tenantId: formData.tenantId.trim(),
        tenantName: formData.tenantName.trim(),
        contactEmail: formData.contactEmail.trim(),
        phone: formData.phone.trim() || null,
        subDomain: formData.subDomain.trim() || null,
        username: formData.username.trim(),
        password: formData.password.trim(),
        userEmail: formData.userEmail.trim(),
        fullName: formData.fullName.trim(),
      };

      try {
        const response = await fetch("http://localhost:8080/api/onboard-tenant", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
          },
          body: JSON.stringify(requestBody),
        });

        if (!response.ok) {
          const errorData = await response.json();
          const errorMessage =
            errorData.message ||
            (errorData.errors && errorData.errors.length > 0
              ? errorData.errors.map((err) => err.defaultMessage).join(", ")
              : "Failed to onboard new tenant.");
          throw new Error(errorMessage);
        }

        const newTenant = await response.json();
        console.log("Tenant and initial user created:", newTenant);

        setSnackbarMessage("Tenant and initial user created successfully!");
        setSnackbarSeverity("success");
        setSnackbarOpen(true);
        setFormData({
          tenantId: "",
          tenantName: "",
          contactEmail: "",
          phone: "",
          subDomain: "",
          username: "",
          password: "",
          userEmail: "",
          fullName: "",
        });
      } catch (error) {
        console.error("API Error:", error);
        setSnackbarMessage(error.message || "An error occurred during onboarding.");
        setSnackbarSeverity("error");
        setSnackbarOpen(true);
      } finally {
        setIsLoading(false);
      }
    } else {
      setSnackbarMessage("Please correct the errors in the form.");
      setSnackbarSeverity("error");
      setSnackbarOpen(true);
    }
  };

  return (
    <DashboardLayout>
      <DashboardNavbar />
      <MDBox py={3}>
        <Grid container spacing={3} justifyContent="center">
          {" "}
          {/* Center the form on the page */}
          <Grid item xs={12} lg={8}>
            {" "}
            {/* Form will take 8/12 columns on large screens */}
            <Card>
              <MDBox
                variant="gradient"
                bgColor="info"
                borderRadius="lg"
                coloredShadow="info"
                mx={2}
                mt={-3}
                p={2}
                mb={1}
                textAlign="center"
                display="flex"
                alignItems="center"
                justifyContent="center"
              >
                <Icon fontSize="medium" color="white" sx={{ mr: 1 }}>
                  {"domain_add"} {/* More relevant icon for new tenant/business */}
                </Icon>
                <MDTypography variant="h4" fontWeight="medium" color="white">
                  Onboard New Tenant
                </MDTypography>
              </MDBox>
              <MDBox mb={3} textAlign="center">
                <MDTypography variant="body2" color="text">
                  Fill out the details below to create a new tenant organization and its initial
                  admin user.
                </MDTypography>
              </MDBox>
              <MDBox pt={2} pb={3} px={3}>
                {" "}
                {/* Reduced top padding to adjust with new sub-cards */}
                <MDBox component="form" role="form" onSubmit={handleSubmit}>
                  <Grid container spacing={3}>
                    {" "}
                    {/* Main grid for side-by-side sections */}
                    {/* Tenant Details Section */}
                    <Grid item xs={12} md={6}>
                      {" "}
                      {/* Takes 12 columns on small, 6 on medium+ */}
                      <Card sx={{ height: "100%", p: 2, boxShadow: 3 }}>
                        {" "}
                        {/* Inner Card for elevation */}
                        <MDBox
                          variant="gradient"
                          bgColor="primary" // Different color for sub-card header
                          borderRadius="lg"
                          coloredShadow="primary"
                          mx={1} // Smaller margin for inner card header
                          mt={-3} // Lift header to overlap card top
                          p={1.5} // Smaller padding for inner card header
                          mb={2}
                          textAlign="center"
                          display="flex"
                          alignItems="center"
                          justifyContent="center"
                        >
                          <Icon fontSize="small" color="white" sx={{ mr: 0.5 }}>
                            {"business"} {/* Icon for Tenant Details */}
                          </Icon>
                          <MDTypography variant="h6" fontWeight="medium" color="white">
                            Tenant Organization
                          </MDTypography>
                        </MDBox>
                        <MDBox px={2}>
                          {" "}
                          {/* Padding for inputs inside the inner card */}
                          <MDBox mb={2}>
                            <MDInput
                              type="text"
                              label="Tenant ID (Unique Identifier)"
                              name="tenantId"
                              value={formData.tenantId}
                              onChange={handleChange}
                              fullWidth
                              error={!!errors.tenantId}
                              helperText={errors.tenantId}
                            />
                          </MDBox>
                          <MDBox mb={2}>
                            <MDInput
                              type="text"
                              label="Tenant Name"
                              name="tenantName"
                              value={formData.tenantName}
                              onChange={handleChange}
                              fullWidth
                              error={!!errors.tenantName}
                              helperText={errors.tenantName}
                            />
                          </MDBox>
                          <MDBox mb={2}>
                            <MDInput
                              type="email"
                              label="Contact Email"
                              name="contactEmail"
                              value={formData.contactEmail}
                              onChange={handleChange}
                              fullWidth
                              error={!!errors.contactEmail}
                              helperText={errors.contactEmail}
                            />
                          </MDBox>
                          <MDBox mb={2}>
                            <MDInput
                              type="tel"
                              label="Phone (Optional)"
                              name="phone"
                              value={formData.phone}
                              onChange={handleChange}
                              fullWidth
                              error={!!errors.phone}
                              helperText={errors.phone}
                            />
                          </MDBox>
                          <MDBox mb={2}>
                            <MDInput
                              type="text"
                              label="Subdomain (Optional)"
                              name="subDomain"
                              value={formData.subDomain}
                              onChange={handleChange}
                              fullWidth
                            />
                          </MDBox>
                        </MDBox>
                      </Card>
                    </Grid>
                    {/* Initial Tenant Admin User Details Section */}
                    <Grid item xs={12} md={6}>
                      <Card sx={{ height: "100%", p: 2, boxShadow: 3 }}>
                        {" "}
                        {/* Inner Card for elevation */}
                        <MDBox
                          variant="gradient"
                          bgColor="success" // Different color for sub-card header
                          borderRadius="lg"
                          coloredShadow="success"
                          mx={1} // Smaller margin for inner card header
                          mt={-3} // Lift header to overlap card top
                          p={1.5} // Smaller padding for inner card header
                          mb={2}
                          textAlign="center"
                          display="flex"
                          alignItems="center"
                          justifyContent="center"
                        >
                          <Icon fontSize="small" color="white" sx={{ mr: 0.5 }}>
                            {"manage_accounts"} {/* Icon for User Details */}
                          </Icon>
                          <MDTypography variant="h6" fontWeight="medium" color="white">
                            Initial Admin User
                          </MDTypography>
                        </MDBox>
                        <MDBox px={2}>
                          {" "}
                          {/* Padding for inputs inside the inner card */}
                          <MDBox mb={2}>
                            <MDInput
                              type="text"
                              label="Username"
                              name="username"
                              value={formData.username}
                              onChange={handleChange}
                              fullWidth
                              error={!!errors.username}
                              helperText={errors.username}
                            />
                          </MDBox>
                          <MDBox mb={2}>
                            <MDInput
                              type="password"
                              label="Password"
                              name="password"
                              value={formData.password}
                              onChange={handleChange}
                              fullWidth
                              error={!!errors.password}
                              helperText={errors.password}
                            />
                          </MDBox>
                          <MDBox mb={2}>
                            <MDInput
                              type="email"
                              label="Email"
                              name="userEmail"
                              value={formData.userEmail}
                              onChange={handleChange}
                              fullWidth
                              error={!!errors.userEmail}
                              helperText={errors.userEmail}
                            />
                          </MDBox>
                          <MDBox mb={2}>
                            <MDInput
                              type="text"
                              label="Full Name"
                              name="fullName"
                              value={formData.fullName}
                              onChange={handleChange}
                              fullWidth
                              error={!!errors.fullName}
                              helperText={errors.fullName}
                            />
                          </MDBox>
                        </MDBox>
                      </Card>
                    </Grid>
                  </Grid>

                  <MDBox mt={4} mb={1} display="flex" justifyContent="center">
                    {" "}
                    {/* Center the button */}
                    <MDButton
                      variant="gradient"
                      color="info" // Keeping 'info' for consistency with main header, or choose 'primary'/'success'
                      type="submit"
                      disabled={isLoading}
                      sx={{ minWidth: "200px" }} // Give the button a fixed minimum width for presence
                    >
                      {isLoading ? "Creating..." : "Create Tenant & User"}
                    </MDButton>
                  </MDBox>
                </MDBox>
              </MDBox>
            </Card>
          </Grid>
        </Grid>
      </MDBox>
      <Footer />
      <CustomSnackbar
        open={snackbarOpen}
        onClose={() => setSnackbarOpen(false)}
        message={snackbarMessage}
        severity={snackbarSeverity}
      />
    </DashboardLayout>
  );
}

export default CreateTenant;
