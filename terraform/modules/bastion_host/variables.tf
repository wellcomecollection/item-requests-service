variable "name" {
  description = "Name of the ASG to create"
}

variable "subnet_list" {
  type = list(string)
}

variable "key_name" {
  description = "SSH key pair name for instance sign-in"
}

variable "vpc_id" {
  description = "VPC for EC2 autoscaling group security group"
}

variable "controlled_access_cidr_ingress" {
  description = "CIDR for SSH access to EC2 instances"
}

variable "custom_security_groups" {
  type    = list(string)
  default = []
}

# Bastion host-specific variables

variable "instance_type" {
  default     = "t2.nano"
  description = "AWS instance type"
}

variable "image_id" {
  description = "ID of the AMI to use on the instances"

  # Amazon Linux AMI
  default = "ami-9cbe9be5"
}

variable "user_data" {
  description = "User data for ec2 container hosts"
  default     = " "
}

variable "associate_public_ip_address" {
  description = "Associate public IP address?"
  default     = true
}

variable "ssh_ingress_security_groups" {
  type    = list(string)
  default = []
}
