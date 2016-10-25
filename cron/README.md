# set cronjob for dumping

## shell script

following http://www.cyberciti.biz/faq/run-execute-sh-shell-script/

### move script to folder
mv /tmp/dump.sh /opt/

### set execute permission on script
chmod +x /opt/dump.sh

### run script for test
sh /opt/dump.sh

## cronjob

following https://www.stetic.com/developer/cronjob-linux-tutorial-und-crontab-syntax.html

### edit crontab
nano /etc/crontab

### set cronjob
#### jede minute
* * * * * root /opt/dump.sh
#### zweimal am Tag (11:59 und 23:59)
59 11 * * * root /opt/dump.sh
59 23 * * * root /opt/dump.sh

### start service if not started
service crond start