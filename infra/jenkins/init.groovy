// ==================== JENKINS INITIAL CONFIGURATION ====================
// This script runs on Jenkins startup to configure the system

import jenkins.model.*
import hudson.security.*
import hudson.tools.*
import hudson.plugins.git.*
import hudson.plugins.maven.*
import hudson.plugins.nodejs.*
import hudson.slaves.*
import hudson.model.*

// ==================== SECURITY CONFIGURATION ====================
def instance = Jenkins.getInstance()

// Create admin user
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("admin", "admin123")
instance.setSecurityRealm(hudsonRealm)

// Set authorization strategy
def strategy = new GlobalMatrixAuthorizationStrategy()
strategy.add(Jenkins.ADMINISTER, "admin")
strategy.add(Jenkins.READ, "anonymous")
instance.setAuthorizationStrategy(strategy)

// ==================== GLOBAL TOOLS CONFIGURATION ====================
def descriptor = instance.getDescriptor("hudson.tasks.Maven$MavenInstallation")

// Configure Maven
def mavenInstallation = new Maven.MavenInstallation(
    "Maven-3.9.6",
    "/usr/share/maven",
    []
)
descriptor.setInstallations(mavenInstallation)
descriptor.save()

// Configure JDK
def jdkDescriptor = instance.getDescriptor("hudson.model.JDK")
def jdkInstallation = new JDK(
    "JDK-21",
    "/usr/lib/jvm/java-21-openjdk"
)
jdkDescriptor.setInstallations(jdkInstallation)
jdkDescriptor.save()

// Configure Node.js
def nodeDescriptor = instance.getDescriptor("hudson.plugins.nodejs.NodeJS")
def nodeInstallation = new NodeJSInstallation(
    "NodeJS-18",
    "/usr/bin/node",
    []
)
nodeDescriptor.setInstallations(nodeInstallation)
nodeDescriptor.save()

// ==================== SYSTEM CONFIGURATION ====================
// Set number of executors
instance.setNumExecutors(4)

// Set system message
instance.setSystemMessage("""
🚀 <strong>Quantis Trading Platform CI/CD</strong><br/>
<br/>
This Jenkins instance is configured for:<br/>
• <strong>Java Services:</strong> Spring Boot microservices<br/>
• <strong>Frontend:</strong> React TypeScript dashboard<br/>
• <strong>Database:</strong> PostgreSQL (Citus) + Cassandra<br/>
• <strong>Containerization:</strong> Docker + Docker Compose<br/>
• <strong>Testing:</strong> JUnit, Maven, npm<br/>
• <strong>Quality:</strong> SonarQube integration<br/>
<br/>
<em>Built with ❤️ for high-performance trading</em>
""")

// ==================== EMAIL CONFIGURATION ====================
def mailer = instance.getDescriptor("hudson.tasks.Mailer")
mailer.setSmtpServer("localhost")
mailer.setDefaultSuffix("@quantis.com")
mailer.setReplyToAddress("noreply@quantis.com")

// ==================== SAVE CONFIGURATION ====================
instance.save()

println "✅ Jenkins configuration completed successfully!"
println "🔐 Admin user: admin / admin123"
println "🚀 Ready for Quantis CI/CD pipeline!"
