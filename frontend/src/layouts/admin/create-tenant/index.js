import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode"; // For decoding token

// Material Dashboard 2 React components
import MDBox from "components/MDBox";
import MDTypography from "components/MDTypography";
import MDInput from "components/MDInput";
import MDButton from "components/MDButton";
import Card from "@mui/material/Card"; // Assuming Card is from MUI or a wrapper
import BasicLayout from "layouts/authentication/components/BasicLayout"; // Adjust path if needed

import CustomSnackbar from "../../../assets/theme-dark/components/CustomSnackbar";

// Assets
import bgImage from "assets/images/bg-sign-in-basic.jpeg";

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

  const [isLoading, setIsLoading] = useState(false); // State for loading indicator

  useEffect(() => {
    // Basic role check on component mount to redirect if not Super Admin
    const accessToken = localStorage.getItem("accessToken");
    if (!accessToken) {
      navigate("/authentication/sign-in"); // Redirect to login if no token
      return;
    }
    try {
      const decodedToken = jwtDecode(accessToken);
      // Ensure 'ROLE_SUPER_ADMIN' matches your JWT claim for roles
      if (!decodedToken.roles || !decodedToken.roles.includes("ROLE_SUPER_ADMIN")) {
        navigate("/dashboard"); // Redirect if not Super Admin
        setSnackbarMessage("You do not have permission to access this page.");
        setSnackbarSeverity("error");
        setSnackbarOpen(true);
      }
    } catch (error) {
      console.error("Error decoding token:", error);
      navigate("/authentication/sign-in"); // Redirect on token error
    }
  }, [navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear error for the field as user types
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const validate = () => {
    let tempErrors = {};
    let isValid = true;

    // Tenant Validation
    if (!formData.tenantId.trim()) {
      tempErrors.tenantId = "Tenant ID is required.";
      isValid = false;
    } else if (!/^[a-zA-Z0-9_-]+$/.test(formData.tenantId.trim())) {
      tempErrors.tenantId =
        "Tenant ID can only contain letters, numbers, hyphens, and underscores.";
      isValid = false;
    }
    // Added specific size validation to match backend DTO
    if (formData.tenantId.trim().length < 3 || formData.tenantId.trim().length > 50) {
      tempErrors.tenantId = "Tenant ID must be between 3 and 50 characters.";
      isValid = false;
    }

    if (!formData.tenantName.trim()) {
      tempErrors.tenantName = "Tenant Name is required.";
      isValid = false;
    }
    // Added specific size validation to match backend DTO
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

    // User Validation
    if (!formData.username.trim()) {
      tempErrors.username = "User Username is required.";
      isValid = false;
    }
    // Added specific size validation to match backend DTO
    if (formData.username.trim().length < 3 || formData.username.trim().length > 50) {
      tempErrors.username = "Username must be between 3 and 50 characters.";
      isValid = false;
    }

    if (!formData.password.trim()) {
      tempErrors.password = "User Password is required.";
      isValid = false;
    } else if (formData.password.length < 8) {
      // Updated to 8 characters for stronger match with backend
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
    // Added specific size validation to match backend DTO
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

      setIsLoading(true); // Start loading

      // --- Prepare the SINGLE combined DTO as per TenantOnboardingRequest ---
      const requestBody = {
        // Tenant Details
        tenantId: formData.tenantId.trim(),
        tenantName: formData.tenantName.trim(), // Match DTO field name
        contactEmail: formData.contactEmail.trim(),
        phone: formData.phone.trim() || null,
        subDomain: formData.subDomain.trim() || null,

        // Initial User Details
        username: formData.username.trim(),
        password: formData.password.trim(),
        userEmail: formData.userEmail.trim(), // Match DTO field name
        fullName: formData.fullName.trim(),
        // Roles are implicitly 'COMPANY_ADMIN' on the backend for this endpoint,
        // so no need to send them from the frontend for this specific API.
      };

      try {
        // --- Make a SINGLE API call to the new endpoint ---
        const response = await fetch("http://localhost:8080/api/onboard-tenant", {
          // <-- Correct endpoint
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
          },
          body: JSON.stringify(requestBody), // <-- Send the combined requestBody
        });

        if (!response.ok) {
          const errorData = await response.json();
          // Check if errorData has a 'message' or 'errors' array (for validation errors)
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
          // Clear form after success
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
        // Optionally redirect to a list of tenants or dashboard
        // navigate("/admin/tenants"); // Example redirect
      } catch (error) {
        console.error("API Error:", error);
        setSnackbarMessage(error.message || "An error occurred during onboarding.");
        setSnackbarSeverity("error");
        setSnackbarOpen(true);
      } finally {
        setIsLoading(false); // End loading, re-enable button
      }
    } else {
      setSnackbarMessage("Please correct the errors in the form.");
      setSnackbarSeverity("error");
      setSnackbarOpen(true);
    }
  };

  return (
    <BasicLayout image={bgImage}>
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
        >
          <MDTypography variant="h4" fontWeight="medium" color="white" mt={1}>
            Create New Tenant & User
          </MDTypography>
        </MDBox>
        <MDBox pt={4} pb={3} px={3}>
          <MDBox component="form" role="form" onSubmit={handleSubmit}>
            <MDTypography variant="h6" mt={2} mb={2}>
              Tenant Details
            </MDTypography>
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
                name="tenantName" // Correct name
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
                error={!!errors.phone} // Keep error check for potential phone validation
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

            <MDTypography variant="h6" mt={4} mb={2}>
              Initial Tenant Admin User Details
            </MDTypography>
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
                name="userEmail" // Correct name
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

            <MDBox mt={4} mb={1}>
              <MDButton
                variant="gradient"
                color="info"
                fullWidth
                type="submit"
                disabled={isLoading} // Disable button when loading
              >
                {isLoading ? "Creating..." : "Create Tenant & User"}
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

export default CreateTenant;
