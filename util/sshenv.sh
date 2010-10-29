#!/bin/bash

# Script to set up environment for friendly robo SSHing. Exports botssh and
# botscp commands.
# 
# Usage:
# . sshenv.sh
#
# Note that this script has to be sourced, not executed, otherwise the exports
# don't work.
#
# To ssh to the bot:
# botssh
#
# To scp files to ~/ade on the robot:
# botscp file1 ... fileN
#
# Hint: Copy your public key to the bot to enable passwordless logins!

if [[ "$BASH_SOURCE" == "$0" ]]
then
    echo "Executing this file doesn't work. You need to source it like this:"
    echo ". $0"
    exit 1
fi

echo -n "Tell me the IP of the robot (ifconfig wlan0): "
read BOTIP

alias botssh='ssh bbr10@$BOTIP'

function botscp()
{
    scp $@ bbr10@$BOTIP:~/ade
    # echo "Copied $@ to ~/ade on robot."
}

export -f botscp

echo "Installed botssh and botscp commands."
