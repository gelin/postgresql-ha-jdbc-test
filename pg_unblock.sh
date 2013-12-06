#!/bin/sh

sudo iptables -D OUTPUT -d 192.168.7.92 -p tcp --dport 5432 -j REJECT --reject-with tcp-reset
sudo iptables -L OUTPUT -n -v

netstat -np | grep 5432
