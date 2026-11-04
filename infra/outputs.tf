output "frontend_url" {
  value = "http://${aws_eip.game.public_ip}"
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
output "aws_region" {
  value = var.aws_region
}
