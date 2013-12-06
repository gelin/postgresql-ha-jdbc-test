#!/bin/sh

sudo iptables -A OUTPUT -d 192.168.7.92 -p tcp --dport 5432 -j REJECT --reject-with tcp-reset
sudo iptables -L OUTPUT -n -v
#sudo tcpkill host 192.168.7.92 and tcp port 5432

netstat -np | grep 5432
