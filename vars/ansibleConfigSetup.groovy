def call() {
	sh """
cat > ansible.cfg <<-'EOF'
[defaults]
forks = 20
host_key_checking = False
remote_user = root
roles_path = roles/
gathering = smart
fact_caching = jsonfile
fact_caching_connection = $HOME/ansible/facts
fact_caching_timeout = 600
log_path = $HOME/ansible.log
nocows = 1
callback_whitelist = profile_tasks

[privilege_escalation]
become = False

[ssh_connection]
ssh_args = -o ControlMaster=auto -o ControlPersist=600s
control_path = %(directory)s/%%h-%%r
pipelining = True
timeout = 10
EOF
	"""

	log.info("${pwd()}/ansible.cfg file created!")
	env.ANSIBLE_CONFIG = "${pwd()}/ansible.cfg"
}