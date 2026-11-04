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
  description       = "Browser terminal, health and public version endpoint"
}
resource "aws_vpc_security_group_egress_rule" "https" {
  security_group_id = aws_security_group.game.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
  description       = "SSM, S3, package and pinned HTTPS downloads"
}

resource "aws_s3_bucket" "artifacts" {
  bucket_prefix = "tictactest-${var.environment}-artifacts-"
}
resource "aws_s3_bucket_public_access_block" "artifacts" {
  bucket                  = aws_s3_bucket.artifacts.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
resource "aws_s3_bucket_server_side_encryption_configuration" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id
  rule {
    apply_server_side_encryption_by_default { sse_algorithm = "AES256" }
  }
}
resource "aws_s3_bucket" "frontend" {
  bucket_prefix = "tictactest-${var.environment}-frontend-"
}
resource "aws_s3_bucket_ownership_controls" "frontend" {
  bucket = aws_s3_bucket.frontend.id
  rule { object_ownership = "BucketOwnerEnforced" }
}
resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket                  = aws_s3_bucket.frontend.id
  block_public_acls       = true
  ignore_public_acls      = true
  block_public_policy     = false
  restrict_public_buckets = false
}
resource "aws_s3_bucket_policy" "frontend" {
  bucket = aws_s3_bucket.frontend.id
  policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Principal = "*", Action = "s3:GetObject", Resource = "${aws_s3_bucket.frontend.arn}/*" }]
  })
  depends_on = [aws_s3_bucket_public_access_block.frontend]
}
resource "aws_s3_bucket_website_configuration" "frontend" {
  bucket = aws_s3_bucket.frontend.id
  index_document { suffix = "index.html" }
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
    installer = filebase64("${path.module}/../ops/runtime/install-tools.sh")
    versions  = filebase64("${path.module}/../ops/runtime/versions.env")
    unit      = filebase64("${path.module}/../ops/runtime/tictactest.service")
    nginx     = filebase64("${path.module}/../ops/runtime/nginx.conf")
    deployer  = filebase64("${path.module}/../ops/runtime/deploy-backend.sh")
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
resource "aws_eip" "game" {
  domain = "vpc"
}
resource "aws_eip_association" "game" {
  instance_id   = aws_instance.game.id
  allocation_id = aws_eip.game.id
}
resource "aws_s3_object" "config" {
  bucket        = aws_s3_bucket.frontend.id
  key           = "config.js"
  content_type  = "application/javascript"
  cache_control = "no-store"
  content = "window.TICTACTEST_CONFIG = Object.freeze(${jsonencode({
    terminalUrl       = "http://${aws_eip.game.public_ip}/terminal/"
    backendVersionUrl = "http://${aws_eip.game.public_ip}/version"
  })});\n"
}
