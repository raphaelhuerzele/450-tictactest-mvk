output "frontend_url" {
  value = "http://${aws_s3_bucket_website_configuration.frontend.website_endpoint}"
}
output "terminal_url" {
  value = "http://${aws_eip.game.public_ip}/terminal/"
}
output "backend_version_url" {
  value = "http://${aws_eip.game.public_ip}/version"
}
output "instance_id" {
  value = aws_instance.game.id
}
output "frontend_bucket" {
  value = aws_s3_bucket.frontend.id
}
output "artifact_bucket" {
  value = aws_s3_bucket.artifacts.id
}
output "aws_region" {
  value = var.aws_region
}
