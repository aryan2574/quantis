import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useAuthStore } from "@/stores/authStore";
import { useToast } from "@/hooks/use-toast";

export function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isRegistering, setIsRegistering] = useState(false);
  const { login, register } = useAuthStore();
  const { toast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      if (isRegistering) {
        const success = await register({
          username,
          email,
          password,
          firstName,
          lastName,
        });
        if (success) {
          toast({
            title: "Registration Successful",
            description: "Welcome to Quantis Trading Dashboard",
            variant: "success",
          });
        } else {
          toast({
            title: "Registration Failed",
            description: "Username or email already exists",
            variant: "destructive",
          });
        }
      } else {
        const success = await login(username, password);
        if (success) {
          toast({
            title: "Login Successful",
            description: "Welcome to Quantis Trading Dashboard",
            variant: "success",
          });
        } else {
          toast({
            title: "Login Failed",
            description: "Invalid username or password",
            variant: "destructive",
          });
        }
      }
    } catch (error) {
      toast({
        title: isRegistering ? "Registration Error" : "Login Error",
        description: "An error occurred",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold">Quantis Trading</CardTitle>
          <CardDescription>Professional Trading Dashboard</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            {isRegistering && (
              <>
                <div className="space-y-2">
                  <Label htmlFor="firstName">First Name</Label>
                  <Input
                    id="firstName"
                    type="text"
                    placeholder="Enter first name"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    required={isRegistering}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="lastName">Last Name</Label>
                  <Input
                    id="lastName"
                    type="text"
                    placeholder="Enter last name"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    required={isRegistering}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="Enter email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required={isRegistering}
                  />
                </div>
              </>
            )}
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input
                id="username"
                type="text"
                placeholder="Enter username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                placeholder="Enter password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading
                ? isRegistering
                  ? "Creating Account..."
                  : "Signing in..."
                : isRegistering
                ? "Create Account"
                : "Sign In"}
            </Button>
          </form>

          <div className="mt-4 text-center">
            <Button
              variant="link"
              onClick={() => setIsRegistering(!isRegistering)}
              className="text-sm"
            >
              {isRegistering
                ? "Already have an account? Sign In"
                : "Don't have an account? Create One"}
            </Button>
          </div>

          {!isRegistering && (
            <div className="mt-4 text-sm text-muted-foreground text-center">
              <p>Demo Credentials:</p>
              <p>
                Username: <strong>trader</strong>
              </p>
              <p>
                Password: <strong>password</strong>
              </p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
