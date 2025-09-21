// import React from "react";
import { Routes, Route } from "react-router-dom";
import { Toaster } from "@/components/ui/toaster";
import { TradingDashboard } from "@/components/TradingDashboard";
import { LoginPage } from "@/components/LoginPage";
import { useAuthStore } from "@/stores/authStore";

function App() {
  const { isAuthenticated } = useAuthStore();

  if (!isAuthenticated) {
    return <LoginPage />;
  }

  return (
    <>
      <Routes>
        <Route path="/" element={<TradingDashboard />} />
        <Route path="/dashboard" element={<TradingDashboard />} />
      </Routes>
      <Toaster />
    </>
  );
}

export default App;
