import { create } from "zustand";
import { persist } from "zustand/middleware";

interface AuthState {
  isAuthenticated: boolean;
  user: {
    id: string;
    username: string;
    email: string;
    firstName?: string;
    lastName?: string;
    riskTolerance?: string;
    tradingStyle?: string;
    accountType?: string;
    accountStatus?: string;
  } | null;
  token: string | null;
  login: (username: string, password: string) => Promise<boolean>;
  register: (userData: RegisterData) => Promise<boolean>;
  logout: () => void;
  setToken: (token: string) => void;
}

interface RegisterData {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  address?: string;
  riskTolerance?: string;
  tradingStyle?: string;
  accountType?: string;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      isAuthenticated: false,
      user: null,
      token: null,

      login: async (username: string, password: string) => {
        try {
          const response = await fetch("http://localhost:3001/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
          });

          if (response.ok) {
            const data = await response.json();
            set({
              isAuthenticated: true,
              user: data.user,
              token: data.access_token,
            });
            localStorage.setItem("auth_token", data.access_token);
            return true;
          }
          return false;
        } catch (error) {
          console.error("Login error:", error);
          return false;
        }
      },

      register: async (userData: RegisterData) => {
        try {
          const response = await fetch("http://localhost:3001/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(userData),
          });

          if (response.ok) {
            const data = await response.json();
            set({
              isAuthenticated: true,
              user: data.user,
              token: data.access_token,
            });
            localStorage.setItem("auth_token", data.access_token);
            return true;
          }
          return false;
        } catch (error) {
          console.error("Registration error:", error);
          return false;
        }
      },

      logout: () => {
        set({
          isAuthenticated: false,
          user: null,
          token: null,
        });
        localStorage.removeItem("auth_token");
      },

      setToken: (token: string) => {
        set({ token });
        localStorage.setItem("auth_token", token);
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        isAuthenticated: state.isAuthenticated,
        user: state.user,
        token: state.token,
      }),
    }
  )
);
