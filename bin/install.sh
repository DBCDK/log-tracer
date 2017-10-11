#!/bin/bash
#
# Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
# See license text in LICENSE.md
#

log_tracer_home="$HOME/.log-tracer"
log_tracer_archive="${log_tracer_home}/archive"
log_tracer_bin="${log_tracer_home}/bin"

echo "Looking for curl..."
if [ -z $(which curl) ]; then
	echo "Not found."
	echo ""
	echo "======================================================================================================"
	echo " Please install curl on your system using your favourite package manager."
	echo ""
	echo " Restart after installing curl."
	echo "======================================================================================================"
	echo ""
	exit 1
fi

echo "Looking for jq..."
if [ -z $(which jq) ]; then
	echo "Not found."
	echo ""
	echo "======================================================================================================"
	echo " Please install jq on your system using your favourite package manager."
	echo ""
	echo " Restart after installing jq."
	echo "======================================================================================================"
	echo ""
	exit 1
fi

echo "Looking for java..."
if [ -z $(which java) ]; then
	echo "Not found."
	echo ""
	echo "======================================================================================================"
	echo " Please install java on your system using your favourite package manager."
	echo ""
	echo " Restart after installing java."
	echo "======================================================================================================"
	echo ""
	exit 1
fi

echo "Installing/updating log-tracer scripts..."

# Create directory structure

echo "Creating distribution directories..."
mkdir -pv "$log_tracer_archive"
mkdir -pv "$log_tracer_bin"

echo "Fetching artifacts..."
tag=`curl -sL https://api.github.com/repos/DBCDK/log-tracer/releases/latest | jq -r ".tag_name"`
curl -sL https://github.com/DBCDK/log-tracer/releases/download/${tag}/install.sh -o ${log_tracer_bin}/install.sh
curl -sL https://github.com/DBCDK/log-tracer/releases/download/${tag}/log-tracer -o ${log_tracer_bin}/log-tracer
curl -sL https://github.com/DBCDK/log-tracer/releases/download/${tag}/log-tracer.jar -o ${log_tracer_archive}/log-tracer-${tag}.jar

if [ $? -eq 0 ]; then
    [ -e ${log_tracer_archive}/log-tracer-current.jar ] && rm ${log_tracer_archive}/log-tracer-current.jar
    ln -s ${log_tracer_archive}/log-tracer-${tag}.jar  ${log_tracer_archive}/log-tracer-current.jar
    echo ${tag} > ${log_tracer_home}/version
fi
chmod a+x ${log_tracer_bin}/install.sh
chmod a+x ${log_tracer_bin}/log-tracer

if [ -f ~/.bash_aliases ]; then
    grep "log-tracer=~/.log-tracer/bin/log-tracer" ~/.bash_aliases ||
        echo -e "\nalias log-tracer=~/.log-tracer/bin/log-tracer" >> ~/.bash_aliases ; . ~/.bash_aliases
fi