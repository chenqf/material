```shell
wget https://repo.huaweicloud.com/java/jdk/8u202-b08/jdk-8u202-linux-x64.tar.gz

tar xzvf jdk-8u202-linux-x64.tar.gz -C /opt/software/

echo "export JAVA_HOME=/opt/software/jdk1.8.0_202" >> /etc/profile
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/profile
echo "export CLASSPATH=.:\$JAVA_HOME/lib/dt.jar:\$JAVA_HOME/lib/tools.jar" >> /etc/profile

source /etc/profile

```