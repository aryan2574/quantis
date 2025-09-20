#!/bin/bash

# ==================== AWS EKS SETUP SCRIPT ====================
# This script sets up AWS EKS cluster for Quantis Trading Platform

set -e

echo "🚀 Setting up AWS EKS cluster for Quantis Trading Platform..."

# ==================== PREREQUISITES ====================
echo "📋 Checking prerequisites..."

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI is not installed. Please install AWS CLI first."
    echo "   https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html"
    exit 1
fi

# Check if eksctl is installed
if ! command -v eksctl &> /dev/null; then
    echo "❌ eksctl is not installed. Please install eksctl first."
    echo "   https://eksctl.io/introduction/installation/"
    exit 1
fi

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed. Please install kubectl first."
    echo "   https://kubernetes.io/docs/tasks/tools/"
    exit 1
fi

echo "✅ Prerequisites check passed!"

# ==================== AWS CONFIGURATION ====================
echo "🔧 Configuring AWS..."

# Check AWS credentials
if ! aws sts get-caller-identity &> /dev/null; then
    echo "❌ AWS credentials not configured. Please run:"
    echo "   aws configure"
    exit 1
fi

# Get AWS account ID
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "✅ AWS Account ID: $AWS_ACCOUNT_ID"

# Set region
AWS_REGION="us-west-2"
echo "✅ AWS Region: $AWS_REGION"

# ==================== CREATE KEY PAIR ====================
echo "🔑 Creating EC2 key pair..."

KEY_NAME="quantis-keypair"
if ! aws ec2 describe-key-pairs --key-names $KEY_NAME &> /dev/null; then
    aws ec2 create-key-pair --key-name $KEY_NAME --query 'KeyMaterial' --output text > ~/.ssh/$KEY_NAME.pem
    chmod 400 ~/.ssh/$KEY_NAME.pem
    echo "✅ Key pair created: ~/.ssh/$KEY_NAME.pem"
else
    echo "✅ Key pair already exists: $KEY_NAME"
fi

# ==================== CREATE EKS CLUSTER ====================
echo "🏗️ Creating EKS cluster..."

# Update cluster config with account ID
sed "s/ACCOUNT_ID/$AWS_ACCOUNT_ID/g" aws/eks-cluster.yaml > aws/eks-cluster-updated.yaml

# Create cluster
eksctl create cluster -f aws/eks-cluster-updated.yaml

echo "✅ EKS cluster created successfully!"

# ==================== CONFIGURE KUBECTL ====================
echo "🔧 Configuring kubectl..."

aws eks update-kubeconfig --region $AWS_REGION --name quantis-trading-cluster

echo "✅ kubectl configured!"

# ==================== INSTALL AWS LOAD BALANCER CONTROLLER ====================
echo "⚖️ Installing AWS Load Balancer Controller..."

# Create IAM policy
curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.6.0/docs/install/iam_policy.json

aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file://iam_policy.json

# Create service account
eksctl create iamserviceaccount \
    --cluster=quantis-trading-cluster \
    --namespace=kube-system \
    --name=aws-load-balancer-controller \
    --role-name=AmazonEKSLoadBalancerControllerRole \
    --attach-policy-arn=arn:aws:iam::$AWS_ACCOUNT_ID:policy/AWSLoadBalancerControllerIAMPolicy \
    --approve

# Install controller
helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
    -n kube-system \
    --set clusterName=quantis-trading-cluster \
    --set serviceAccount.create=false \
    --set serviceAccount.name=aws-load-balancer-controller

echo "✅ AWS Load Balancer Controller installed!"

# ==================== INSTALL CLUSTER AUTOSCALER ====================
echo "📈 Installing Cluster Autoscaler..."

# Create IAM policy for autoscaler
cat > cluster-autoscaler-policy.json << EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "autoscaling:DescribeAutoScalingGroups",
                "autoscaling:DescribeAutoScalingInstances",
                "autoscaling:DescribeLaunchConfigurations",
                "autoscaling:DescribeTags",
                "autoscaling:SetDesiredCapacity",
                "autoscaling:TerminateInstanceInAutoScalingGroup",
                "ec2:DescribeLaunchTemplateVersions"
            ],
            "Resource": "*"
        }
    ]
}
EOF

aws iam create-policy \
    --policy-name ClusterAutoscalerPolicy \
    --policy-document file://cluster-autoscaler-policy.json

# Create service account
eksctl create iamserviceaccount \
    --cluster=quantis-trading-cluster \
    --namespace=kube-system \
    --name=cluster-autoscaler \
    --role-name=AmazonEKSClusterAutoscalerRole \
    --attach-policy-arn=arn:aws:iam::$AWS_ACCOUNT_ID:policy/ClusterAutoscalerPolicy \
    --approve

# Install autoscaler
kubectl apply -f https://raw.githubusercontent.com/kubernetes/autoscaler/master/cluster-autoscaler/cloudprovider/aws/examples/cluster-autoscaler-autodiscover.yaml

# Update autoscaler deployment
kubectl patch deployment cluster-autoscaler \
    -n kube-system \
    -p '{"spec":{"template":{"metadata":{"annotations":{"cluster-autoscaler.kubernetes.io/safe-to-evict":"false"}},"spec":{"containers":[{"name":"cluster-autoscaler","image":"k8s.gcr.io/autoscaling/cluster-autoscaler:v1.28.0","command":["cluster-autoscaler","--v=4","--stderrthreshold=info","--cloud-provider=aws","--skip-nodes-with-local-storage=false","--expander=least-waste","--node-group-auto-discovery=asg:tag=k8s.io/cluster-autoscaler/enabled,k8s.io/cluster-autoscaler/quantis-trading-cluster"]}]}}}}}'

echo "✅ Cluster Autoscaler installed!"

# ==================== CREATE NAMESPACE ====================
echo "📁 Creating namespace..."

kubectl create namespace quantis-trading

echo "✅ Namespace created!"

# ==================== DEPLOY QUANTIS ====================
echo "🚀 Deploying Quantis Trading Platform..."

# Apply all Kubernetes manifests
kubectl apply -f k8s/

echo "✅ Quantis Trading Platform deployed!"

# ==================== VERIFY DEPLOYMENT ====================
echo "🔍 Verifying deployment..."

# Wait for pods to be ready
echo "⏳ Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod --all -n quantis-trading --timeout=300s

# Show pod status
echo "📊 Pod status:"
kubectl get pods -n quantis-trading

# Show services
echo "🌐 Services:"
kubectl get services -n quantis-trading

echo ""
echo "🎉 AWS EKS setup completed successfully!"
echo ""
echo "📋 Access Information:"
echo "   Cluster Name: quantis-trading-cluster"
echo "   Region: $AWS_REGION"
echo "   Namespace: quantis-trading"
echo ""
echo "🔧 Next Steps:"
echo "   1. Get external IP: kubectl get services -n quantis-trading"
echo "   2. Access dashboard: http://EXTERNAL-IP"
echo "   3. Monitor logs: kubectl logs -f deployment/trading-engine -n quantis-trading"
echo ""
echo "📚 Useful Commands:"
echo "   kubectl get pods -n quantis-trading"
echo "   kubectl get services -n quantis-trading"
echo "   kubectl logs -f deployment/trading-engine -n quantis-trading"
echo "   kubectl scale deployment trading-engine --replicas=5 -n quantis-trading"
echo ""
echo "🎯 Your Quantis Trading Platform is now running on AWS EKS!"
