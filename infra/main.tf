data "aws_vpc" "default" {
  default = true
}
data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  filter {
    name   = "default-for-az"
    values = ["true"]
  }
}
data "aws_iam_instance_profile" "lab" {
  name = var.instance_profile_name
}
resource "aws_security_group" "game" {
  name_prefix = "tictactest-${var.environment}-"
  description = "HTTP game only; management through SSM, no SSH or public ttyd port"
  vpc_id      = data.aws_vpc.default.id
}
resource "aws_vpc_security_group_ingress_rule" "http" {
  for_each          = var.http_cidrs
  security_group_id = aws_security_group.game.id
  cidr_ipv4         = each.value
  from_port         = 80
  to_port           = 80
  ip_protocol       = "tcp"
  description       = "Static frontend, terminal, health and version endpoint"
}
resource "aws_vpc_security_group_egress_rule" "https" {
  security_group_id = aws_security_group.game.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
  description       = "SSM, GitHub release assets and pinned HTTPS runtime downloads"
}
resource "aws_eip" "game" {
  domain = "vpc"
}
resource "aws_instance" "game" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  subnet_id                   = sort(data.aws_subnets.default.ids)[0]
  vpc_security_group_ids      = [aws_security_group.game.id]
  iam_instance_profile        = data.aws_iam_instance_profile.lab.name
  associate_public_ip_address = true
  user_data_replace_on_change = true
  user_data_base64 = base64gzip(templatefile("${path.module}/bootstrap.sh.tftpl", {
    installer         = filebase64("${path.module}/../ops/runtime/install-tools.sh")
    versions          = filebase64("${path.module}/../ops/runtime/versions.env")
    unit              = filebase64("${path.module}/../ops/runtime/tictactest.service")
    nginx             = filebase64("${path.module}/../ops/runtime/nginx.conf")
    deployer          = filebase64("${path.module}/../ops/runtime/deploy-backend.sh")
    frontend_deployer = filebase64("${path.module}/../ops/runtime/deploy-frontend.sh")
    frontend_config = base64encode("window.TICTACTEST_CONFIG = Object.freeze(${jsonencode({
      terminalUrl       = "http://${aws_eip.game.public_ip}/terminal/"
      backendVersionUrl = "http://${aws_eip.game.public_ip}/version"
    })});\n")
  }))
  metadata_options {
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }
  root_block_device {
    volume_size = 12
    volume_type = "gp3"
    encrypted   = true
  }
  tags = { Name = "tictactest-${var.environment}" }
}
resource "aws_eip_association" "game" {
  instance_id   = aws_instance.game.id
  allocation_id = aws_eip.game.id
}
