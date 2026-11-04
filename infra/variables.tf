variable "aws_region" {
  type    = string
  default = "us-east-1"
}
variable "environment" {
  type    = string
  default = "lab"
  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{0,15}$", var.environment))
    error_message = "Use a short lowercase environment name."
  }
}
variable "ami_id" {
  type        = string
  description = "Pinned Amazon Linux 2023 x86_64 AMI ID in the selected region."
  validation {
    condition     = can(regex("^ami-[a-f0-9]+$", var.ami_id))
    error_message = "Set an explicit AMI ID; do not resolve latest on each apply."
  }
}
variable "instance_type" {
  type    = string
  default = "t3.micro"
  validation {
    condition     = contains(["t3.micro", "t3.small"], var.instance_type)
    error_message = "The pinned runtime requires an x86_64 t3 instance allowed in the Lab."
  }
}
variable "instance_profile_name" {
  type    = string
  default = "LabInstanceProfile"
}
variable "http_cidrs" {
  type        = set(string)
  default     = ["0.0.0.0/0"]
  description = "Public game HTTP access; restrict to classroom/client CIDRs if possible."
  validation {
    condition     = alltrue([for cidr in var.http_cidrs : can(cidrnetmask(cidr))])
    error_message = "Use valid IPv4 CIDRs."
  }
}
